package net.minecraft.src;

public class ItemBucket extends Item {
	private int isFull;

	public ItemBucket(int var1, int var2) {
		super(var1);
		this.maxStackSize = 1;
		this.maxDamage = 64;
		this.isFull = var2;
	}
}
