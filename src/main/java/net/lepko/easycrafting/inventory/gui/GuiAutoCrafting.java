package net.lepko.easycrafting.inventory.gui;

import net.lepko.easycrafting.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.inventory.ContainerAutoCrafting;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiAutoCrafting extends GuiTabbed {

    public GuiAutoCrafting(InventoryPlayer playerInventory, TileEntityAutoCrafting tileInventory) {
        super(new ContainerAutoCrafting(playerInventory, tileInventory));
        ySize = 235;
    }

    @Override
    public void initTabs() {
    }
}
