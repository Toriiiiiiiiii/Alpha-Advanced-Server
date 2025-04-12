package net.minecraft.src;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;

public class NetLoginHandler extends NetHandler {
	public static Logger logger = Logger.getLogger("Minecraft");
	private static Random rand = new Random();
	public NetworkManager netManager;
	public boolean finishedProcessing = false;
	private MinecraftServer mcServer;
	private int loginTimer = 0;
	private String username = null;
	private Packet1Login packet1login = null;
	private String serverId = "";

	public NetLoginHandler(MinecraftServer var1, Socket var2, String var3) throws IOException {
		this.mcServer = var1;
		this.netManager = new NetworkManager(var2, var3, this);
	}

	public void tryLogin() throws IOException {
		if(this.packet1login != null) {
			this.doLogin(this.packet1login);
			this.packet1login = null;
		}

		if(this.loginTimer++ == 100) {
			this.kickUser("Took too long to log in");
		} else {
			this.netManager.processReadPackets();
		}

	}

	public void kickUser(String var1) {
		logger.info("Disconnecting " + this.getUserAndIPString() + ": " + var1);
		this.netManager.addToSendQueue(new Packet255KickDisconnect(var1));
		this.netManager.serverShutdown();
		this.finishedProcessing = true;
	}

	public void handleHandshake(Packet2Handshake var1) {
		if(this.mcServer.onlineMode) {
			this.serverId = Long.toHexString(rand.nextLong());
			this.netManager.addToSendQueue(new Packet2Handshake(this.serverId));
		} else {
			this.netManager.addToSendQueue(new Packet2Handshake("-"));
		}

	}

	public void handleLogin(Packet1Login var1) {
		this.username = var1.username;
		if(var1.protocolVersion != 2) {
			this.kickUser("Outdated client!");
		} else {
			if(!this.mcServer.onlineMode) {
				this.doLogin(var1);
			} else {
				(new ThreadLoginVerifier(this, var1)).start();
			}

		}
	}

	public void doLogin(Packet1Login var1) {
		EntityPlayerMP var2 = this.mcServer.configManager.login(this, var1.username, var1.password);
		if(var2 != null) {
			logger.info(this.getUserAndIPString() + " logged in");
			NetServerHandler var3 = new NetServerHandler(this.mcServer, this.netManager, var2);
			var3.sendPacket(new Packet1Login("", "", 0));
			var3.sendPacket(new Packet6SpawnPosition(this.mcServer.worldMngr.spawnX, this.mcServer.worldMngr.spawnY, this.mcServer.worldMngr.spawnZ));
			this.mcServer.configManager.playerLoggedIn(var2);
			var3.teleportTo(var2.posX, var2.posY, var2.posZ, var2.rotationYaw, var2.rotationPitch);
			var3.sendInventoryPackets();
			this.mcServer.networkServer.addPlayer(var3);
			var3.sendPacket(new Packet4UpdateTime(this.mcServer.worldMngr.worldTime));
		}

		this.finishedProcessing = true;
	}

	public void handleErrorMessage(String var1) {
		logger.info(this.getUserAndIPString() + " lost connection");
		this.finishedProcessing = true;
	}

	public void registerPacket(Packet var1) {
		this.kickUser("Protocol error");
	}

	public String getUserAndIPString() {
		return this.username != null ? this.username + " [" + this.netManager.getRemoteAddress().toString() + "]" : this.netManager.getRemoteAddress().toString();
	}

	static String getServerId(NetLoginHandler var0) {
		return var0.serverId;
	}

	static Packet1Login setLoginPacket(NetLoginHandler var0, Packet1Login var1) {
		return var0.packet1login = var1;
	}
}
