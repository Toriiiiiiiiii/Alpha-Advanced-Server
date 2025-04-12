package net.minecraft.src;

public class ItemInWorldManager {
	private World worldObj;
	public EntityPlayer thisPlayer;
	private float removeProgressUnused;
	private float removeProgress = 0.0F;
	private int curBlockDurability = 0;
	private float curblockDamage = 0.0F;
	private int posX;
	private int posY;
	private int posZ;

	public ItemInWorldManager(World var1) {
		this.worldObj = var1;
	}

	public void onBlockClicked(int var1, int var2, int var3) {
		int var4 = this.worldObj.getBlockId(var1, var2, var3);
		if(var4 > 0 && this.removeProgress == 0.0F) {
			Block.blocksList[var4].onBlockClicked(this.worldObj, var1, var2, var3, this.thisPlayer);
		}

		if(var4 > 0 && Block.blocksList[var4].blockStrength(this.thisPlayer) >= 1.0F) {
			this.tryHarvestBlock(var1, var2, var3);
		}

	}

	public void blockRemoving() {
		this.removeProgress = 0.0F;
		this.curBlockDurability = 0;
	}

	public void updateBlockRemoving(int var1, int var2, int var3, int var4) {
		if(this.curBlockDurability > 0) {
			--this.curBlockDurability;
		} else {
			if(var1 == this.posX && var2 == this.posY && var3 == this.posZ) {
				int var5 = this.worldObj.getBlockId(var1, var2, var3);
				if(var5 == 0) {
					return;
				}

				Block var6 = Block.blocksList[var5];
				this.removeProgress += var6.blockStrength(this.thisPlayer);
				++this.curblockDamage;
				if(this.removeProgress >= 1.0F) {
					this.tryHarvestBlock(var1, var2, var3);
					this.removeProgress = 0.0F;
					this.removeProgressUnused = 0.0F;
					this.curblockDamage = 0.0F;
					this.curBlockDurability = 5;
				}
			} else {
				this.removeProgress = 0.0F;
				this.removeProgressUnused = 0.0F;
				this.curblockDamage = 0.0F;
				this.posX = var1;
				this.posY = var2;
				this.posZ = var3;
			}

		}
	}

	public boolean removeBlock(int var1, int var2, int var3) {
		Block var4 = Block.blocksList[this.worldObj.getBlockId(var1, var2, var3)];
		int var5 = this.worldObj.getBlockMetadata(var1, var2, var3);
		boolean var6 = this.worldObj.setBlockWithNotify(var1, var2, var3, 0);
		if(var4 != null && var6) {
			var4.onBlockDestroyedByPlayer(this.worldObj, var1, var2, var3, var5);
		}

		return var6;
	}

	public boolean tryHarvestBlock(int var1, int var2, int var3) {
		int var4 = this.worldObj.getBlockId(var1, var2, var3);
		int var5 = this.worldObj.getBlockMetadata(var1, var2, var3);
		boolean var6 = this.removeBlock(var1, var2, var3);
		ItemStack var7 = this.thisPlayer.getCurrentEquippedItem();
		if(var7 != null) {
			var7.onDestroyBlock(var4, var1, var2, var3);
			if(var7.stackSize == 0) {
				var7.onItemDestroyedByUse(this.thisPlayer);
				this.thisPlayer.destroyCurrentEquippedItem();
			}
		}

		if(var6 && this.thisPlayer.canHarvestBlock(Block.blocksList[var4])) {
			Block.blocksList[var4].dropBlockAsItem(this.worldObj, var1, var2, var3, var5);
		}

		return var6;
	}

	public boolean activeBlockOrUseItem(EntityPlayer var1, World var2, ItemStack var3, int var4, int var5, int var6, int var7) {
		int var8 = var2.getBlockId(var4, var5, var6);
		return var8 > 0 && Block.blocksList[var8].blockActivated(var2, var4, var5, var6, var1) ? true : (var3 == null ? false : var3.useItem(var1, var2, var4, var5, var6, var7));
	}
}
