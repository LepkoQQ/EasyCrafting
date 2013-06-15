package net.lepko.easycrafting.inventory.gui;

import net.lepko.easycrafting.ModEasyCrafting;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GuiTabbed extends GuiContainer {

    public static final String GUI_TEXTURE = "/mods/" + VersionHelper.MOD_ID + "/textures/gui/easycraftinggui.png";

    public TabGroup tabGroup = new TabGroup(this);
    public int currentTab = 0;

    public GuiTabbed(Container container) {
        super(container);
        tabGroup.addTab(new Tab().icon(new ItemStack(ModEasyCrafting.blockEasyCraftingTable)).tooltip("Available"));
        tabGroup.addTab(new Tab().icon(new ItemStack(Item.compass)).tooltip("Search"));
        tabGroup.addTab(new Tab());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        tabGroup.drawBackground();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        tabGroup.drawForeground();

        Tab tab = tabGroup.getTabAt(mouseX, mouseY);
        if (tab != null) {
            drawCreativeTabHoveringText(tab.tooltip, mouseX - guiLeft, mouseY - guiTop);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (!tabGroup.mouseClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    public int guiLeft() {
        return this.guiLeft;
    }

    public int guiTop() {
        return this.guiTop;
    }

    public void drawRectangle(int x, int y, int texLeft, int texTop, int width, int height) {
        this.drawTexturedModalRect(x, y, texLeft, texTop, width, height);
    }

    public RenderItem itemRenderer() {
        return itemRenderer;
    }

    public Minecraft mc() {
        return mc;
    }
}
