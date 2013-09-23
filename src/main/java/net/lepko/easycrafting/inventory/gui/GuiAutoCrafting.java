package net.lepko.easycrafting.inventory.gui;

import net.lepko.easycrafting.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.core.VersionHelper;
import net.lepko.easycrafting.inventory.ContainerAutoCrafting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiAutoCrafting extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(VersionHelper.MOD_ID, "textures/gui/autocraftinggui.png");

    private final TileEntityAutoCrafting tileEntity;
    private ButtonMode button;

    public GuiAutoCrafting(InventoryPlayer playerInventory, TileEntityAutoCrafting tileInventory) {
        super(new ContainerAutoCrafting(playerInventory, tileInventory));
        this.tileEntity = tileInventory;
        ySize = 207;
    }

    @Override
    public void initGui() {
        super.initGui();

        button = new ButtonMode(this, guiLeft + xSize - 20, guiTop + 4, 16, 16, 256 - 16, 16);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float tickTime, int mouseX, int mouseY) {
        // Background
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Button
        button.render(mouseX, mouseY, tileEntity.mode);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRenderer.drawString(I18n.getString(tileEntity.getInvName()), 8, 6, 0x404040);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tickTime) {
        super.drawScreen(mouseX, mouseY, tickTime);

        button.renderTooltip(mouseX, mouseY, tileEntity.mode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // on mouse down

        button.click(mouseX, mouseY, mouseButton, tileEntity);
    }

    // public accessors
    public void drawHoverText(String text, int mouseX, int mouseY) {
        drawCreativeTabHoveringText(text, mouseX, mouseY);
    }
}
