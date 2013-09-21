package net.lepko.easycrafting.core;

import net.lepko.easycrafting.ModEasyCrafting;
import net.lepko.easycrafting.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.inventory.ContainerAutoCrafting;
import net.lepko.easycrafting.inventory.ContainerEasyCrafting;
import net.lepko.easycrafting.inventory.gui.GuiAutoCrafting;
import net.lepko.easycrafting.inventory.gui.GuiEasyCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static int GUI_EASYCRAFTING = 0;
    public static int GUI_AUTOCRAFTING = 1;

    public static void openGui(int guiID, EntityPlayer player, World world, int x, int y, int z) {
        player.openGui(ModEasyCrafting.instance, guiID, world, x, y, z);
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
        if (id == GUI_EASYCRAFTING) {
            if (tile_entity instanceof TileEntityEasyCrafting) {
                return new ContainerEasyCrafting(player.inventory, (TileEntityEasyCrafting) tile_entity);
            }
        } else if (id == GUI_AUTOCRAFTING) {
            if (tile_entity instanceof TileEntityAutoCrafting) {
                return new ContainerAutoCrafting(player.inventory, (TileEntityAutoCrafting) tile_entity);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
        if (id == GUI_EASYCRAFTING) {
            if (tile_entity instanceof TileEntityEasyCrafting) {
                return new GuiEasyCrafting(player.inventory, (TileEntityEasyCrafting) tile_entity);
            }
        } else if (id == GUI_AUTOCRAFTING) {
            if (tile_entity instanceof TileEntityAutoCrafting) {
                return new GuiAutoCrafting(player.inventory, (TileEntityAutoCrafting) tile_entity);
            }
        }
        return null;
    }
}