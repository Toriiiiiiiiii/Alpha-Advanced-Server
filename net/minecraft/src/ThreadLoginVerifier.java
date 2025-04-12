package net.minecraft.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

class ThreadLoginVerifier extends Thread {
	final Packet1Login loginPacket;
	final NetLoginHandler loginHandler;

	ThreadLoginVerifier(NetLoginHandler var1, Packet1Login var2) {
		this.loginHandler = var1;
		this.loginPacket = var2;
	}

	public void run() {
		try {
			String var1 = NetLoginHandler.getServerId(this.loginHandler);
			URL var2 = new URL("http://session.minecraft.net/game/checkserver.jsp?user=" + this.loginPacket.username + "&serverId=" + var1);
			BufferedReader var3 = new BufferedReader(new InputStreamReader(var2.openStream()));
			String var4 = var3.readLine();
			var3.close();
			System.out.println("THE REPLY IS " + var4);
			if(var4.equals("YES")) {
				NetLoginHandler.setLoginPacket(this.loginHandler, this.loginPacket);
			} else {
				this.loginHandler.kickUser("Failed to verify username!");
			}
		} catch (Exception var5) {
			var5.printStackTrace();
		}

	}
}
