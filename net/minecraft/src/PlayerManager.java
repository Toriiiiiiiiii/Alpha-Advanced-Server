package net.minecraft.src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public class PlayerManager {
	private List players = new ArrayList();
	private MCHashTable2 playerInstances = new MCHashTable2();
	private List playerInstancesToUpdate = new ArrayList();
	private MinecraftServer mcServer;

	public PlayerManager(MinecraftServer var1) {
		this.mcServer = var1;
	}

	public void updatePlayerInstances() throws IOException {
		for(int var1 = 0; var1 < this.playerInstancesToUpdate.size(); ++var1) {
			((PlayerInstance)this.playerInstancesToUpdate.get(var1)).onUpdate();
		}

		this.playerInstancesToUpdate.clear();
	}

	private PlayerInstance getPlayerInstance(int var1, int var2, boolean var3) {
		long var4 = (long)var1 + 2147483647L | (long)var2 + 2147483647L << 32;
		PlayerInstance var6 = (PlayerInstance)this.playerInstances.lookup(var4);
		if(var6 == null && var3) {
			var6 = new PlayerInstance(this, var1, var2);
			this.playerInstances.addKey(var4, var6);
		}

		return var6;
	}

	public void sendTileEntity(Packet var1, int var2, int var3, int var4) {
		int var5 = var2 >> 4;
		int var6 = var4 >> 4;
		PlayerInstance var7 = this.getPlayerInstance(var5, var6, false);
		if(var7 != null) {
			var7.sendTileEntity(var1);
		}

	}

	public void markBlockNeedsUpdate(int var1, int var2, int var3) {
		int var4 = var1 >> 4;
		int var5 = var3 >> 4;
		PlayerInstance var6 = this.getPlayerInstance(var4, var5, false);
		if(var6 != null) {
			var6.markBlockNeedsUpdate(var1 & 15, var2, var3 & 15);
		}

	}

	public void addPlayer(EntityPlayerMP var1) {
		this.mcServer.configManager.sendPacketToAllPlayers(new Packet3Chat("\u00a7e" + var1.username + " joined the game."));
		int var2 = (int)var1.posX >> 4;
		int var3 = (int)var1.posZ >> 4;
		var1.managedPosX = var1.posX;
		var1.managedPosZ = var1.posZ;

		for(int var4 = var2 - 10; var4 <= var2 + 10; ++var4) {
			for(int var5 = var3 - 10; var5 <= var3 + 10; ++var5) {
				this.getPlayerInstance(var4, var5, true).addPlayer(var1);
			}
		}

		this.players.add(var1);
	}

	public void removePlayer(EntityPlayerMP var1) {
		this.mcServer.configManager.sendPacketToAllPlayers(new Packet3Chat("\u00a7e" + var1.username + " left the game."));
		int var2 = (int)var1.posX >> 4;
		int var3 = (int)var1.posZ >> 4;

		for(int var4 = var2 - 10; var4 <= var2 + 10; ++var4) {
			for(int var5 = var3 - 10; var5 <= var3 + 10; ++var5) {
				PlayerInstance var6 = this.getPlayerInstance(var4, var5, false);
				if(var6 != null) {
					var6.removePlayer(var1);
				}
			}
		}

		this.players.remove(var1);
	}

	private boolean a(int var1, int var2, int var3, int var4) {
		int var5 = var1 - var3;
		int var6 = var2 - var4;
		return var5 >= -10 && var5 <= 10 ? var6 >= -10 && var6 <= 10 : false;
	}

	public void updateMountedMovingPlayer(EntityPlayerMP var1) {
		int var2 = (int)var1.posX >> 4;
		int var3 = (int)var1.posZ >> 4;
		double var4 = var1.managedPosX - var1.posX;
		double var6 = var1.managedPosZ - var1.posZ;
		double var8 = var4 * var4 + var6 * var6;
		if(var8 >= 64.0D) {
			int var10 = (int)var1.managedPosX >> 4;
			int var11 = (int)var1.managedPosZ >> 4;
			int var12 = var2 - var10;
			int var13 = var3 - var11;
			if(var12 != 0 || var13 != 0) {
				for(int var14 = var2 - 10; var14 <= var2 + 10; ++var14) {
					for(int var15 = var3 - 10; var15 <= var3 + 10; ++var15) {
						if(!this.a(var14, var15, var10, var11)) {
							this.getPlayerInstance(var14, var15, true).addPlayer(var1);
						}

						if(!this.a(var14 - var12, var15 - var13, var2, var3)) {
							PlayerInstance var16 = this.getPlayerInstance(var14 - var12, var15 - var13, false);
							if(var16 != null) {
								var16.removePlayer(var1);
							}
						}
					}
				}

				var1.managedPosX = var1.posX;
				var1.managedPosZ = var1.posZ;
			}
		}
	}

	public int getMaxTrackingDistance() {
		return 144;
	}

	static MinecraftServer getMinecraftServer(PlayerManager var0) {
		return var0.mcServer;
	}

	static MCHashTable2 getPlayerInstances(PlayerManager var0) {
		return var0.playerInstances;
	}

	static List getPlayerInstancesToUpdate(PlayerManager var0) {
		return var0.playerInstancesToUpdate;
	}
}
