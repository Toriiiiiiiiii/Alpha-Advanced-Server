package net.minecraft.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;

public class ServerConfigurationManager {
	public static Logger logger = Logger.getLogger("Minecraft");
	public List playerEntities = new ArrayList();
	private MinecraftServer mcServer;
	private PlayerManager playerManagerObj;
	private int maxPlayers;
	private Set bannedPlayers = new HashSet();
	private Set bannedIPs = new HashSet();
	private Set ops = new HashSet();
	private File bannedPlayersFile;
	private File ipBanFile;
	private File opFile;
	private PlayerNBTManager playerNBTManagerObj;

	public ServerConfigurationManager(MinecraftServer var1) {
		this.mcServer = var1;
		this.bannedPlayersFile = var1.getFile("banned-players.txt");
		this.ipBanFile = var1.getFile("banned-ips.txt");
		this.opFile = var1.getFile("ops.txt");
		this.playerManagerObj = new PlayerManager(var1);
		this.maxPlayers = var1.propertyManagerObj.getIntProperty("max-players", 20);
		this.readBannedPlayers();
		this.loadBannedList();
		this.loadOps();
		this.writeBannedPlayers();
		this.saveBannedList();
		this.saveOps();
	}

	public void setPlayerManager(WorldServer var1) {
		this.playerNBTManagerObj = new PlayerNBTManager(new File(var1.saveDirectory, "players"));
	}

	public int getMaxTrackingDistance() {
		return this.playerManagerObj.getMaxTrackingDistance();
	}

	public void playerLoggedIn(EntityPlayerMP var1) {
		this.playerEntities.add(var1);
		this.playerNBTManagerObj.readPlayerNBT(var1);
		this.mcServer.worldMngr.chunkProviderServer.loadChunk((int)var1.posX >> 4, (int)var1.posZ >> 4);

		while(this.mcServer.worldMngr.getCollidingBoundingBoxes(var1, var1.boundingBox).size() != 0) {
			var1.setPosition(var1.posX, var1.posY + 1.0D, var1.posZ);
		}

		this.mcServer.worldMngr.spawnEntityInWorld(var1);
		this.playerManagerObj.addPlayer(var1);
	}

	public void serverUpdateMountedMovingPlayer(EntityPlayerMP var1) {
		this.playerManagerObj.updateMountedMovingPlayer(var1);
	}

	public void playerLoggedOut(EntityPlayerMP var1) {
		this.playerManagerObj.removePlayer(var1);
		this.playerNBTManagerObj.writePlayerNBT(var1);
		this.mcServer.worldMngr.setEntityDead(var1);
		this.playerEntities.remove(var1);
	}

	public EntityPlayerMP login(NetLoginHandler var1, String var2, String var3) {
		if(this.bannedPlayers.contains(var2.trim().toLowerCase())) {
			var1.kickUser("You are banned from this server!");
			return null;
		} else {
			String var4 = var1.netManager.getRemoteAddress().toString();
			var4 = var4.substring(var4.indexOf("/") + 1);
			var4 = var4.substring(0, var4.indexOf(":"));
			if(this.bannedIPs.contains(var4)) {
				var1.kickUser("Your IP address is banned from this server!");
				return null;
			} else if(this.playerEntities.size() >= this.maxPlayers) {
				var1.kickUser("The server is full!");
				return null;
			} else {
				for(int var5 = 0; var5 < this.playerEntities.size(); ++var5) {
					EntityPlayerMP var6 = (EntityPlayerMP)this.playerEntities.get(var5);
					if(var6.username.equalsIgnoreCase(var2)) {
						var6.playerNetServerHandler.kickPlayer("You logged in from another location");
					}
				}

				return new EntityPlayerMP(this.mcServer, this.mcServer.worldMngr, var2, new ItemInWorldManager(this.mcServer.worldMngr));
			}
		}
	}

	public void onTick() throws IOException {
		this.playerManagerObj.updatePlayerInstances();
	}

	public void markBlockNeedsUpdate(int var1, int var2, int var3) {
		this.playerManagerObj.markBlockNeedsUpdate(var1, var2, var3);
	}

	public void sendPacketToAllPlayers(Packet var1) {
		for(int var2 = 0; var2 < this.playerEntities.size(); ++var2) {
			EntityPlayerMP var3 = (EntityPlayerMP)this.playerEntities.get(var2);
			var3.playerNetServerHandler.sendPacket(var1);
		}

	}

	public String getPlayerList() {
		String var1 = "";

		for(int var2 = 0; var2 < this.playerEntities.size(); ++var2) {
			if(var2 > 0) {
				var1 = var1 + ", ";
			}

			var1 = var1 + ((EntityPlayerMP)this.playerEntities.get(var2)).username;
		}

		return var1;
	}

	public void banPlayer(String var1) {
		this.bannedPlayers.add(var1.toLowerCase());
		this.writeBannedPlayers();
	}

	public void pardonPlayer(String var1) {
		this.bannedPlayers.remove(var1.toLowerCase());
		this.writeBannedPlayers();
	}

	private void readBannedPlayers() {
		try {
			this.bannedPlayers.clear();
			BufferedReader var1 = new BufferedReader(new FileReader(this.bannedPlayersFile));
			String var2 = "";

			while(true) {
				var2 = var1.readLine();
				if(var2 == null) {
					var1.close();
					break;
				}

				this.bannedPlayers.add(var2.trim().toLowerCase());
			}
		} catch (Exception var3) {
			logger.warning("Failed to load ban list: " + var3);
		}

	}

	private void writeBannedPlayers() {
		try {
			PrintWriter var1 = new PrintWriter(new FileWriter(this.bannedPlayersFile, false));
			Iterator var2 = this.bannedPlayers.iterator();

			while(var2.hasNext()) {
				String var3 = (String)var2.next();
				var1.println(var3);
			}

			var1.close();
		} catch (Exception var4) {
			logger.warning("Failed to save ban list: " + var4);
		}

	}

	public void banIP(String var1) {
		this.bannedIPs.add(var1.toLowerCase());
		this.saveBannedList();
	}

	public void pardonIP(String var1) {
		this.bannedIPs.remove(var1.toLowerCase());
		this.saveBannedList();
	}

	private void loadBannedList() {
		try {
			this.bannedIPs.clear();
			BufferedReader var1 = new BufferedReader(new FileReader(this.ipBanFile));
			String var2 = "";

			while(true) {
				var2 = var1.readLine();
				if(var2 == null) {
					var1.close();
					break;
				}

				this.bannedIPs.add(var2.trim().toLowerCase());
			}
		} catch (Exception var3) {
			logger.warning("Failed to load ip ban list: " + var3);
		}

	}

	private void saveBannedList() {
		try {
			PrintWriter var1 = new PrintWriter(new FileWriter(this.ipBanFile, false));
			Iterator var2 = this.bannedIPs.iterator();

			while(var2.hasNext()) {
				String var3 = (String)var2.next();
				var1.println(var3);
			}

			var1.close();
		} catch (Exception var4) {
			logger.warning("Failed to save ip ban list: " + var4);
		}

	}

	public void opPlayer(String var1) {
		this.ops.add(var1.toLowerCase());
		this.saveOps();
	}

	public void deopPlayer(String var1) {
		this.ops.remove(var1.toLowerCase());
		this.saveOps();
	}

	private void loadOps() {
		try {
			this.ops.clear();
			BufferedReader var1 = new BufferedReader(new FileReader(this.opFile));
			String var2 = "";

			while(true) {
				var2 = var1.readLine();
				if(var2 == null) {
					var1.close();
					break;
				}

				this.ops.add(var2.trim().toLowerCase());
			}
		} catch (Exception var3) {
			logger.warning("Failed to load ip ban list: " + var3);
		}

	}

	private void saveOps() {
		try {
			PrintWriter var1 = new PrintWriter(new FileWriter(this.opFile, false));
			Iterator var2 = this.ops.iterator();

			while(var2.hasNext()) {
				String var3 = (String)var2.next();
				var1.println(var3);
			}

			var1.close();
		} catch (Exception var4) {
			logger.warning("Failed to save ip ban list: " + var4);
		}

	}

	public boolean isOp(String var1) {
		return this.ops.contains(var1.trim().toLowerCase());
	}

	public EntityPlayerMP getPlayerEntity(String var1) {
		for(int var2 = 0; var2 < this.playerEntities.size(); ++var2) {
			EntityPlayerMP var3 = (EntityPlayerMP)this.playerEntities.get(var2);
			if(var3.username.equalsIgnoreCase(var1)) {
				return var3;
			}
		}

		return null;
	}

	public void sendChatMessageToPlayer(String var1, String var2) {
		EntityPlayerMP var3 = this.getPlayerEntity(var1);
		if(var3 != null) {
			var3.playerNetServerHandler.sendPacket(new Packet3Chat(var2));
		}

	}

	public void sendChatMessageToAllOps(String var1) {
		Packet3Chat var2 = new Packet3Chat(var1);

		for(int var3 = 0; var3 < this.playerEntities.size(); ++var3) {
			EntityPlayerMP var4 = (EntityPlayerMP)this.playerEntities.get(var3);
			if(this.isOp(var4.username)) {
				var4.playerNetServerHandler.sendPacket(var2);
			}
		}

	}

	public boolean sendPacketToPlayer(String var1, Packet var2) {
		EntityPlayerMP var3 = this.getPlayerEntity(var1);
		if(var3 != null) {
			var3.playerNetServerHandler.sendPacket(var2);
			return true;
		} else {
			return false;
		}
	}

	public void sentTileEntityToPlayer(int var1, int var2, int var3, TileEntity var4) {
		this.playerManagerObj.sendTileEntity(new Packet59ComplexEntity(var1, var2, var3, var4), var1, var2, var3);
	}

	public void savePlayerStates() {
		for(int var1 = 0; var1 < this.playerEntities.size(); ++var1) {
			this.playerNBTManagerObj.writePlayerNBT((EntityPlayerMP)this.playerEntities.get(var1));
		}

	}
}
