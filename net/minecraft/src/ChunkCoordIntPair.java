package net.minecraft.src;

public class ChunkCoordIntPair {
	public int chunkXPos;
	public int chunkZPos;

	public ChunkCoordIntPair(int var1, int var2) {
		this.chunkXPos = var1;
		this.chunkZPos = var2;
	}

	public int hashCode() {
		return this.chunkXPos << 8 | this.chunkZPos;
	}

	public boolean equals(Object var1) {
		ChunkCoordIntPair var2 = (ChunkCoordIntPair)var1;
		return var2.chunkXPos == this.chunkXPos && var2.chunkZPos == this.chunkZPos;
	}

	public double a(Entity var1) {
		double var2 = (double)(this.chunkXPos * 16 + 8);
		double var4 = (double)(this.chunkZPos * 16 + 8);
		double var6 = var2 - var1.posX;
		double var8 = var4 - var1.posZ;
		return var6 * var6 + var8 * var8;
	}
}
