package net.lepko.easycrafting.handlers;

import net.lepko.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.inventory.ContainerEasyCrafting;
import net.lepko.easycrafting.inventory.gui.GuiEasyCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == 0) {
            TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
            if (tile_entity instanceof TileEntityEasyCrafting) {
                return new ContainerEasyCrafting((TileEntityEasyCrafting) tile_entity, player.inventory);
            }
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
        }
        return null;
    }
}