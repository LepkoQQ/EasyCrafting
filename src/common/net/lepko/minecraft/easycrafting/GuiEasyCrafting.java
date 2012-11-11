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

public class GuiEasyCrafting extends GuiContainer {

	private static final int TABINDEX_CRAFTING = 0;
	private static final int TABINDEX_SEARCH = 1;

	private static int selectedTabIndex = TABINDEX_CRAFTING;

	protected int currentScroll = 0;
	private int maxScroll = 0;
	private float scrollbarOffset = 0;
	protected ArrayList<EasyRecipe> renderList = new ArrayList<EasyRecipe>();
	protected ArrayList<EasyRecipe> craftableList = new ArrayList<EasyRecipe>();
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
		if (selectedTabIndex != TABINDEX_SEARCH) {
			fontRenderer.drawString("Easy Crafting Table", 8, 6, 0x404040);
		} else {
			fontRenderer.drawString("Search:", 8, 6, 0x404040);
		}
	}

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
		itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, iconItemStack, x, y);
		GL11.glDisable(GL11.GL_LIGHTING);
		itemRenderer.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

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

	public void renderSlotBackColor(Slot slot, boolean canCraft) {
		ItemStack item = slot.getStack();

		int x = this.guiLeft + slot.xDisplayPosition;
		int y = this.guiTop + slot.yDisplayPosition;
		int w = 16;
		int h = 16;
		int color = canCraft ? 0x8000A000 : 0x80A00000;
		this.drawRect(x, y, x + w, y + h, color);
	}

	private void updateSearch() {
		if (selectedTabIndex == TABINDEX_SEARCH) {
			ArrayList<EasyRecipe> all = (ArrayList<EasyRecipe>) Recipes.getAllRecipes();
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

	public void refreshCraftingOutput() {
		EntityPlayer player = (EntityPlayer) this.mc.thePlayer;
		craftableList = Recipes.getCraftableRecipes(player.inventory);
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
}