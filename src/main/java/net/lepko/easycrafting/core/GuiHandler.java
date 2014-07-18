package net.lepko.easycrafting.core;

import net.lepko.easycrafting.core.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.core.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.core.inventory.ContainerAutoCrafting;
import net.lepko.easycrafting.core.inventory.ContainerEasyCrafting;
import net.lepko.easycrafting.core.inventory.gui.GuiAutoCrafting;
import net.lepko.easycrafting.core.inventory.gui.GuiEasyCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public enum GuiHandler implements IGuiHandler {
    INSTANCE;
public final static int guiEasyCrafting = 1;
public final static int guiAutoCrafting = 2;
   
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
    	switch (id) {

    	case GuiHandler.guiEasyCrafting:
    	if (!(te instanceof TileEntityEasyCrafting)) {
    	return null;
    	} else {
    	return new ContainerEasyCrafting(player.inventory, (TileEntityEasyCrafting) te);
    	}

    	case GuiHandler.guiAutoCrafting:
        	if (!(te instanceof TileEntityAutoCrafting)) {
        	return null;
        	} else {
        	return new ContainerAutoCrafting(player.inventory, (TileEntityAutoCrafting) te);
        	}
    	default:
    		return null;
    	}
      }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
    	switch (id) {

    	case GuiHandler.guiEasyCrafting:
    	if (!(te instanceof TileEntityEasyCrafting)) {
    	return null;
    	} else {
    	return new GuiEasyCrafting(player.inventory, (TileEntityEasyCrafting) te);
    	}

    	case GuiHandler.guiAutoCrafting:
    	if (!(te instanceof TileEntityAutoCrafting)) {
    	return null;
    	} else {
    	return new GuiAutoCrafting(player.inventory, (TileEntityAutoCrafting) te);
    	}
    	default:
    		return null;
    	}
    	}
        		
       }