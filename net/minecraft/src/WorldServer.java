package net.minecraft.src;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldServer extends World {
	public ChunkProviderServer chunkProviderServer;
	public boolean disableSpawnProtection = false;
	public boolean levelSaving;
	private boolean monsters;
	private SpawnerAnimals monsterSpawner = new SpawnerServer(this, 200, IMobs.class, new Class[]{EntityZombie.class, EntitySkeleton.class, EntityCreeper.class, EntitySpider.class, EntitySlime.class});
	private SpawnerAnimals animalSpawner = new SpawnerAnimals(15, EntityAnimal.class, new Class[]{EntitySheep.class, EntityPig.class, EntityCow.class, EntityChicken.class});

	public WorldServer(File var1, String var2, boolean var3) {
		super(var1, var2);
		this.monsters = var3;
	}

	public void tick() {
		super.tick();
		if(this.monsters) {
			this.monsterSpawner.onUpdate(this);
		}

		this.animalSpawner.onUpdate(this);
	}

	protected IChunkProvider getChunkProvider(File var1) {
		this.chunkProviderServer = new ChunkProviderServer(this, new ChunkLoader(var1, true), new ChunkProviderGenerate(this, this.randomSeed));
		return this.chunkProviderServer;
	}

	public List getTileEntityList(int var1, int var2, int var3, int var4, int var5, int var6) {
		ArrayList var7 = new ArrayList();

		for(int var8 = 0; var8 < this.loadedTileEntityList.size(); ++var8) {
			TileEntity var9 = (TileEntity)this.loadedTileEntityList.get(var8);
			if(var9.xCoord >= var1 && var9.yCoord >= var2 && var9.zCoord >= var3 && var9.xCoord < var4 && var9.yCoord < var5 && var9.zCoord < var6) {
				var7.add(var9);
			}
		}

		return var7;
	}
}
