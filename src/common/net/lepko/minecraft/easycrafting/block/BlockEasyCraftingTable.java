package net.lepko.minecraft.easycrafting;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This is the block part of the easycraft table.
 */
public class BlockEasyCraftingTable extends BlockContainer {

	/**
	 * Creates an instance of this class.
	 *
	 * @param  blockID	The ID to be used for the easycraft table block.
	 * @return N/A
	 */
	public BlockEasyCraftingTable(int blockID) {
		super(blockID, 0, Material.wood);
		this.setHardness(2.5F);
		this.setStepSound(soundWoodFootstep);
		this.setBlockName("easycraftingtable");
		this.setCreativeTab(CreativeTabs.tabDecorations);
	}

	/**
	 * Gets the texture index for the given side.
	 *
	 * @param  side		The side that you want the texture of.
	 * @return 			The texture index for the given side.
	 */
	@Override
	public int getBlockTextureFromSide(int side) {
		switch (side) {
		case 0:
			return 3;
		case 1:
			return 0;
		case 2:
		case 4:
			return 1;
		case 3:
		case 5:
			return 2;
		default:
			return 3;
		}
	}

	/**
	 * Returns the filepath of the texture file.
	 *
	 * @param  N/A
	 * @return 		The filepath of the texture file in string format.
	 */
	@Override
	public String getTextureFile() {
		return ProxyCommon.blocksTextureFile;
	}

	/**
	 * Activates the block, opening the gui.
	 *
	 * @param  world	The current world.
	 * @param  x		The x position of the block.
	 * @param  y		The y position of the block.
	 * @param  z		The z position of the block.
	 * @param  player	The player activating the block.
	 * @param  i		Unused; Parent requirement.
	 * @param  f		Unused; Parent requirement.
	 * @param  g		Unused; Parent requirement.
	 * @param  t		Unused; Parent requirement.
	 * @return 			True if the player is can activate the block, false if not
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float f, float g, float t) {
		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);

		if (tile_entity == null || player.isSneaking()) {
			return false;
		}

		player.openGui(ModEasyCrafting.instance, 0, world, x, y, z);
		return true;
	}

	/**
	 * Handles the breaking of the block.
	 *
	 * @param  world	The current world.
	 * @param  x		The x position of the block.
	 * @param  y		The y position of the block.
	 * @param  z		The z position of the block.
	 * @param  i		Unused; Parent requirement.
	 * @param  j		Unused; Parent requirement.
	 * @return N/A
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int i, int j) {
		dropItems(world, x, y, z);
		super.breakBlock(world, x, y, z, i, j);
	}

	/**
	 * Handles the dropping of the blocks inventory contents.
	 *
	 * @param  world	The current world.
	 * @param  x		The x position of the block.
	 * @param  y		The y position of the block.
	 * @param  z		The z position of the block.
	 * @return N/A
	 */
	private void dropItems(World world, int x, int y, int z) {
		Random rand = new Random();

		TileEntityEasyCrafting var7 = (TileEntityEasyCrafting) world.getBlockTileEntity(x, y, z);

		if (var7 != null) {
			for (int var8 = 0; var8 < var7.getSizeInventory(); ++var8) {
				ItemStack var9 = var7.getStackInSlot(var8);

				if (var9 != null) {
					float var10 = rand.nextFloat() * 0.8F + 0.1F;
					float var11 = rand.nextFloat() * 0.8F + 0.1F;
					EntityItem var14;

					for (float var12 = rand.nextFloat() * 0.8F + 0.1F; var9.stackSize > 0; world.spawnEntityInWorld(var14)) {
						int var13 = rand.nextInt(21) + 10;

						if (var13 > var9.stackSize) {
							var13 = var9.stackSize;
						}

						var9.stackSize -= var13;
						var14 = new EntityItem(world, (double) ((float) x + var10), (double) ((float) y + var11), (double) ((float) z + var12), new ItemStack(var9.itemID, var13, var9.getItemDamage()));
						float var15 = 0.05F;
						var14.motionX = (double) ((float) rand.nextGaussian() * var15);
						var14.motionY = (double) ((float) rand.nextGaussian() * var15 + 0.2F);
						var14.motionZ = (double) ((float) rand.nextGaussian() * var15);

						if (var9.hasTagCompound()) {
							var14.item.setTagCompound((NBTTagCompound) var9.getTagCompound().copy());
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a new tile entity of an easycraft table.
	 *
	 * @param  world	The current world.
	 * @return 			The newly created tile entity.
	 */
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityEasyCrafting();
	}
}