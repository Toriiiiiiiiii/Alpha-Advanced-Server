package net.minecraft.src;

public class ItemFood extends Item {
	private int healAmount;

	public ItemFood(int var1, int var2) {
		super(var1);
		this.healAmount = var2;
		this.maxStackSize = 1;
	}
}
