package net.minecraft.src;

import java.io.IOException;
import java.net.Socket;
import net.minecraft.server.MinecraftServer;

class NetworkAcceptThread extends Thread {
	final MinecraftServer minecraftServer;
	final NetworkListenThread networkListenThread;

	NetworkAcceptThread(NetworkListenThread var1, String var2, MinecraftServer var3) {
		super(var2);
		this.networkListenThread = var1;
		this.minecraftServer = var3;
	}

	public void run() {
		while(this.networkListenThread.isListening) {
			try {
				Socket var1 = NetworkListenThread.getServerSocket(this.networkListenThread).accept();
				if(var1 != null) {
					NetLoginHandler var2 = new NetLoginHandler(this.minecraftServer, var1, "Connection #" + NetworkListenThread.incrementConnections(this.networkListenThread));
					NetworkListenThread.addPendingConnection(this.networkListenThread, var2);
				}
			} catch (IOException var3) {
				var3.printStackTrace();
			}
		}

	}
}
