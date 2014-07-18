package net.lepko.easycrafting.core;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.lepko.easycrafting.EasyCrafting;
import net.lepko.easycrafting.core.inventory.gui.IGuiTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public enum GuiHandler implements IGuiHandler {
    INSTANCE;

    public static void openGui(EntityPlayer player, World world, int x, int y, int z) {
        FMLNetworkHandler.openGui(player, EasyCrafting.INSTANCE, 0, world, x, y, z);
    }

    @Override
    public Object getServerGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof IGuiTile) {
            return ((IGuiTile) te).getServerGuiElement(player, te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof IGuiTile) {
            return ((IGuiTile) te).getClientGuiElement(player, te);
        }
        return null;
    }
}