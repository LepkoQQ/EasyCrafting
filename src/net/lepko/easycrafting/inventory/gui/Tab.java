package net.lepko.easycrafting.inventory.gui;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public abstract class Tab {

    public int width = 32;
    public int height = 28;
    public int index = 0;
    public TabGroup group;
    public ItemStack iconStack = new ItemStack(Block.stone);
    public String tooltip = "tab";

    public Tab(ItemStack iconStack, String tooltip) {
        this.iconStack = iconStack;
        this.tooltip = tooltip;
    }

    public void drawBackground() {
        int texLeft = 256 - width;
        int texTop = index * height;

        int x = group.gui.guiLeft() - 28;
        int y = group.gui.guiTop() + index * (height + 1);

        if (group.gui.currentTab == index) {
            group.gui.drawRectangle(x, y, texLeft - width, texTop, width, height);
        } else {
            group.gui.drawRectangle(x, y, texLeft, texTop, width - 4, height);
        }
    }

    public void drawForeground() {
        int x = -18;
        int y = index * (height + 1) + 6;
        if (group.gui.currentTab == index) {
            x -= 2;
        }
        group.gui.itemRenderer().renderItemAndEffectIntoGUI(group.gui.mc().fontRenderer, group.gui.mc().renderEngine, iconStack, x, y);
    }

    public boolean mouseClick(int mouseX, int mouseY) {
        if (group.gui.currentTab != index) {
            group.gui.currentTab = index;
            onTabSelected();
            return true;
        }
        return false;
    }

    public void onTabSelected() {
    }
}