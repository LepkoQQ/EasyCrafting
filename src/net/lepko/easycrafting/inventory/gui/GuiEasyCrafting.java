package net.lepko.easycrafting.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import net.lepko.easycrafting.ModEasyCrafting;
import net.lepko.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.handlers.TickHandlerClient;
import net.lepko.easycrafting.helpers.ChatFormat;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.helpers.RecipeWorker;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.lepko.easycrafting.inventory.ContainerEasyCrafting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ImmutableList;

public class GuiEasyCrafting extends GuiContainer {

    private static final String GUI_TEXTURE = "/mods/" + VersionHelper.MOD_ID + "/textures/gui/easycraftinggui.png";

    private static final int TABINDEX_CRAFTING = 0;
    private static final int TABINDEX_SEARCH = 1;

    private static int selectedTabIndex = TABINDEX_CRAFTING;
    private static String lastSearch = "";

    public int currentScroll = 0;
    private int maxScroll = 0;
    private float scrollbarOffset = 0;
    public ImmutableList<EasyRecipe> renderList;
    public ImmutableList<EasyRecipe> craftableList;
    private boolean[] canCraftCache;
    private boolean wasClicking = false;
    private boolean isScrolling = false;
    private ItemStack[] tabIcons = { new ItemStack(ModEasyCrafting.blockEasyCraftingTable), new ItemStack(Item.compass) };
    private String[] tabDescriptions = { "Available Recipes", "Search Recipes" };
    private GuiTextField searchField;

    public GuiEasyCrafting(InventoryPlayer player_inventory, TileEntityEasyCrafting tile_entity) {
        super(new ContainerEasyCrafting(tile_entity, player_inventory));

        if (this.inventorySlots != null && this.inventorySlots instanceof ContainerEasyCrafting) {
            ((ContainerEasyCrafting) this.inventorySlots).gui = this;
        }

        ySize = 235;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        this.searchField = new GuiTextField(this.fontRenderer, this.guiLeft + 82, this.guiTop + 6, 89, this.fontRenderer.FONT_HEIGHT);
        this.searchField.setMaxStringLength(15);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setVisible(false);
        this.searchField.setTextColor(0xFFFFFF);
        switchToTab(selectedTabIndex);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        int offsetX = 0;
        if (selectedTabIndex != TABINDEX_SEARCH) {
            fontRenderer.drawString("Easy Crafting Table", 8, 6, 0x404040);
            offsetX = 159;
        } else {
            fontRenderer.drawString("Search:", 8, 6, 0x404040);
            offsetX = 70;
        }

        if (RecipeWorker.lock.isLocked()) {
            fontRenderer.drawString(ChatFormat.MAGIC + "x", offsetX, 6, 0x404040);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderHelper.enableGUIStandardItemLighting();
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        // Tabs
        drawTabs();
        // Main GUI
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        // Search field and output slot backgrounds
        if (selectedTabIndex == TABINDEX_SEARCH) {
            int xSearchTex = xSize - 90 - 7;
            this.drawTexturedModalRect(this.guiLeft + xSearchTex, y + 4, xSearchTex, 256 - 12, 90, 12);
            this.searchField.drawTextBox();

            if (this.canCraftCache != null) {
                int offset = this.currentScroll * 8;
                for (int k = 0; k < 40 && k + offset < this.canCraftCache.length; k++) {
                    renderSlotBackColor(this.inventorySlots.getSlot(k), this.canCraftCache[k + offset]);
                }
            }
        }
        // Storage slots background
        for (int l = 0; l < 18; l++) {
            renderSlotBackColor(this.inventorySlots.getSlot(l + 40), false);
        }
        // Scrollbar
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int xTex = this.maxScroll == 0 ? 12 : 0;
        this.drawTexturedModalRect(x + 156, y + 17 + (int) (this.scrollbarOffset * 73.0F), xTex, 240, 12, 16);
        // Selected tab
        drawTab(selectedTabIndex);
    }

    private void drawTabs() {
        for (int i = 0; i < 2; i++) {
            if (i == selectedTabIndex) {
                continue;
            }
            drawTab(i);
        }
    }

    private void drawTab(int i) {
        int width = 32;
        int height = 28;
        int texLeft = 256 - width;
        int texTop = i * height;
        int x = this.guiLeft - 28 - 2;
        int y = this.guiTop + (i * (height + 1));

        if (i == selectedTabIndex) {
            texLeft -= width;
            x += 2;
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        this.drawTexturedModalRect(x, y, texLeft, texTop, width, height);
        this.zLevel = 100.0F;
        itemRenderer.zLevel = 100.0F;
        x += 10 + (i == selectedTabIndex ? -1 : 1);
        y += 6;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        ItemStack iconItemStack = tabIcons[i];
        itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, iconItemStack, x, y);
        GL11.glDisable(GL11.GL_LIGHTING);
        itemRenderer.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        if (selectedTabIndex != TABINDEX_SEARCH) {
            if (Keyboard.isKeyDown(this.mc.gameSettings.keyBindChat.keyCode)) {
                this.switchToTab(TABINDEX_SEARCH);
            } else {
                super.keyTyped(par1, par2);
            }
        } else {
            if (!this.checkHotbarKeys(par2)) {
                if (this.searchField.textboxKeyTyped(par1, par2)) {
                    updateSearch();
                } else {
                    super.keyTyped(par1, par2);
                }
            }
        }
    }

    @Override
    public void handleMouseInput() {
        // Handle mouse scroll
        int delta = Mouse.getEventDWheel();
        if (delta == 0) {
            // Fix NEI auto clicking slots when mouse is being scrolled; only call super when mouse is not scrolling
            super.handleMouseInput();
        } else {
            setScrollPosition(this.currentScroll + (delta > 0 ? -1 : 1));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3) {
        // Handle scrollbar dragging
        boolean leftMouseDown = Mouse.isButtonDown(0);
        int left = this.guiLeft + 155;
        int top = this.guiTop + 18;
        int right = left + 14;
        int bottom = top + 89;

        if (!this.wasClicking && leftMouseDown && mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom) {
            this.isScrolling = this.maxScroll > 0;
        } else if (!leftMouseDown) {
            this.isScrolling = false;
        }

        this.wasClicking = leftMouseDown;

        if (this.isScrolling) {
            setScrollPosition(((float) (mouseY - top) - 7.5F) / ((float) (bottom - top) - 15.0F));
        }

        super.drawScreen(mouseX, mouseY, par3);

        // Handle tab hover text
        for (int i = 0; i < tabDescriptions.length; i++) {
            if (isOverTab(i, mouseX, mouseY)) {
                this.drawCreativeTabHoveringText(tabDescriptions[i], mouseX, mouseY);
            }
        }

        for (int j = 0; j < 40; j++) {
            Slot slot = this.inventorySlots.getSlot(j);
            if (this.isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)) {
                // && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.keyCode)
                this.drawIngredientTooltip(j, mouseX, mouseY);
            }
        }

        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        // Handle tab changing
        if (button == 0) {
            for (int i = 0; i < tabDescriptions.length; i++) {
                if (i != selectedTabIndex && isOverTab(i, x, y)) {
                    switchToTab(i);
                    return;
                }
            }
        }

        super.mouseClicked(x, y, button);
    }

    private boolean isOverTab(int tabIndex, int x, int y) {
        int width = 32;
        int height = 28;
        int tabX = this.guiLeft - 28 - 2;
        int tabY = this.guiTop + (tabIndex * (height + 1));
        return x > tabX && x < tabX + width && y > tabY && y < tabY + height;
    }

    private void switchToTab(int tabIndex) {
        if (this.searchField != null) {
            if (tabIndex == TABINDEX_SEARCH) {
                this.searchField.setVisible(true);
                this.searchField.setCanLoseFocus(false);
                this.searchField.setFocused(true);
                this.searchField.setText(lastSearch);
            } else {
                this.searchField.setVisible(false);
                this.searchField.setCanLoseFocus(true);
                this.searchField.setFocused(false);
            }
        }
        GuiEasyCrafting.selectedTabIndex = tabIndex;
        updateSearch();
    }

    public void renderSlotBackColor(Slot slot, boolean canCraft) {
        int x = this.guiLeft + slot.xDisplayPosition;
        int y = this.guiTop + slot.yDisplayPosition;
        int w = 16;
        int h = 16;
        int color = canCraft ? 0x8000A000 : 0x80A00000;
        Gui.drawRect(x, y, x + w, y + h, color);
    }

    @SuppressWarnings("unchecked")
    private void updateSearch() {
        if (selectedTabIndex == TABINDEX_SEARCH) {
            ImmutableList<EasyRecipe> all = RecipeHelper.getAllRecipes();
            ArrayList<EasyRecipe> list = new ArrayList<EasyRecipe>();
            lastSearch = this.searchField.getText().toLowerCase();

            recipeLoop: for (int i = 0; i < all.size(); i++) {
                EasyRecipe r = all.get(i);
                List<String> itemProps = r.getResult().toItemStack().getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
                for (int j = 0; j < itemProps.size(); j++) {
                    if (itemProps.get(j).toLowerCase().contains(lastSearch)) {
                        list.add(r);
                        continue recipeLoop;
                    }
                }
            }

            this.renderList = ImmutableList.copyOf(list);
        }
        this.currentScroll = 0;
        this.scrollbarOffset = 0.0F;
        TickHandlerClient.updateEasyCraftingOutput();
    }

    public void refreshCraftingOutput() {
        craftableList = RecipeWorker.instance().getCraftableRecipes();
        if (selectedTabIndex == TABINDEX_CRAFTING) {
            renderList = craftableList;
        } else if (selectedTabIndex == TABINDEX_SEARCH) {
            updateSlotBackgroundCache();
        }

        this.maxScroll = (int) (Math.ceil((double) renderList.size() / 8.0D) - 5);
        if (this.maxScroll < 0) {
            this.maxScroll = 0;
        }

        if (this.currentScroll > this.maxScroll) {
            setScrollPosition(this.maxScroll);
        } else {
            ContainerEasyCrafting c = (ContainerEasyCrafting) this.inventorySlots;
            c.scrollTo(this.currentScroll, renderList);
        }
    }

    private void updateSlotBackgroundCache() {
        this.canCraftCache = new boolean[renderList.size()];
        for (int i = 0; i < renderList.size(); i++) {
            if (craftableList.contains(renderList.get(i))) {
                canCraftCache[i] = true;
            } else {
                canCraftCache[i] = false;
            }
        }
    }

    private void setScrollPosition(int scroll) {
        if (scroll < 0) {
            scroll = 0;
        } else if (scroll > this.maxScroll) {
            scroll = this.maxScroll;
        }
        this.currentScroll = scroll;

        this.scrollbarOffset = (float) this.currentScroll / (float) this.maxScroll;
        if (this.scrollbarOffset < 0.0F || Float.isNaN(this.scrollbarOffset)) {
            this.scrollbarOffset = 0.0F;
        } else if (this.scrollbarOffset > 1.0F) {
            this.scrollbarOffset = 1.0F;
        }

        ContainerEasyCrafting c = (ContainerEasyCrafting) this.inventorySlots;
        c.scrollTo(this.currentScroll, renderList);
    }

    private void setScrollPosition(float scrollOffset) {
        if (scrollOffset < 0.0F || Float.isNaN(scrollOffset)) {
            scrollOffset = 0.0F;
        } else if (scrollOffset > 1.0F) {
            scrollOffset = 1.0F;
        }

        if (this.scrollbarOffset == scrollOffset) {
            return;
        }
        this.scrollbarOffset = scrollOffset;

        this.currentScroll = (int) (this.scrollbarOffset * (float) this.maxScroll);
        if (this.currentScroll < 0) {
            this.currentScroll = 0;
        } else if (this.currentScroll > this.maxScroll) {
            this.currentScroll = this.maxScroll;
        }

        ContainerEasyCrafting c = (ContainerEasyCrafting) this.inventorySlots;
        c.scrollTo(this.currentScroll, renderList);
    }

    protected void drawIngredientTooltip(int slotIndex, int mouseX, int mouseY) {

        EasyRecipe recipe = null;

        int recipe_index = slotIndex + (this.currentScroll * 8);
        if (recipe_index >= 0 && this.renderList != null && recipe_index < this.renderList.size()) {
            EasyRecipe r = this.renderList.get(recipe_index);
            if (r.getResult().equalsItemStack(this.inventorySlots.getSlot(slotIndex).getStack())) {
                recipe = r;
            }
        }

        if (recipe == null) {
            return;
        }

        ArrayList<ItemStack> ingredientList = recipe.getCompactIngredientList();

        if (!ingredientList.isEmpty()) {
            int width = 16;
            int height = 16;
            int xPos = mouseX - width - 12;
            int yPos = mouseY - 4;

            if (ingredientList.size() > 1) {
                height += (ingredientList.size() - 1) * (height + 2);
            }

            if (this.guiTop + yPos + height + 6 > this.height) {
                yPos = this.height - height - this.guiTop - 6;
            }

            int bgColor = 0xF0100010;
            int borderColor = 0x505000FF;// red: 0x50FF0000;// green: 0x5000A700;// vanilla purple: 0x505000FF;
            int borderColorDark = (borderColor & 0xFEFEFE) >> 1 | borderColor & 0xFF000000;

            this.zLevel = 300.0F;
            itemRenderer.zLevel = 300.0F;

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);

            this.drawGradientRect(xPos - 3, yPos - 4, xPos + width + 3, yPos - 3, bgColor, bgColor);
            this.drawGradientRect(xPos - 3, yPos + height + 3, xPos + width + 3, yPos + height + 4, bgColor, bgColor);
            this.drawGradientRect(xPos - 3, yPos - 3, xPos + width + 3, yPos + height + 3, bgColor, bgColor);
            this.drawGradientRect(xPos - 4, yPos - 3, xPos - 3, yPos + height + 3, bgColor, bgColor);
            this.drawGradientRect(xPos + width + 3, yPos - 3, xPos + width + 4, yPos + height + 3, bgColor, bgColor);

            this.drawGradientRect(xPos - 3, yPos - 3 + 1, xPos - 3 + 1, yPos + height + 3 - 1, borderColor, borderColorDark);
            this.drawGradientRect(xPos + width + 2, yPos - 3 + 1, xPos + width + 3, yPos + height + 3 - 1, borderColor, borderColorDark);
            this.drawGradientRect(xPos - 3, yPos - 3, xPos + width + 3, yPos - 3 + 1, borderColor, borderColor);
            this.drawGradientRect(xPos - 3, yPos + height + 2, xPos + width + 3, yPos + height + 3, borderColorDark, borderColorDark);

            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            for (ItemStack is : ingredientList) {
                itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, is, xPos, yPos);
                itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, is, xPos, yPos);

                yPos += 18;
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);

            this.zLevel = 0.0F;
            itemRenderer.zLevel = 0.0F;
        }
    }
}