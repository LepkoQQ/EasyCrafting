package net.lepko.easycrafting.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class TabGroup {

    public final GuiTabbed gui;
    public final boolean leftSide;
    private final List<Tab> tabs = new ArrayList<Tab>();

    public TabGroup(GuiTabbed gui) {
        this.gui = gui;
        this.leftSide = true;
    }

    public TabGroup(GuiTabbed gui, boolean leftSide) {
        this.gui = gui;
        this.leftSide = leftSide;
    }

    public void addTab(Tab tab) {
        tab.index = tabs.size();
        tab.group = this;
        tabs.add(tab);
    }

    public Tab getTab(int index) {
        return tabs.get(index);
    }

    public Tab getTabAt(int mouseX, int mouseY) {
        if (mouseX > gui.guiLeft() - 28 && mouseX < gui.guiLeft()) {
            if (mouseY > gui.guiTop()) {
                int tabIndex = (mouseY - gui.guiTop()) / 29;
                if (tabIndex < tabs.size()) {
                    return tabs.get(tabIndex);
                }
            }
        }
        return null;
    }

    public void drawBackground() {
        for (Tab tab : tabs) {
            tab.drawBackground();
        }
    }

    public void drawForeground() {
        gui.itemRenderer().zLevel = 100.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        for (Tab tab : tabs) {
            tab.drawForeground();
        }
        GL11.glDisable(GL11.GL_LIGHTING);
        gui.itemRenderer().zLevel = 0.0F;
    }

    public boolean mouseClick(int mouseX, int mouseY) {
        Tab tab = getTabAt(mouseX, mouseY);
        if (tab != null) {
            return tab.mouseClick(mouseX, mouseY);
        }
        return false;
    }
}