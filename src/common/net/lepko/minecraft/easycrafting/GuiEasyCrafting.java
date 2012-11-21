package net.lepko.minecraft.easycrafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderHelper;
import net.minecraft.src.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This handles the drawing of the gui, and user inputs.
 */
public class GuiEasyCrafting extends GuiContainer {

	/** Index of the crafting tab */
	private static final int TABINDEX_CRAFTING = 0;
	/** Index of the search tab */
	private static final int TABINDEX_SEARCH = 1;

	/** The index of the currently selected tab */
	private static int selectedTabIndex = TABINDEX_CRAFTING;

	/** How many rows down the slider of the scrollbar is currently scrolled */
	protected int currentScroll = 0;
	/** The maximum amount of rows the slider can scroll down */
	private int maxScroll = 0;
	/** The draw position of the slider of the scrollbar, from the top of the scrollbar */
	private float scrollbarOffset = 0;
	/** The list of recipes that are to appear in the gui (differs from the craftablelist when in the search tab, but equal when in the craft tab) */
	protected ArrayList<EasyRecipe> renderList = new ArrayList<EasyRecipe>();
	/** The list of recipes that the player can craft */
	protected ArrayList<EasyRecipe> craftableList = new ArrayList<EasyRecipe>();
	/** Search tab, the list of recipes that the player has the available ingredients for */
	private boolean[] canCraftCache;
	/** Used to track dragging of the scrollbar slider, keeps track if the user had the mouse-button pressed the previous tick */
	private boolean wasClicking = false;
	/** Used to track dragging of the scrollbar slider, keeps track if the slider is being dragged */
	private boolean isScrolling = false;
	/** Defines the icons for the tabs of the gui */
	private ItemStack[] tabIcons = { new ItemStack(ModEasyCrafting.blockEasyCraftingTable), new ItemStack(Item.compass) };
	/** The text that appears when hovering over the tabs of the gui */
	private String[] tabDescriptions = { "Available Recipes", "Search Recipes" };
	/** Search tab, the text used as the search criteria */
	private GuiTextField searchField;
	/** The easycraft table tile entity linked to this gui. */
	protected TileEntityEasyCrafting tileEntity;

	/**
	 * Creates an instance of this class.
	 *
	 * @param  player_inventory		The inventory of the player interacting with this gui
	 * @param  tile_entity			The tile entity that is the easycraft table
	 * @return N/A
	 */
	public GuiEasyCrafting(InventoryPlayer player_inventory, TileEntityEasyCrafting tile_entity) {
		super(new ContainerEasyCrafting(tile_entity, player_inventory));
		
		this.tileEntity = tile_entity;

		if (this.inventorySlots != null && this.inventorySlots instanceof ContainerEasyCrafting) {
			((ContainerEasyCrafting) this.inventorySlots).gui = this;
		}

		ySize = 235;
	}

	/**
	 * Initialises the needed variables with their default values.
	 *
	 * @param  N/A
	 * @return N/A
	 */
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

	/**
	 * Properly closes off the gui.
	 *
	 * @param  N/A
	 * @return N/A
	 */
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Draws the foreground layer of the gui, such as text.
	 *
	 * @param  i	Unused; Parent requirement.
	 * @param  j	Unused; Parent requirement.
	 * @return N/A
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		if (selectedTabIndex != TABINDEX_SEARCH) {
			fontRenderer.drawString("Easy Crafting Table", 8, 6, 0x404040);
		} else {
			fontRenderer.drawString("Search:", 8, 6, 0x404040);
		}
	}

	/**
	 * Draws the background layer of the gui, such as textures.
	 *
	 * @param  f	Unused; Parent requirement.
	 * @param  i	Unused; Parent requirement.
	 * @param  j	Unused; Parent requirement.
	 * @return N/A
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		int texture = mc.renderEngine.getTexture("/net/lepko/minecraft/easycrafting/textures/easycraftinggui.png");
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		RenderHelper.enableGUIStandardItemLighting();
		this.mc.renderEngine.bindTexture(texture);
		// Tabs
		drawTabs();
		// Main GUI
		this.mc.renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		// Search field
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
		// Scrollbar
		this.mc.renderEngine.bindTexture(texture);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int xTex = this.maxScroll == 0 ? 12 : 0;
		this.drawTexturedModalRect(x + 156, y + 17 + (int) (this.scrollbarOffset * 73.0F), xTex, 240, 12, 16);
		// Selected tab
		drawTab(selectedTabIndex);
	}

	/**
	 * Draws the two tabs (Craft and Search)
	 *
	 * @param  N/A
	 * @return N/A
	 */
	private void drawTabs() {
		for (int i = 0; i < 2; i++) {
			if (i == selectedTabIndex) {
				continue;
			}
			drawTab(i);
		}
	}

	/**
	 * Draws the requested tab.
	 *
	 * @param  i	The index of the tab to be drawn
	 * @return N/A
	 */
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
		itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, iconItemStack, x, y);
		GL11.glDisable(GL11.GL_LIGHTING);
		itemRenderer.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	/**
	 * Intercepts keyboard key types, and updates the gui as required.
	 *
	 * @param  par1		The UNICODE character of the pressed key
	 * @param  par2		The keycode of the pressed key
	 * @return N/A
	 */
	@Override
	protected void keyTyped(char par1, int par2) {
		if (selectedTabIndex != TABINDEX_SEARCH) {
			if (Keyboard.isKeyDown(this.mc.gameSettings.keyBindChat.keyCode)) {
				this.switchToTab(1);
			} else {
				super.keyTyped(par1, par2);
			}
		} else {
			if (!this.func_82319_a(par2)) {
				if (this.searchField.textboxKeyTyped(par1, par2)) {
					updateSearch();
				} else {
					super.keyTyped(par1, par2);
				}
			}
		}
	}

	/**
	 * Handles scrolling of the scrollbar through the mousewheel.
	 *
	 * @param  N/A
	 * @return N/A
	 */
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

	/**
	 * Handles intercepting of mousecursor position for updating the gui.
	 *
	 * @param  mouseX	The x coordinate of the mousecursor.
	 * @param  mouseY	The y coordinate of the mousecursor.
	 * @param  par3		Unused; Parent requirement.
	 * @return N/A
	 */
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
		
		//Handle slot hover text
		for (int slotCounter = 0; slotCounter < 40; ++slotCounter)
        {
            Slot currentSlot = (Slot)this.inventorySlots.inventorySlots.get(slotCounter);

            if (this.isMouseOverSlot(currentSlot, mouseX, mouseY))
            {
				EasyRecipe currentRecipe = renderRecipeInSlot(slotCounter);
				if (currentRecipe != null) {
					String tooltip = currentRecipe.tooltipString();
					//TODO: Fix tooltip display. Do tooltip display as a grid of itemstacks.
					//TODO: Allow multiple levels of tooltip display.
				
					// GL11.glDisable(GL11.GL_LIGHTING);
					// GL11.glDisable(GL11.GL_DEPTH_TEST);
					// fontRenderer.drawString(tooltip, mouseX + 1, mouseY + 1, 0x404040);
					// this.drawGradientRect(mouseX, mouseY, mouseX + 128, mouseY + 64, -2130706433, -2130706433);
					// GL11.glEnable(GL11.GL_LIGHTING);
					// GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
            }
        }
	}
	
	/**
	 * Gets the recipe in the render slot specified.
	 *
	 * @param  	slotIndex	The position of the recipe to find
	 * @return 				The easyrecipe in the specified slot.
	 */
	public EasyRecipe renderRecipeInSlot(int slotIndex) {
		int offset = this.currentScroll * 8;
		if ( (slotIndex + offset < renderList.size()) && (slotIndex + offset >= 0) ) {
			return renderList.get(slotIndex + offset);
		} else {
			return null;
		}
	}

	/**
	 * Handles mouse-clicks for switching between tabs.
	 *
	 * @param  x		The x coordinate of the mousecursor.
	 * @param  y		The y coordinate of the mousecursor.
	 * @param  button	The mousebutton that was pressed.
	 * @return N/A
	 */
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

	/**
	 * Checks if the given x/y coordinates are within the boundaries of the given tab.
	 *
	 * @param  tabIndex		The index of the tab to check the boundaries for.
	 * @param  x			The x coordinate to check.
	 * @param  y			The y coordinate to check.
	 * @return 				True if the coordinates are within the tab boundaries, false if not.
	 */
	private boolean isOverTab(int tabIndex, int x, int y) {
		int width = 32;
		int height = 28;
		int tabX = this.guiLeft - 28 - 2;
		int tabY = this.guiTop + (tabIndex * (height + 1));
		return x > tabX && x < tabX + width && y > tabY && y < tabY + height;
	}
	
	/**
	 * Checks if the given x/y coordinates are within the boundaries of the given slot.
	 *
	 * @param  checkSlot	The slot to check the boundaries of.
	 * @param  x			The x coordinate to check.
	 * @param  y			The y coordinate to check.
	 * @return 				True if the coordinates are within the slot boundaries, false if not.
	 */
    private boolean isMouseOverSlot(Slot checkSlot, int x, int y)
    {
		int width = 16;
		int height = 16;
        x -= this.guiLeft;
        y -= this.guiTop;
        return x >= checkSlot.xDisplayPosition - 1 && x < checkSlot.xDisplayPosition + width + 1 && y >= checkSlot.yDisplayPosition - 1 && y < checkSlot.yDisplayPosition + height + 1;
    }

	/**
	 * Properly sets the variables for switching between the tabs
	 *
	 * @param  tabIndex		The tab index to switch to.
	 * @return N/A
	 */
	private void switchToTab(int tabIndex) {
		if (this.searchField != null) {
			if (tabIndex == TABINDEX_SEARCH) {
				this.searchField.setVisible(true);
				this.searchField.setCanLoseFocus(false);
				this.searchField.setFocused(true);
				this.searchField.setText("");
			} else {
				this.searchField.setVisible(false);
				this.searchField.setCanLoseFocus(true);
				this.searchField.setFocused(false);
			}
		}
		this.selectedTabIndex = tabIndex;
		updateSearch();
	}

	/**
	 * Updates the backrgound colour of the slot (search tab) based on ingredient availability
	 *
	 * @param  slot			The slot to update the background colour of.
	 * @param  canCraft		If the recipe in that slot can be crafted or not.
	 * @return N/A
	 */
	public void renderSlotBackColor(Slot slot, boolean canCraft) {
		ItemStack item = slot.getStack();

		int x = this.guiLeft + slot.xDisplayPosition;
		int y = this.guiTop + slot.yDisplayPosition;
		int w = 16;
		int h = 16;
		int color = canCraft ? 0x8000A000 : 0x80A00000;
		this.drawRect(x, y, x + w, y + h, color);
	}

	/**
	 * Updates the recipe renderlist based on the search text entered by the user.
	 *
	 * @param  N/A
	 * @return N/A
	 */
	private void updateSearch() {
		if (selectedTabIndex == TABINDEX_SEARCH) {
			ArrayList<EasyRecipe> all = this.tileEntity.recipesManager.getAllowedRecipes();
			ArrayList<EasyRecipe> list = new ArrayList<EasyRecipe>();
			String query = this.searchField.getText().toLowerCase();

			recipeLoop: for (int i = 0; i < all.size(); i++) {
				EasyRecipe r = all.get(i);
				List<String> itemProps = r.result.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
				for (int j = 0; j < itemProps.size(); j++) {
					if (itemProps.get(j).toLowerCase().contains(query)) {
						list.add(r);
						continue recipeLoop;
					}
				}
			}

			this.renderList = list;
		}
		this.currentScroll = 0;
		this.scrollbarOffset = 0.0F;
		TickHandlerClient.updateEasyCraftingOutput();
	}

	/**
	 * Recalculates the displayed recipelist (craft tab) or their backgrounds (search tab).
	 *
	 * @param  N/A
	 * @return N/A
	 */
	public void refreshCraftingOutput() {
		EntityPlayer player = (EntityPlayer) this.mc.thePlayer;
		craftableList = this.tileEntity.recipesManager.getCraftableRecipes(player.inventory);
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

	/**
	 * Determines if a recipe can be crafted or not  based on ingredient availability (search tab)
	 *
	 * @param  N/A
	 * @return N/A
	 */
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

	/**
	 * Updates the recipelist based on the scroll position.
	 *
	 * @param  scroll	How many rows down to scroll
	 * @return N/A
	 */
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

	/**
	 * Updates the recipelist based on the scroll position.
	 *
	 * @param  scrollOffset		How far down (percentage wise: 0.0 to 1.0) the slider is.
	 * @return N/A
	 */
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
}