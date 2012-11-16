package net.lepko.minecraft.easycrafting;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * Creates the elements and containers, needed client and serverside, for the gui.
 */
public class GuiHandler implements IGuiHandler {

	/**
	 * Creates the container needed for the gui on the serverside.
	 *
	 * @param  id		Unused; Parent requirement.
	 * @param  player	The player from which to fetch the inventory.
	 * @param  world	The world within to fetch the tile.
	 * @param  x		The x coordinate in the world of the tile.
	 * @param  y		The y coordinate in the world of the tile.
	 * @param  z		The z coordinate in the world of the tile.
	 * @return 			The container for the serverside of the gui, if an easycraft table tile. Null if not.
	 */
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
		if (tile_entity instanceof TileEntityEasyCrafting) {
			return new ContainerEasyCrafting((TileEntityEasyCrafting) tile_entity, player.inventory);
		}
		return null;
	}

	/**
	 * Creates the gui needed for the clientside.
	 *
	 * @param  id		Unused; Parent requirement.
	 * @param  player	The player from which to fetch the inventory.
	 * @param  world	The world within to fetch the tile.
	 * @param  x		The x coordinate in the world of the tile.
	 * @param  y		The y coordinate in the world of the tile.
	 * @param  z		The z coordinate in the world of the tile.
	 * @return 			The gui for the client, if an easycraft table tile. Null if not.
	 */
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
		if (tile_entity instanceof TileEntityEasyCrafting) {
			return new GuiEasyCrafting(player.inventory, (TileEntityEasyCrafting) tile_entity);
		}
		return null;
	}
}