package net.minecraft.src;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.server.MinecraftServer;

public class EntityPlayerMP extends EntityPlayer {
	public NetServerHandler playerNetServerHandler;
	public MinecraftServer mcServer;
	public ItemInWorldManager theItemInWorldManager;
	public double managedPosX;
	public double managedPosZ;
	public List loadedChunks = new LinkedList();
	public Set loadChunks = new HashSet();
	public double managedPosY;

	public EntityPlayerMP(MinecraftServer var1, World var2, String var3, ItemInWorldManager var4) {
		super(var2);
		int var5 = var2.spawnX + this.rand.nextInt(20) - 10;
		int var6 = var2.spawnZ + this.rand.nextInt(20) - 10;
		int var7 = var2.getTopSolidOrLiquidBlock(var5, var6);
		this.setLocationAndAngles((double)var5 + 0.5D, (double)var7, (double)var6 + 0.5D, 0.0F, 0.0F);
		this.mcServer = var1;
		this.stepHeight = 0.0F;
		var4.thisPlayer = this;
		this.username = var3;
		this.theItemInWorldManager = var4;
		this.yOffset = 0.0F;
	}

	public void onUpdate() {
	}

	public void onDeath(Entity var1) {
	}

	public boolean attackEntityFrom(Entity var1, int var2) {
		return false;
	}

	public void heal(int var1) {
	}

	public void onUpdateEntity() {
		super.onUpdate();
		ChunkCoordIntPair var1 = null;
		double var2 = 0.0D;

		for(int var4 = 0; var4 < this.loadedChunks.size(); ++var4) {
			ChunkCoordIntPair var5 = (ChunkCoordIntPair)this.loadedChunks.get(var4);
			double var6 = var5.a(this);
			if(var4 == 0 || var6 < var2) {
				var1 = var5;
				var2 = var5.a(this);
			}
		}

		if(var1 != null) {
			boolean var8 = false;
			if(var2 < 1024.0D) {
				var8 = true;
			}

			if(this.playerNetServerHandler.getNumChunkDataPackets() < 2) {
				var8 = true;
			}

			if(var8) {
				this.loadedChunks.remove(var1);
				this.playerNetServerHandler.sendPacket(new Packet51MapChunk(var1.chunkXPos * 16, 0, var1.chunkZPos * 16, 16, 128, 16, this.mcServer.worldMngr));
				List var9 = this.mcServer.worldMngr.getTileEntityList(var1.chunkXPos * 16, 0, var1.chunkZPos * 16, var1.chunkXPos * 16 + 16, 128, var1.chunkZPos * 16 + 16);

				for(int var10 = 0; var10 < var9.size(); ++var10) {
					TileEntity var7 = (TileEntity)var9.get(var10);
					this.playerNetServerHandler.sendPacket(new Packet59ComplexEntity(var7.xCoord, var7.yCoord, var7.zCoord, var7));
				}
			}
		}

	}

	public void onLivingUpdate() {
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.isJumping = false;
		super.onLivingUpdate();
	}

	public void onItemPickup(Entity var1, int var2) {
		if(!var1.isDead && var1 instanceof EntityItem) {
			this.playerNetServerHandler.sendPacket(new Packet17AddToInventory(((EntityItem)var1).item, var2));
			this.mcServer.entityTracker.sendPacketToTrackedPlayers(var1, new Packet22Collect(var1.entityID, this.entityID));
		}

		super.onItemPickup(var1, var2);
	}

	public void swingItem() {
		if(!this.isSwinging) {
			this.swingProgressInt = -1;
			this.isSwinging = true;
			this.mcServer.entityTracker.sendPacketToTrackedPlayers(this, new Packet18ArmAnimation(this, 1));
		}

	}

	protected float getEyeHeight() {
		return 1.62F;
	}
}
