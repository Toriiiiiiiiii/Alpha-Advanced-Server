package net.minecraft.src;

public interface IInventory {
	int getSizeInventory();

	ItemStack getStackInSlot(int var1);
}
