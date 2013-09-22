package net.lepko.easycrafting.inventory.gui;

import net.lepko.easycrafting.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.core.VersionHelper;
import net.lepko.easycrafting.inventory.ContainerAutoCrafting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiAutoCrafting extends GuiContainer {

    
    
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(VersionHelper.MOD_ID, "textures/gui/autocraftinggui.png");

    public GuiAutoCrafting(InventoryPlayer playerInventory, TileEntityAutoCrafting tileInventory) {
        super(new ContainerAutoCrafting(playerInventory, tileInventory));
        ySize = 207;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        // Background
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
