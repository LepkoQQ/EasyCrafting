package net.lepko.easycrafting.core.inventory.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;

public abstract class GuiTabbed extends GuiContainer {

    public TabGroup tabGroup = new TabGroup(this);
    public int currentTab = 0;

    public GuiTabbed(Container container) {
        super(container);
    }

    @Override
    public void initGui() {
        super.initGui();
        initTabs();
        tabGroup.getTab(currentTab).onTabSelected();
    }

    public abstract void initTabs();

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

        RenderHelper.enableGUIStandardItemLighting();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (!tabGroup.mouseClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    // public accessors
    public int guiLeft() {
        return guiLeft;
    }

    public int guiTop() {
        return guiTop;
    }

    public void drawRectangle(int x, int y, int texLeft, int texTop, int width, int height) {
        drawTexturedModalRect(x, y, texLeft, texTop, width, height);
    }

    public RenderItem itemRenderer() {
        return itemRender;
    }

    public Minecraft mc() {
        return mc;
    }
}
