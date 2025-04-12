package net.minecraft.src;

import java.util.Random;

public class BlockFurnace extends BlockContainer {
	private final boolean isActive;
	private Random random = new Random();

	protected BlockFurnace(int var1, boolean var2) {
		super(var1, Material.rock);
		this.isActive = var2;
		this.blockIndexInTexture = 45;
	}

	public int idDropped(int var1, Random var2) {
		return Block.stoneOvenIdle.blockID;
	}

	public void onBlockAdded(World var1, int var2, int var3, int var4) {
		super.onBlockAdded(var1, var2, var3, var4);
		this.setDefaultDirection(var1, var2, var3, var4);
	}
	
//	public void onBlockPlaced(World world, int x, int y, int z, int var5) {
//		if(!world.multiplayerWorld) {
//			EntityPlayer plr = (EntityPlayer)world.playerEntities.get(var5);
//			double playerAngle = Math.abs(plr.rotationYaw % 360);
//			
//			byte direction = 0;
//			if(playerAngle > 45 && playerAngle <= 135) {
//				direction = 4;
//			} if(playerAngle > 135 && playerAngle <= 225) {
//				direction = 3;
//			} if(playerAngle > 225 && playerAngle <= 315) {
//				direction = 5;
//			} if(playerAngle > 315 || playerAngle < 45) {
//				direction = 2;
//			}
//			
//			world.setBlockMetadataWithNotify(x, y, z, direction);
//		}
//	}

	private void setDefaultDirection(World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockId(var2, var3, var4 - 1);
		int var6 = var1.getBlockId(var2, var3, var4 + 1);
		int var7 = var1.getBlockId(var2 - 1, var3, var4);
		int var8 = var1.getBlockId(var2 + 1, var3, var4);
		byte var9 = 3;
		if(Block.opaqueCubeLookup[var5] && !Block.opaqueCubeLookup[var6]) {
			var9 = 3;
		}

		if(Block.opaqueCubeLookup[var6] && !Block.opaqueCubeLookup[var5]) {
			var9 = 2;
		}

		if(Block.opaqueCubeLookup[var7] && !Block.opaqueCubeLookup[var8]) {
			var9 = 5;
		}

		if(Block.opaqueCubeLookup[var8] && !Block.opaqueCubeLookup[var7]) {
			var9 = 4;
		}

		var1.setBlockMetadataWithNotify(var2, var3, var4, var9);
	}

	public int getBlockTextureFromSide(int var1) {
		return var1 == 1 ? Block.stone.blockID : (var1 == 0 ? Block.stone.blockID : (var1 == 3 ? this.blockIndexInTexture - 1 : this.blockIndexInTexture));
	}

	public boolean blockActivated(World var1, int var2, int var3, int var4, EntityPlayer var5) {
		TileEntityFurnace var6 = (TileEntityFurnace)var1.getBlockTileEntity(var2, var3, var4);
		var5.displayGUIFurnace(var6);
		return true;
	}

	public static void updateFurnaceBlockState(boolean var0, World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMetadata(var2, var3, var4);
		TileEntity var6 = var1.getBlockTileEntity(var2, var3, var4);
		if(var0) {
			var1.setBlockWithNotify(var2, var3, var4, Block.stoneOvenActive.blockID);
		} else {
			var1.setBlockWithNotify(var2, var3, var4, Block.stoneOvenIdle.blockID);
		}

		var1.setBlockMetadataWithNotify(var2, var3, var4, var5);
		var1.setBlockTileEntity(var2, var3, var4, var6);
	}

	protected TileEntity getBlockEntity() {
		return new TileEntityFurnace();
	}
	
	public void onBlockRemoval(World world, int x, int y, int z) {
		if(world.getBlockId(x, y, z) == 0) {
			TileEntityFurnace var5 = (TileEntityFurnace)world.getBlockTileEntity(x, y, z);
		
			for(int var6 = 0; var6 < 3; ++var6) {
				ItemStack var7 = var5.getStackInSlot(var6);
				if(var7 != null) {
					float var8 = this.random.nextFloat() * 0.8F + 0.1F;
					float var9 = this.random.nextFloat() * 0.8F + 0.1F;
					float var10 = this.random.nextFloat() * 0.8F + 0.1F;

					while(var7.stackSize > 0) {
						int var11 = this.random.nextInt(21) + 10;
						if(var11 > var7.stackSize) {
							var11 = var7.stackSize;
						}

						var7.stackSize -= var11;
						EntityItem var12 = new EntityItem(world, (double)((float)x + var8), (double)((float)y + var9), (double)((float)z + var10), new ItemStack(var7.itemID, var11, var7.itemDmg));
						float var13 = 0.05F;
						var12.motionX = (double)((float)this.random.nextGaussian() * var13);
						var12.motionY = (double)((float)this.random.nextGaussian() * var13 + 0.2F);
						var12.motionZ = (double)((float)this.random.nextGaussian() * var13);
						world.spawnEntityInWorld(var12);
					}
				}
			}
		}
		
		super.onBlockRemoval(world, x, y, z);
	}
}
