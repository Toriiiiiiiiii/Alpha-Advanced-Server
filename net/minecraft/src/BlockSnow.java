package net.minecraft.src;

import java.util.Random;

public class BlockSnow extends Block {
	protected BlockSnow(int var1, int var2) {
		super(var1, var2, Material.snow);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F / 16.0F, 1.0F);
		this.setTickOnLoad(true);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World var1, int var2, int var3, int var4) {
		return null;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean canPlaceBlockAt(World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockId(var2, var3 - 1, var4);
		return var5 != 0 && Block.blocksList[var5].isOpaqueCube() ? var1.getBlockMaterial(var2, var3 - 1, var4).getIsSolid() : false;
	}

	public void onNeighborBlockChange(World var1, int var2, int var3, int var4, int var5) {
		this.canSnowStay(var1, var2, var3, var4);
	}

	private boolean canSnowStay(World var1, int var2, int var3, int var4) {
		if(!this.canPlaceBlockAt(var1, var2, var3, var4)) {
			this.dropBlockAsItem(var1, var2, var3, var4, var1.getBlockMetadata(var2, var3, var4));
			var1.setBlockWithNotify(var2, var3, var4, 0);
			return false;
		} else {
			return true;
		}
	}

	public int idDropped(int var1, Random var2) {
		return Item.snowball.shiftedIndex;
	}

	public int quantityDropped(Random var1) {
		return 0;
	}

	public void updateTick(World var1, int var2, int var3, int var4, Random var5) {
		if(var1.getSavedLightValue(EnumSkyBlock.Block, var2, var3, var4) > 11) {
			this.dropBlockAsItem(var1, var2, var3, var4, var1.getBlockMetadata(var2, var3, var4));
			var1.setBlockWithNotify(var2, var3, var4, 0);
		}

	}

	public boolean shouldSideBeRendered(IBlockAccess var1, int var2, int var3, int var4, int var5) {
		Material var6 = var1.getBlockMaterial(var2, var3, var4);
		return var5 == 1 ? true : (var6 == this.material ? false : super.shouldSideBeRendered(var1, var2, var3, var4, var5));
	}
}
