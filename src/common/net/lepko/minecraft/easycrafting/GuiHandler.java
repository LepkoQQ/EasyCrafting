package net.lepko.minecraft.easycrafting;

import net.lepko.minecraft.easycrafting.block.ContainerEasyCrafting;
import net.lepko.minecraft.easycrafting.block.GuiEasyCrafting;
import net.lepko.minecraft.easycrafting.block.TileEntityEasyCrafting;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 0) {
			TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
			if (tile_entity instanceof TileEntityEasyCrafting) {
				return new ContainerEasyCrafting((TileEntityEasyCrafting) tile_entity, player.inventory);
			}
		} else if (id == 1) {
			return null; // Client only
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 0) {
			TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
			if (tile_entity instanceof TileEntityEasyCrafting) {
				return new GuiEasyCrafting(player.inventory, (TileEntityEasyCrafting) tile_entity);
			}
		} else if (id == 1) {
			// return new GuiOptionsScreen();
		}
		return null;
	}
}