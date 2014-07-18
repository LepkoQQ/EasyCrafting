package net.lepko.easycrafting.core.inventory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public interface IGuiTile {
    Object getServerGuiElement(EntityPlayer player, TileEntity tileEntity);
    Object getClientGuiElement(EntityPlayer player, TileEntity tileEntity);
}
