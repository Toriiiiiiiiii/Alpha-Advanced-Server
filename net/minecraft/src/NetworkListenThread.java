package net.minecraft.src;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;

public class NetworkListenThread {
	public static Logger logger = Logger.getLogger("Minecraft");
	private ServerSocket serverSocket;
	private Thread listenThread;
	public volatile boolean isListening = false;
	private int connectionNumber = 0;
	private ArrayList pendingConnections = new ArrayList();
	private ArrayList playerList = new ArrayList();
	public MinecraftServer mcServer;

	public NetworkListenThread(MinecraftServer var1, InetAddress var2, int var3) throws IOException {
		this.mcServer = var1;
		this.serverSocket = new ServerSocket(var3, 0, var2);
		this.serverSocket.setPerformancePreferences(0, 2, 1);
		this.isListening = true;
		this.listenThread = new NetworkAcceptThread(this, "Listen thread", var1);
		this.listenThread.start();
	}

	public void addPlayer(NetServerHandler var1) {
		this.playerList.add(var1);
	}

	private void addPendingConnection(NetLoginHandler var1) {
		if(var1 == null) {
			throw new IllegalArgumentException("Got null pendingconnection!");
		} else {
			this.pendingConnections.add(var1);
		}
	}

	public void handleNetworkListenThread() {
		int var1;
		for(var1 = 0; var1 < this.pendingConnections.size(); ++var1) {
			NetLoginHandler var2 = (NetLoginHandler)this.pendingConnections.get(var1);

			try {
				var2.tryLogin();
			} catch (Exception var5) {
				var2.kickUser("Internal server error");
				logger.log(Level.WARNING, "Failed to handle packet: " + var5, var5);
			}

			if(var2.finishedProcessing) {
				this.pendingConnections.remove(var1--);
			}
		}

		for(var1 = 0; var1 < this.playerList.size(); ++var1) {
			NetServerHandler var6 = (NetServerHandler)this.playerList.get(var1);

			try {
				var6.handlePackets();
			} catch (Exception var4) {
				var6.kickPlayer("Internal server error");
				logger.log(Level.WARNING, "Failed to handle packet: " + var4, var4);
			}

			if(var6.connectionClosed) {
				this.playerList.remove(var1--);
			}
		}

	}

	static ServerSocket getServerSocket(NetworkListenThread var0) {
		return var0.serverSocket;
	}

	static int incrementConnections(NetworkListenThread var0) {
		return var0.connectionNumber++;
	}

	static void addPendingConnection(NetworkListenThread var0, NetLoginHandler var1) {
		var0.addPendingConnection(var1);
	}
}
