package net.lepko.easycrafting.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import net.lepko.easycrafting.ModEasyCrafting;
import net.lepko.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.helpers.RecipeWorker;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.lepko.easycrafting.inventory.ContainerEasyCrafting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiEasyCrafting extends GuiTabbed {

    private class TabEasyCrafting extends Tab {
        public TabEasyCrafting(ItemStack iconStack, String tooltip) {
            super(iconStack, tooltip);
        }

        @Override
        public void onTabSelected() {
            updateSearch();
        }
    }

    private static final String GUI_TEXTURE = "/mods/" + VersionHelper.MOD_ID + "/textures/gui/easycraftinggui.png";
    private static String LAST_SEARCH = "";

    public List<EasyRecipe> shownRecipes;
    public List<EasyRecipe> craftableRecipes;

    // TODO: private
    public int currentRowOffset = 0;
    private int maxRowOffset = 0;
    private float currentScrollValue = 0;
    private boolean wasClicking = false;
    private boolean isDraggingScrollBar = false;

    private final IInventory tileInventory;
    private GuiTextField searchField;

    public GuiEasyCrafting(InventoryPlayer playerInventory, TileEntityEasyCrafting tileInventory) {
        super(new ContainerEasyCrafting(playerInventory, tileInventory));
        this.tileInventory = tileInventory;
        ((ContainerEasyCrafting) inventorySlots).gui = this;
        ySize = 235;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        searchField = new GuiTextField(fontRenderer, guiLeft + 82, guiTop + 6, 89, fontRenderer.FONT_HEIGHT);
        searchField.setMaxStringLength(32);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setVisible(true);
        searchField.setTextColor(0xFFFFFF);
        searchField.setCanLoseFocus(false);
        searchField.setFocused(true);
        searchField.setText(LAST_SEARCH);
    }

    @Override
    public void initTabs() {
        tabGroup.addTab(new TabEasyCrafting(new ItemStack(ModEasyCrafting.blockEasyCraftingTable), "Available"));
        tabGroup.addTab(new TabEasyCrafting(new ItemStack(Item.compass), "Search"));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString("Easy Crafting", 7, 6, 0x404040);
        int offsetX = 70;

        // fontRenderer.drawString("Search:", 8, 6, 0x404040);
        // offsetX = 70;

        if (RecipeWorker.lock.isLocked()) {
            fontRenderer.drawString(EnumChatFormatting.OBFUSCATED + "x", offsetX, 6, 0x404040);
        }

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        // Background
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Tabs
        super.drawGuiContainerBackgroundLayer(f, i, j);

        // Scrollbar
        int scrollTextureX = maxRowOffset == 0 ? 12 : 0;
        drawTexturedModalRect(guiLeft + 156, guiTop + 17 + (int) (currentScrollValue * 73.0F), scrollTextureX, 240, 12, 16);

        // Search
        int searchTextureX = xSize - 90 - 7;
        drawTexturedModalRect(guiLeft + searchTextureX, guiTop + 4, searchTextureX, 256 - 12, 90, 12);
        searchField.drawTextBox();

        // Output slot backgrounds
        // if (selectedTabIndex == TABINDEX_SEARCH) {
        // if (canCraftCache != null) {
        // int offset = currentScroll * 8;
        // for (int k = 0; k < 40 && k + offset < canCraftCache.length; k++) {
        // renderSlotBackColor(inventorySlots.getSlot(k), canCraftCache[k + offset]);
        // }
        // }
        // }
        // Storage slots background
        // for (int l = 0; l < 18; l++) {
        // renderSlotBackColor(inventorySlots.getSlot(l + 40), false);
        // }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        if (!checkHotbarKeys(par2)) {
            if (searchField.textboxKeyTyped(par1, par2)) {
                updateSearch();
            } else {
                super.keyTyped(par1, par2);
            }
        }
    }

    @Override
    public void handleMouseInput() {
        int mouseScroll = Mouse.getEventDWheel();
        if (mouseScroll == 0) { // Bypass NEI fast transfer manager
            super.handleMouseInput();
        } else {
            setRowOffset(currentRowOffset + (mouseScroll > 0 ? -1 : 1));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float time) {
        // Handle scrollbar dragging
        boolean leftMouseDown = Mouse.isButtonDown(0);
        int left = guiLeft + 155;
        int top = guiTop + 18;
        int right = left + 14;
        int bottom = top + 89;

        if (!wasClicking && leftMouseDown && mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom) {
            isDraggingScrollBar = maxRowOffset > 0;
        } else if (!leftMouseDown) {
            isDraggingScrollBar = false;
        }

        wasClicking = leftMouseDown;

        if (isDraggingScrollBar) {
            setScrollValue((mouseY - top - 7.5F) / (bottom - top - 15.0F));
        }

        super.drawScreen(mouseX, mouseY, time);
    }

    // public void renderSlotBackColor(Slot slot, boolean canCraft) {
    // int x = guiLeft + slot.xDisplayPosition;
    // int y = guiTop + slot.yDisplayPosition;
    // int w = 16;
    // int h = 16;
    // int color = canCraft ? 0x8000A000 : 0x80A00000;
    // Gui.drawRect(x, y, x + w, y + h, color);
    // }

    @SuppressWarnings("unchecked")
    private void updateSearch() {
        List<EasyRecipe> all = currentTab == 0 ? craftableRecipes : RecipeHelper.getAllRecipes();
        List<EasyRecipe> list = new ArrayList<EasyRecipe>();
        if (all == null || searchField == null) {
            return;
        }
        LAST_SEARCH = searchField.getText().toLowerCase();
        if (!LAST_SEARCH.trim().isEmpty()) {
            for (EasyRecipe recipe : all) {
                List<String> tips = recipe.getResult().toItemStack().getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
                for (String tip : tips) {
                    if (tip.toLowerCase().contains(LAST_SEARCH)) {
                        list.add(recipe);
                        break;
                    }
                }
            }
            shownRecipes = list;
        } else {
            shownRecipes = all;
        }

        maxRowOffset = (int) (Math.ceil(shownRecipes.size() / 8.0D) - 5);
        maxRowOffset = maxRowOffset < 0 ? 0 : maxRowOffset;

        setRowOffset(0);
    }

    public void refreshCraftingOutput() {
        craftableRecipes = RecipeWorker.instance().getCraftableRecipes();
        updateSearch();
        // updateSlotBackgroundCache();
        // setRowOffset(currentRowOffset);
    }

    // private void updateSlotBackgroundCache() {
    // canCraftCache = new boolean[renderList.size()];
    // for (int i = 0; i < renderList.size(); i++) {
    // if (craftableList.contains(renderList.get(i))) {
    // canCraftCache[i] = true;
    // } else {
    // canCraftCache[i] = false;
    // }
    // }
    // }

    private void setRowOffset(int rowOffset) {
        currentRowOffset = MathHelper.clamp_int(rowOffset, 0, maxRowOffset);
        currentScrollValue = MathHelper.clamp_float(currentRowOffset / (float) maxRowOffset, 0F, 1F);
        setSlots(currentRowOffset, shownRecipes);
    }

    private void setScrollValue(float scrollValue) {
        currentScrollValue = MathHelper.clamp_float(scrollValue, 0F, 1F);
        currentRowOffset = MathHelper.clamp_int((int) (currentScrollValue * maxRowOffset + 0.5F), 0, maxRowOffset);
        setSlots(currentRowOffset, shownRecipes);
    }

    // TODO: this can get data from fields now, remove args
    private void setSlots(int currentScroll, List<EasyRecipe> renderList) {
        if (renderList != null) {
            int offset = currentScroll * 8;
            for (int i = 0; i < 40; i++) {
                if (i + offset >= renderList.size() || i + offset < 0) {
                    tileInventory.setInventorySlotContents(i, null);
                } else {
                    ItemStack is = renderList.get(i + offset).getResult().toItemStack();
                    tileInventory.setInventorySlotContents(i, is);
                }
            }
        }
    }

    // TODO: simplify
    protected void drawIngredientTooltip(int slotIndex, int mouseX, int mouseY, boolean leftSide) {

        EasyRecipe recipe = null;

        int recipe_index = slotIndex + currentRowOffset * 8;
        if (recipe_index >= 0 && shownRecipes != null && recipe_index < shownRecipes.size()) {
            EasyRecipe r = shownRecipes.get(recipe_index);
            if (r.getResult().equalsItemStack(inventorySlots.getSlot(slotIndex).getStack())) {
                recipe = r;
            }
        }

        if (recipe == null) {
            return;
        }

        ArrayList<ItemStack> ingredientList = recipe.getCompactIngredientList();

        if (ingredientList != null && !ingredientList.isEmpty()) {
            int width = 16;
            int height = 16;
            int xPos = mouseX + 12;
            int yPos = mouseY - 12 + 14;

            if (ingredientList.size() > 1) {
                width += (ingredientList.size() - 1) * (width + 2);
            }

            if (leftSide) {
                xPos -= 28 + width;
            }

            // TODO: change color from craftcache
            int bgColor = 0xF0100010;
            int borderColor = 0x5000A700;// red: 0x50FF0000;// green: 0x5000A700;// vanilla purple: 0x505000FF;
            int borderColorDark = (borderColor & 0xFEFEFE) >> 1 | borderColor & 0xFF000000;

            zLevel = 300.0F;
            itemRenderer.zLevel = 300.0F;

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);

            drawGradientRect(xPos - 3, yPos - 4, xPos + width + 3, yPos - 3, bgColor, bgColor);
            drawGradientRect(xPos - 3, yPos + height + 3, xPos + width + 3, yPos + height + 4, bgColor, bgColor);
            drawGradientRect(xPos - 3, yPos - 3, xPos + width + 3, yPos + height + 3, bgColor, bgColor);
            drawGradientRect(xPos - 4, yPos - 3, xPos - 3, yPos + height + 3, bgColor, bgColor);
            drawGradientRect(xPos + width + 3, yPos - 3, xPos + width + 4, yPos + height + 3, bgColor, bgColor);

            drawGradientRect(xPos - 3, yPos - 3 + 1, xPos - 3 + 1, yPos + height + 3 - 1, borderColor, borderColorDark);
            drawGradientRect(xPos + width + 2, yPos - 3 + 1, xPos + width + 3, yPos + height + 3 - 1, borderColor, borderColorDark);
            drawGradientRect(xPos - 3, yPos - 3, xPos + width + 3, yPos - 3 + 1, borderColor, borderColor);
            drawGradientRect(xPos - 3, yPos + height + 2, xPos + width + 3, yPos + height + 3, borderColorDark, borderColorDark);

            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            for (ItemStack is : ingredientList) {
                if (is.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                    ItemStack is2 = is.copy();
                    is2.setItemDamage(0);
                    // TODO: rotate display of all possible stacks
                    itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, is2, xPos, yPos);
                    itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, is2, xPos, yPos);
                } else {
                    itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, is, xPos, yPos);
                    itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, is, xPos, yPos);
                }

                xPos += 18;
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);

            zLevel = 0.0F;
            itemRenderer.zLevel = 0.0F;
        }
    }

    @Override
    protected void drawItemStackTooltip(ItemStack stack, int mouseX, int mouseY) {
        if (isCtrlKeyDown()) {
            for (int j = 0; j < 40; j++) {
                Slot slot = inventorySlots.getSlot(j);
                if (isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)) {
                    List<String> list = new ArrayList<String>();
                    String itemName = (String) stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips).get(0);
                    list.add("\u00a7" + Integer.toHexString(stack.getRarity().rarityColor) + itemName);

                    FontRenderer font = stack.getItem().getFontRenderer(stack);
                    drawHoveringText(list, mouseX, mouseY, (font == null ? fontRenderer : font));

                    boolean leftSide = (mouseX + 12 + fontRenderer.getStringWidth(itemName) > this.width);
                    drawIngredientTooltip(j, mouseX, mouseY, leftSide);
                    return;
                }
            }
        }
        super.drawItemStackTooltip(stack, mouseX, mouseY);
    }
}