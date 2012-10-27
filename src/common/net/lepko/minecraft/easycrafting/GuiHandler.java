package net.lepko.minecraft.easycrafting;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
		if (tile_entity instanceof TileEntityEasyCrafting) {
			return new ContainerEasyCrafting((TileEntityEasyCrafting) tile_entity, player.inventory);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
		if (tile_entity instanceof TileEntityEasyCrafting) {
			return new GuiEasyCrafting(player.inventory, (TileEntityEasyCrafting) tile_entity);
		}
		return null;
	}
}