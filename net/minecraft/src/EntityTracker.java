package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.server.MinecraftServer;

public class EntityTracker {
	private Set trackedEntitySet = new HashSet();
	private MCHashTable trackedEntityHashTable = new MCHashTable();
	private MinecraftServer mcServer;
	private int maxTrackingDistanceThreshold;

	public EntityTracker(MinecraftServer var1) {
		this.mcServer = var1;
		this.maxTrackingDistanceThreshold = var1.configManager.getMaxTrackingDistance();
	}

	public void trackEntity(Entity var1) {
		if(var1 instanceof EntityPlayerMP) {
			this.trackEntity(var1, 512, 2);
			EntityPlayerMP var2 = (EntityPlayerMP)var1;
			Iterator var3 = this.trackedEntitySet.iterator();

			while(var3.hasNext()) {
				EntityTrackerEntry var4 = (EntityTrackerEntry)var3.next();
				if(var4.trackedEntity != var2) {
					var4.updatePlayerEntity(var2);
				}
			}
		} else if(var1 instanceof EntityItem) {
			this.trackEntity(var1, 64, 20);
		} else if(var1 instanceof EntityMinecart) {
			this.trackEntity(var1, 160, 4);
		} else if(var1 instanceof IAnimals) {
			this.trackEntity(var1, 160, 2);
		}

	}

	public void trackEntity(Entity var1, int var2, int var3) {
		if(var2 > this.maxTrackingDistanceThreshold) {
			var2 = this.maxTrackingDistanceThreshold;
		}

		if(this.trackedEntityHashTable.containsItem(var1.entityID)) {
			throw new IllegalStateException("Entity is already tracked!");
		} else {
			EntityTrackerEntry var4 = new EntityTrackerEntry(var1, var2, var3);
			this.trackedEntitySet.add(var4);
			this.trackedEntityHashTable.addKey(var1.entityID, var4);
			var4.updatePlayerEntities(this.mcServer.worldMngr.playerEntities);
		}
	}

	public void untrackEntity(Entity var1) {
		EntityTrackerEntry var2 = (EntityTrackerEntry)this.trackedEntityHashTable.removeObject(var1.entityID);
		if(var2 != null) {
			this.trackedEntitySet.remove(var2);
			var2.removeFromTrackedPlayers();
		}

	}

	public void updateTrackedEntities() {
		ArrayList var1 = new ArrayList();
		Iterator var2 = this.trackedEntitySet.iterator();

		while(var2.hasNext()) {
			EntityTrackerEntry var3 = (EntityTrackerEntry)var2.next();
			var3.updatePlayerList(this.mcServer.worldMngr.playerEntities);
			if(var3.playerEntitiesUpdated && var3.trackedEntity instanceof EntityPlayerMP) {
				var1.add((EntityPlayerMP)var3.trackedEntity);
			}
		}

		for(int var6 = 0; var6 < var1.size(); ++var6) {
			EntityPlayerMP var7 = (EntityPlayerMP)var1.get(var6);
			Iterator var4 = this.trackedEntitySet.iterator();

			while(var4.hasNext()) {
				EntityTrackerEntry var5 = (EntityTrackerEntry)var4.next();
				if(var5.trackedEntity != var7) {
					var5.updatePlayerEntity(var7);
				}
			}
		}

	}

	public void sendPacketToTrackedPlayers(Entity var1, Packet var2) {
		EntityTrackerEntry var3 = (EntityTrackerEntry)this.trackedEntityHashTable.lookup(var1.entityID);
		if(var3 != null) {
			var3.sendPacketToTrackedPlayers(var2);
		}

	}
}
