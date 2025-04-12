package net.minecraft.src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChunkProviderServer implements IChunkProvider {
	private Set droppedChunksSet = new HashSet();
	private Chunk chunk;
	private IChunkProvider serverChunkProvider;
	private IChunkLoader serverChunkLoader;
	private Map id2ChunkMap = new HashMap();
	private List loadedChunks = new ArrayList();
	private WorldServer worldObj;

	public ChunkProviderServer(WorldServer var1, IChunkLoader var2, IChunkProvider var3) {
		this.chunk = new Chunk(var1, new byte[-Short.MIN_VALUE], 0, 0);
		this.chunk.isChunkRendered = true;
		this.chunk.neverSave = true;
		this.worldObj = var1;
		this.serverChunkLoader = var2;
		this.serverChunkProvider = var3;
	}

	public boolean chunkExists(int var1, int var2) {
		ChunkCoordinates var3 = new ChunkCoordinates(var1, var2);
		return this.id2ChunkMap.containsKey(var3);
	}

	public void dropChunk(int var1, int var2) {
		int var3 = var1 * 16 + 8 - this.worldObj.spawnX;
		int var4 = var2 * 16 + 8 - this.worldObj.spawnZ;
		byte var5 = 20;
		if(var3 < -var5 || var3 > var5 || var4 < -var5 || var4 > var5) {
			this.droppedChunksSet.add(new ChunkCoordinates(var1, var2));
		}

	}

	public Chunk loadChunk(int var1, int var2) {
		ChunkCoordinates var3 = new ChunkCoordinates(var1, var2);
		this.droppedChunksSet.remove(new ChunkCoordinates(var1, var2));
		Chunk var4 = (Chunk)this.id2ChunkMap.get(var3);
		if(var4 == null) {
			var4 = this.loadAndSaveChunk(var1, var2);
			if(var4 == null) {
				if(this.serverChunkProvider == null) {
					var4 = this.chunk;
				} else {
					var4 = this.serverChunkProvider.provideChunk(var1, var2);
				}
			}

			this.id2ChunkMap.put(var3, var4);
			this.loadedChunks.add(var4);
			if(var4 != null) {
				var4.onChunkLoad();
			}

			if(!var4.isTerrainPopulated && this.chunkExists(var1 + 1, var2 + 1) && this.chunkExists(var1, var2 + 1) && this.chunkExists(var1 + 1, var2)) {
				this.populate(this, var1, var2);
			}

			if(this.chunkExists(var1 - 1, var2) && !this.provideChunk(var1 - 1, var2).isTerrainPopulated && this.chunkExists(var1 - 1, var2 + 1) && this.chunkExists(var1, var2 + 1) && this.chunkExists(var1 - 1, var2)) {
				this.populate(this, var1 - 1, var2);
			}

			if(this.chunkExists(var1, var2 - 1) && !this.provideChunk(var1, var2 - 1).isTerrainPopulated && this.chunkExists(var1 + 1, var2 - 1) && this.chunkExists(var1, var2 - 1) && this.chunkExists(var1 + 1, var2)) {
				this.populate(this, var1, var2 - 1);
			}

			if(this.chunkExists(var1 - 1, var2 - 1) && !this.provideChunk(var1 - 1, var2 - 1).isTerrainPopulated && this.chunkExists(var1 - 1, var2 - 1) && this.chunkExists(var1, var2 - 1) && this.chunkExists(var1 - 1, var2)) {
				this.populate(this, var1 - 1, var2 - 1);
			}
		}

		return var4;
	}

	public Chunk provideChunk(int var1, int var2) {
		ChunkCoordinates var3 = new ChunkCoordinates(var1, var2);
		Chunk var4 = (Chunk)this.id2ChunkMap.get(var3);
		return var4 == null ? (this.worldObj.worldChunkLoadOverride ? this.loadChunk(var1, var2) : this.chunk) : var4;
	}

	private Chunk loadAndSaveChunk(int var1, int var2) {
		if(this.serverChunkLoader == null) {
			return null;
		} else {
			try {
				Chunk var3 = this.serverChunkLoader.loadChunk(this.worldObj, var1, var2);
				if(var3 != null) {
					var3.lastSaveTime = this.worldObj.worldTime;
				}

				return var3;
			} catch (Exception var4) {
				var4.printStackTrace();
				return null;
			}
		}
	}

	private void saveExtraChunkData(Chunk var1) {
		if(this.serverChunkLoader != null) {
			try {
				this.serverChunkLoader.saveExtraChunkData(this.worldObj, var1);
			} catch (Exception var3) {
				var3.printStackTrace();
			}

		}
	}

	private void saveChunk(Chunk var1) {
		if(this.serverChunkLoader != null) {
			try {
				var1.lastSaveTime = this.worldObj.worldTime;
				this.serverChunkLoader.saveChunk(this.worldObj, var1);
			} catch (IOException var3) {
				var3.printStackTrace();
			}

		}
	}

	public void populate(IChunkProvider var1, int var2, int var3) {
		Chunk var4 = this.provideChunk(var2, var3);
		if(!var4.isTerrainPopulated) {
			var4.isTerrainPopulated = true;
			if(this.serverChunkProvider != null) {
				this.serverChunkProvider.populate(var1, var2, var3);
				var4.setChunkModified();
			}
		}

	}

	public boolean saveChunks(boolean var1, IProgressUpdate var2) {
		int var3 = 0;

		for(int var4 = 0; var4 < this.loadedChunks.size(); ++var4) {
			Chunk var5 = (Chunk)this.loadedChunks.get(var4);
			if(var1 && !var5.neverSave) {
				this.saveExtraChunkData(var5);
			}

			if(var5.needsSaving(var1)) {
				this.saveChunk(var5);
				var5.isModified = false;
				++var3;
				if(var3 == 2 && !var1) {
					return false;
				}
			}
		}

		if(var1) {
			if(this.serverChunkLoader == null) {
				return true;
			}

			this.serverChunkLoader.saveExtraData();
		}

		return true;
	}

	public boolean unload100OldestChunks() {
		if(!this.worldObj.levelSaving) {
			for(int var1 = 0; var1 < 16; ++var1) {
				if(!this.droppedChunksSet.isEmpty()) {
					ChunkCoordinates var2 = (ChunkCoordinates)this.droppedChunksSet.iterator().next();
					Chunk var3 = this.provideChunk(var2.posX, var2.posZ);
					var3.onChunkUnload();
					this.saveChunk(var3);
					this.saveExtraChunkData(var3);
					this.droppedChunksSet.remove(var2);
					this.id2ChunkMap.remove(var2);
					this.loadedChunks.remove(var3);
				}
			}

			if(this.serverChunkLoader != null) {
				this.serverChunkLoader.chunkTick();
			}
		}

		return this.serverChunkProvider.unload100OldestChunks();
	}

	public boolean canSave() {
		return !this.worldObj.levelSaving;
	}
}
