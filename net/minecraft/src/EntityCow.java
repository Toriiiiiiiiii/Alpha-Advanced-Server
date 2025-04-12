package net.minecraft.src;

public class EntityCow extends EntityAnimal {
	public boolean unusedBool2 = false;

	public EntityCow(World var1) {
		super(var1);
		this.texture = "/mob/cow.png";
		this.setSize(0.9F, 1.3F);
	}

	public void writeEntityToNBT(NBTTagCompound var1) {
		super.writeEntityToNBT(var1);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
	}

	protected String getLivingSound() {
		return "mob.cow";
	}

	protected String getHurtSound() {
		return "mob.cowhurt";
	}

	protected String getDeathSound() {
		return "mob.cowhurt";
	}

	protected float getSoundVolume() {
		return 0.4F;
	}

	protected int getDropItemId() {
		return Item.leather.shiftedIndex;
	}
}
