package net.minecraft.src;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EntityTrackerEntry {
	public Entity trackedEntity;
	public int trackingDistanceThreshold;
	public int updateFrequency;
	public int encodedPosX;
	public int encodedPosY;
	public int encodedPosZ;
	public int encodedRotationYaw;
	public int encodedRotationPitch;
	public int updateCounter = 0;
	private double lastTrackedEntityPosX;
	private double lastTrackedEntityPosY;
	private double lastTrackedEntityPosZ;
	private boolean firstUpdateDone = false;
	public boolean playerEntitiesUpdated = false;
	public Set trackedPlayers = new HashSet();

	public EntityTrackerEntry(Entity var1, int var2, int var3) {
		this.trackedEntity = var1;
		this.trackingDistanceThreshold = var2;
		this.updateFrequency = var3;
		this.encodedPosX = MathHelper.floor_double(var1.posX * 32.0D);
		this.encodedPosY = MathHelper.floor_double(var1.posY * 32.0D);
		this.encodedPosZ = MathHelper.floor_double(var1.posZ * 32.0D);
		this.encodedRotationYaw = MathHelper.floor_float(var1.rotationYaw * 256.0F / 360.0F);
		this.encodedRotationPitch = MathHelper.floor_float(var1.rotationPitch * 256.0F / 360.0F);
	}

	public boolean equals(Object var1) {
		return var1 instanceof EntityTrackerEntry ? ((EntityTrackerEntry)var1).trackedEntity.entityID == this.trackedEntity.entityID : false;
	}

	public int hashCode() {
		return this.trackedEntity.entityID;
	}

	public void updatePlayerList(List var1) {
		this.playerEntitiesUpdated = false;
		if(!this.firstUpdateDone || this.trackedEntity.getDistanceSq(this.lastTrackedEntityPosX, this.lastTrackedEntityPosY, this.lastTrackedEntityPosZ) > 16.0D) {
			this.updatePlayerEntities(var1);
			this.lastTrackedEntityPosX = this.trackedEntity.posX;
			this.lastTrackedEntityPosY = this.trackedEntity.posY;
			this.lastTrackedEntityPosZ = this.trackedEntity.posZ;
			this.firstUpdateDone = true;
			this.playerEntitiesUpdated = true;
		}

		if(this.updateCounter++ % this.updateFrequency == 0) {
			int var2 = MathHelper.floor_double(this.trackedEntity.posX * 32.0D);
			int var3 = MathHelper.floor_double(this.trackedEntity.posY * 32.0D);
			int var4 = MathHelper.floor_double(this.trackedEntity.posZ * 32.0D);
			int var5 = MathHelper.floor_float(this.trackedEntity.rotationYaw * 256.0F / 360.0F);
			int var6 = MathHelper.floor_float(this.trackedEntity.rotationPitch * 256.0F / 360.0F);
			boolean var7 = var2 != this.encodedPosX || var3 != this.encodedPosY || var4 != this.encodedPosZ;
			boolean var8 = var5 != this.encodedRotationYaw || var6 != this.encodedRotationPitch;
			int var9 = var2 - this.encodedPosX;
			int var10 = var3 - this.encodedPosY;
			int var11 = var4 - this.encodedPosZ;
			Object var12 = null;
			if(var9 >= -128 && var9 < 128 && var10 >= -128 && var10 < 128 && var11 >= -128 && var11 < 128) {
				if(var7 && var8) {
					var12 = new Packet33RelEntityMoveLook(this.trackedEntity.entityID, (byte)var9, (byte)var10, (byte)var11, (byte)var5, (byte)var6);
				} else if(var7) {
					var12 = new Packet31RelEntityMove(this.trackedEntity.entityID, (byte)var9, (byte)var10, (byte)var11);
				} else if(var8) {
					var12 = new Packet32EntityLook(this.trackedEntity.entityID, (byte)var5, (byte)var6);
				} else {
					var12 = new Packet30Entity(this.trackedEntity.entityID);
				}
			} else {
				var12 = new Packet34EntityTeleport(this.trackedEntity.entityID, var2, var3, var4, (byte)var5, (byte)var6);
			}

			if(var12 != null) {
				this.sendPacketToTrackedPlayers((Packet)var12);
			}

			this.encodedPosX = var2;
			this.encodedPosY = var3;
			this.encodedPosZ = var4;
			this.encodedRotationYaw = var5;
			this.encodedRotationPitch = var6;
		}

	}

	public void sendPacketToTrackedPlayers(Packet var1) {
		Iterator var2 = this.trackedPlayers.iterator();

		while(var2.hasNext()) {
			EntityPlayerMP var3 = (EntityPlayerMP)var2.next();
			var3.playerNetServerHandler.sendPacket(var1);
		}

	}

	public void removeFromTrackedPlayers() {
		this.sendPacketToTrackedPlayers(new Packet29DestroyEntity(this.trackedEntity.entityID));
	}

	public void updatePlayerEntity(EntityPlayerMP var1) {
		if(var1 != this.trackedEntity) {
			double var2 = var1.posX - (double)(this.encodedPosX / 32);
			double var4 = var1.posZ - (double)(this.encodedPosZ / 32);
			if(var2 >= (double)(-this.trackingDistanceThreshold) && var2 <= (double)this.trackingDistanceThreshold && var4 >= (double)(-this.trackingDistanceThreshold) && var4 <= (double)this.trackingDistanceThreshold) {
				if(!this.trackedPlayers.contains(var1)) {
					this.trackedPlayers.add(var1);
					var1.playerNetServerHandler.sendPacket(this.getSpawnPacket());
				}
			} else if(this.trackedPlayers.contains(var1)) {
				this.trackedPlayers.remove(var1);
				var1.playerNetServerHandler.sendPacket(new Packet29DestroyEntity(this.trackedEntity.entityID));
			}

		}
	}

	public void updatePlayerEntities(List var1) {
		for(int var2 = 0; var2 < var1.size(); ++var2) {
			this.updatePlayerEntity((EntityPlayerMP)var1.get(var2));
		}

	}

	private Packet getSpawnPacket() {
		if(this.trackedEntity instanceof EntityItem) {
			EntityItem var3 = (EntityItem)this.trackedEntity;
			Packet21PickupSpawn var2 = new Packet21PickupSpawn(var3);
			var3.posX = (double)var2.xPosition / 32.0D;
			var3.posY = (double)var2.yPosition / 32.0D;
			var3.posZ = (double)var2.zPosition / 32.0D;
			var3.motionX = (double)var2.rotation / 128.0D;
			var3.motionY = (double)var2.pitch / 128.0D;
			var3.motionZ = (double)var2.roll / 128.0D;
			return var2;
		} else if(this.trackedEntity instanceof EntityPlayerMP) {
			return new Packet20NamedEntitySpawn((EntityPlayer)this.trackedEntity);
		} else {
			if(this.trackedEntity instanceof EntityMinecart) {
				EntityMinecart var1 = (EntityMinecart)this.trackedEntity;
				if(var1.minecartType == 0) {
					return new Packet23VehicleSpawn(this.trackedEntity, 10);
				}

				if(var1.minecartType == 1) {
					return new Packet23VehicleSpawn(this.trackedEntity, 11);
				}

				if(var1.minecartType == 2) {
					return new Packet23VehicleSpawn(this.trackedEntity, 12);
				}
			}

			if(this.trackedEntity instanceof EntityBoat) {
				return new Packet23VehicleSpawn(this.trackedEntity, 1);
			} else if(this.trackedEntity instanceof IAnimals) {
				return new Packet24MobSpawn((EntityLiving)this.trackedEntity);
			} else {
				throw new IllegalArgumentException("Don\'t know how to add " + this.trackedEntity.getClass() + "!");
			}
		}
	}
}
