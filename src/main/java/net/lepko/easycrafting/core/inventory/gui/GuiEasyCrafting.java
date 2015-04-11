package net.lepko.easycrafting.core.inventory.gui;

import codechicken.nei.guihook.IContainerTooltipHandler;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.Optional;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.block.ModBlocks;
import net.lepko.easycrafting.core.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.lepko.easycrafting.core.inventory.ContainerEasyCrafting;
import net.lepko.easycrafting.core.network.PacketHandler;
import net.lepko.easycrafting.core.network.message.MessageEasyCrafting;
import net.lepko.easycrafting.core.recipe.RecipeChecker;
import net.lepko.easycrafting.core.recipe.RecipeHelper;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.lepko.easycrafting.core.util.InventoryUtils;
import net.lepko.easycrafting.core.util.ItemMap;
import net.lepko.easycrafting.core.util.StackUtils;
import net.lepko.easycrafting.core.util.WrappedStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Optional.Interface(iface = "codechicken.nei.guihook.IContainerTooltipHandler", modid = "NotEnoughItems")
public class GuiEasyCrafting extends GuiTabbed implements IContainerTooltipHandler {

	private class TabEasyCrafting extends Tab {
		public TabEasyCrafting(ItemStack iconStack, String tooltip) {
			super(iconStack, tooltip);
		}

		@Override
		public void onTabSelected() {
			updateSearch(true);
		}
	}

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Ref.RES_DOMAIN, "textures/gui/easycraftinggui.png");
	private static String LAST_SEARCH = "";
	private static boolean WORKER_LOCKED = false;

	public List<WrappedRecipe> shownRecipes;
	public List<WrappedRecipe> craftableRecipes = ImmutableList.of();

	private int currentRowOffset = 0;
	private int maxRowOffset = 0;
	private float currentScrollValue = 0;
	private boolean wasClicking = false;
	private boolean isDraggingScrollBar = false;
	private boolean[] canCraftCache;

	private final IInventory tileInventory;
	private GuiTextField searchField;

	public GuiEasyCrafting(InventoryPlayer playerInventory, TileEntityEasyCrafting tileInventory) {
		super(new ContainerEasyCrafting(playerInventory, tileInventory));
		this.tileInventory = tileInventory;
		ySize = 235;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		searchField = new GuiTextField(fontRendererObj, guiLeft + 82, guiTop + 6, 89, fontRendererObj.FONT_HEIGHT);
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
		tabGroup.addTab(new TabEasyCrafting(new ItemStack(ModBlocks.table), "Available"));
		tabGroup.addTab(new TabEasyCrafting(new ItemStack(Items.compass), "Search"));
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float time) {
		// XXX: Check lock on worker thread
		WORKER_LOCKED = !RecipeChecker.INSTANCE.done;

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
		if (canCraftCache != null && currentTab != 0) {
			int offset = currentRowOffset * 8;
			for (int k = 0; k < 40 && k + offset < canCraftCache.length; k++) {
				drawSlotBackground(inventorySlots.getSlot(k), canCraftCache[k + offset]);
			}
		}

		// Storage slots background
		for (int l = 0; l < 18; l++) {
			drawSlotBackground(inventorySlots.getSlot(l + 40), false);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String title = "Easy Crafting";
		if (WORKER_LOCKED) {
			title = "Searching...";
		}
		fontRendererObj.drawString(title, 7, 6, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (!checkHotbarKeys(par2)) {
			if (searchField.textboxKeyTyped(par1, par2)) {
				updateSearch(true);
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

	// button:
	// 0 -> left mouse
	// 1 -> action=0 or 1 -> right mouse
	// _____action=5 -> left click dragged over
	// 2 -> middle mouse
	// 5 -> action=5 -> right click dragged over
	//
	// action:
	// 0 -> click
	// 1 -> shift click
	// 2 -> swap with slot in hotbar (button is 0-8)
	// 3 -> pick block
	// 4 -> drop block
	// 5 -> dragged stack
	// 6 -> double click
	@Override
	protected void handleMouseClick(Slot slot, int slotIndex, int button, int action) {
		if (slotIndex >= 0 && slotIndex < 40) {
			onCraftingSlotClick(slot, slotIndex, button, action);
		} else {
			super.handleMouseClick(slot, slotIndex, button, action);
		}
	}

	private void onCraftingSlotClick(Slot slot, int slotIndex, int button, int action) {
		Ref.LOGGER.trace("Clicked: " + slot.getClass().getSimpleName() + "@" + slotIndex + ", button=" + button + ", action=" + action + ", stack=" + slot.getStack());

		if (action > 1 || button > 1 || !slot.getHasStack()) {
			return;
		}

		ItemStack heldStack = mc.thePlayer.inventory.getItemStack();
		ItemStack slotStack = slot.getStack();

		WrappedRecipe recipe = null;
		int recipeIndex = slotIndex + currentRowOffset * 8;
		if (recipeIndex >= 0 && shownRecipes != null && recipeIndex < shownRecipes.size()) {
			WrappedRecipe r = shownRecipes.get(recipeIndex);
			if (StackUtils.areEqualNoSizeNoNBT(r.getOutput(), slotStack) && craftableRecipes != null && craftableRecipes.contains(r)) {
				recipe = r;
			}
		}
		if (recipe == null) {
			return;
		}

		// slotStack already has a stack from recipe.handler.getCraftingResult()
		ItemStack finalStack = slotStack.copy();
		int finalStackSize = 0;

		if (heldStack == null) {
			finalStackSize = finalStack.stackSize;
		} else if (StackUtils.canStack(slotStack, heldStack) == 0) {
			finalStackSize = finalStack.stackSize + heldStack.stackSize;
		}

		if (finalStackSize > 0) {
			boolean isRightClick = button == 1;
			boolean isShiftClick = action == 1;

			PacketHandler.INSTANCE.sendToServer(new MessageEasyCrafting(recipe, isRightClick, isShiftClick));

			if (isRightClick) { // Right click; craft until max stack
				int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(slotStack, heldStack);
				int timesCrafted = RecipeHelper.canCraft(recipe, mc.thePlayer.inventory, RecipeManager.getAllRecipes(), false, maxTimes, ConfigHandler.MAX_RECURSION);
				if (timesCrafted > 0) {
					finalStack.stackSize = finalStackSize + (timesCrafted - 1) * finalStack.stackSize;
					mc.thePlayer.inventory.setItemStack(finalStack);
				}
			} 
			else if (isShiftClick) {
				int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(slotStack, null);
				int timesCrafted = RecipeHelper.canCraftWithComponents(recipe, mc.thePlayer.inventory, RecipeManager.getAllRecipes(), false, maxTimes, ConfigHandler.MAX_RECURSION);
				if (timesCrafted > 0) {	
					finalStack.stackSize *= timesCrafted; //ignore finalStackSize; it might contain heldStack size
					RecipeHelper.canCraftWithComponents(recipe, mc.thePlayer.inventory, RecipeManager.getAllRecipes(), true, timesCrafted, ConfigHandler.MAX_RECURSION);
					InventoryUtils.addItemToInventory(mc.thePlayer.inventory, finalStack);
				}
			}
			else { // Left click; craft once
				finalStack.stackSize = finalStackSize;
				mc.thePlayer.inventory.setItemStack(finalStack);
			}
		}
	}

	private void drawSlotBackground(Slot slot, boolean canCraft) {
		int x = guiLeft + slot.xDisplayPosition;
		int y = guiTop + slot.yDisplayPosition;
		int color = canCraft ? 0x8000A000 : 0x80A00000;
		Gui.drawRect(x, y, x + 16, y + 16, color);
	}

	@SuppressWarnings("unchecked")
	private void updateSearch(boolean scrollToTop) {
		List<WrappedRecipe> all = currentTab == 0 ? craftableRecipes : RecipeManager.getAllRecipes();
		List<WrappedRecipe> list = new ArrayList<WrappedRecipe>();
		if (all == null || searchField == null) {
			return;
		}

		LAST_SEARCH = searchField.getText().toLowerCase();
		if (!LAST_SEARCH.trim().isEmpty()) {
			for (WrappedRecipe recipe : all) {
				try {
					List<String> tips = recipe.getOutput().getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
					for (String tip : tips) {
						if (tip.toLowerCase().contains(LAST_SEARCH)) {
							list.add(recipe);
							break;
						}
					}
				}
				catch(NullPointerException ex)
				{
					Ref.LOGGER.info("Exception on Update Search. Continuing with search" + recipe.getOutput().toString());
				}
			}
			shownRecipes = list;
		} else {
			shownRecipes = all;
		}

		maxRowOffset = (int) (Math.ceil(shownRecipes.size() / 8.0D) - 5);
		maxRowOffset = maxRowOffset < 0 ? 0 : maxRowOffset;

		rebuildCanCraftCache();
		setRowOffset(scrollToTop ? 0 : currentRowOffset);

	}


	public void refreshCraftingOutput() {
		craftableRecipes = ImmutableList.copyOf(RecipeChecker.INSTANCE.recipes);
		updateSearch(false);
	}

	private void rebuildCanCraftCache() {
		canCraftCache = new boolean[shownRecipes.size()];
		for (int i = 0; i < shownRecipes.size(); i++) {
			canCraftCache[i] = craftableRecipes.contains(shownRecipes.get(i));
		}
	}

	private void setRowOffset(int rowOffset) {
		currentRowOffset = MathHelper.clamp_int(rowOffset, 0, maxRowOffset);
		currentScrollValue = MathHelper.clamp_float(currentRowOffset / (float) maxRowOffset, 0F, 1F);
		setSlots();
	}

	private void setScrollValue(float scrollValue) {
		currentScrollValue = MathHelper.clamp_float(scrollValue, 0F, 1F);
		currentRowOffset = MathHelper.clamp_int((int) (currentScrollValue * maxRowOffset + 0.5F), 0, maxRowOffset);
		setSlots();
	}

	private void setSlots() {
		if (shownRecipes != null) {
			int offset = currentRowOffset * 8;
			for (int i = 0; i < 40; i++) {
				if (i + offset >= shownRecipes.size() || i + offset < 0) {
					tileInventory.setInventorySlotContents(i, null);
				} else {
					WrappedRecipe recipe = shownRecipes.get(i + offset);
					ItemStack is = recipe.handler.getCraftingResult(recipe, recipe.usedIngredients);
					tileInventory.setInventorySlotContents(i, is);
				}
			}
		}
	}

	// START NEI
	@Override
	public List<String> handleTooltip(GuiContainer gui, int mouseX, int mouseY, List<String> currentTip) {
		return currentTip;
	}

	@Override
	public List<String> handleItemDisplayName(GuiContainer gui, ItemStack stack, List<String> currentTip) {
		return currentTip;
	}

	@Override
	public List<String> handleItemTooltip(GuiContainer gui, ItemStack stack, int mouseX, int mouseY, List<String> currentTip) {
		if (!drawCustomTooltip(stack, mouseX, mouseY, currentTip)) {
			return currentTip;
		}
		// Hack to always return empty list
		return new LinkedList<String>() {
			@Override
			public int size() {
				return 0;
			}
		};
	}
	// END NEI

	@SuppressWarnings("unchecked")
	@Override
	protected void renderToolTip(ItemStack stack, int mouseX, int mouseY) {
		if (!drawCustomTooltip(stack, mouseX, mouseY, (List<String>) stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips))) {
			super.renderToolTip(stack, mouseX, mouseY);
		}
	}

	private boolean drawCustomTooltip(ItemStack stack, int mouseX, int mouseY, List<String> currentTip) {
		if (isCtrlKeyDown() && currentTip != null && !currentTip.isEmpty()) {
			for (int j = 0; j < 40; j++) {
				Slot slot = inventorySlots.getSlot(j);
				//isPointInRegion
				if (func_146978_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)) {
					FontRenderer font = stack.getItem().getFontRenderer(stack);
					String itemName = currentTip.get(0);
					List<String> list = ImmutableList.of(stack.getRarity().rarityColor + itemName);
					drawHoveringText(list, mouseX, mouseY, font == null ? fontRendererObj : font);

					boolean leftSide = mouseX + 12 + fontRendererObj.getStringWidth(itemName) > width;
					drawIngredientTooltip(j, mouseX, mouseY, leftSide);
					return true;
				}
			}
		}
		return false;
	}

	private void drawIngredientTooltip(int slotIndex, int mouseX, int mouseY, boolean leftSide) {

		WrappedRecipe recipe = null;

		int recipe_index = slotIndex + currentRowOffset * 8;
		if (recipe_index >= 0 && shownRecipes != null && recipe_index < shownRecipes.size()) {
			WrappedRecipe r = shownRecipes.get(recipe_index);
			if (StackUtils.areCraftingEquivalent(r.getOutput(), inventorySlots.getSlot(slotIndex).getStack())) {
				recipe = r;
			}
		}

		if (recipe == null) {
			return;
		}

		boolean canCraft = canCraftCache[recipe_index];
		List<WrappedStack> ingredientList = canCraft && !recipe.usedIngredients.isEmpty() ? StackUtils.collateStacks(recipe.usedIngredients) : recipe.collatedInputs;

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

			// red: 0x50FF0000;// green: 0x5000A700;// vanilla purple: 0x505000FF;
			int bgColor = 0xF0100010;
			int borderColor = canCraft ? 0x5000A700 : 0x50FF0000;
			int borderColorDark = (borderColor & 0xFEFEFE) >> 1 | borderColor & 0xFF000000;

		zLevel += 500.0F;
		itemRender.zLevel += 500.0F;

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

		for (WrappedStack ws : ingredientList) {
			renderItem(xPos, yPos, ws);
			xPos += 18;
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);

		zLevel -= 500.0F;
		itemRender.zLevel -= 500.0F;
		}
	}

	private void renderItem(int xPos, int yPos, WrappedStack ws) {
		ItemStack is = null;

		int count = 0;
		for (ItemStack stack : ws.stacks) {
			if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE && stack.getHasSubtypes()) {
				count += ItemMap.get(stack.getItem()).size();
			} else {
				count++;
			}
		}

		int num = (int) ((mc.theWorld.getTotalWorldTime() / 10) % count);

		count = 0;
		for (ItemStack stack : ws.stacks) {
			if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE && stack.getHasSubtypes()) {
				List<ItemStack> all = ItemMap.get(stack.getItem());
				if (num >= count && num < count + all.size()) {
					is = StackUtils.copyStack(all.get(num - count), ws.size);
					break;
				}
				count += all.size();
			} else {
				if (num == count) {
					is = StackUtils.copyStack(stack, ws.size);
					break;
				}
				count++;
			}
		}

		//
		if (is != null) {
			itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, is, xPos, yPos);
			itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, is, xPos, yPos);
		} else {
			Ref.LOGGER.warn("Error rendering stack in recipe tooltip: is == null");
		}
	}
}