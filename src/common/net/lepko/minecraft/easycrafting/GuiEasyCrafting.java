package net.lepko.minecraft.easycrafting;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiEasyCrafting extends GuiContainer {
	protected int currentScroll = 0;
	private int maxScroll = 0;
	private float scrollbarOffset = 0;
	protected ArrayList<EasyRecipe> rl = new ArrayList<EasyRecipe>();
	private boolean wasClicking = false;
	private boolean isScrolling = false;

	public GuiEasyCrafting(InventoryPlayer player_inventory, TileEntityEasyCrafting tile_entity) {
		super(new ContainerEasyCrafting(tile_entity, player_inventory));
		ySize = 235;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		fontRenderer.drawString("Easy Crafting Table", 8, 6, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		int texture = mc.renderEngine.getTexture("/net/lepko/minecraft/easycrafting/textures/easycraftinggui.png");
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		// Scrollbar
		int xTex = this.maxScroll == 0 ? 12 : 0;
		this.drawTexturedModalRect(x + 156, y + 17 + (int) (this.scrollbarOffset * 73.0F), xTex, 240, 12, 16);
	}

	@Override
	public void handleMouseInput() {
		// Handle mouse scroll
		int delta = Mouse.getEventDWheel();
		if (delta == 0) {
			// Fix NEI auto clicking slots when mouse is being scrolled; only call super when mouse is not scrolling
			super.handleMouseInput();
		} else {
			this.maxScroll = (rl.size() / 8 + 1 - 5);
			if (this.maxScroll < 0) {
				this.maxScroll = 0;
			}

			if (rl.size() > 40) {
				if (delta > 0) {
					this.currentScroll--;
				} else if (delta < 0) {
					this.currentScroll++;
				}

				if (this.currentScroll < 0) {
					this.currentScroll = 0;
				} else if (this.currentScroll > this.maxScroll) {
					this.currentScroll = this.maxScroll;
				}

				this.scrollbarOffset = (float) this.currentScroll / (float) this.maxScroll;

				if (this.scrollbarOffset < 0.0F) {
					this.scrollbarOffset = 0.0F;
				} else if (this.scrollbarOffset > 1.0F) {
					this.scrollbarOffset = 1.0F;
				}

				ContainerEasyCrafting c = (ContainerEasyCrafting) this.inventorySlots;
				c.scrollTo(this.currentScroll, rl);
			}
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
			this.isScrolling = rl.size() > 40;
		} else if (!leftMouseDown) {
			this.isScrolling = false;
		}

		this.wasClicking = leftMouseDown;

		if (this.isScrolling) {
			this.scrollbarOffset = ((float) (mouseY - top) - 7.5F) / ((float) (bottom - top) - 15.0F);

			if (this.scrollbarOffset < 0.0F) {
				this.scrollbarOffset = 0.0F;
			} else if (this.scrollbarOffset > 1.0F) {
				this.scrollbarOffset = 1.0F;
			}

			this.currentScroll = (int) (this.scrollbarOffset * (float) this.maxScroll);

			if (this.currentScroll < 0) {
				this.currentScroll = 0;
			} else if (this.currentScroll > this.maxScroll) {
				this.currentScroll = this.maxScroll;
			}

			ContainerEasyCrafting c = (ContainerEasyCrafting) this.inventorySlots;
			c.scrollTo(this.currentScroll, rl);
		}

		super.drawScreen(mouseX, mouseY, par3);
	}

	public void refreshCraftingOutput() {
		EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().thePlayer;
		rl = Recipes.getCraftableItems(player.inventory);

		this.maxScroll = (rl.size() / 8 + 1 - 5);

		if (this.maxScroll < 0) {
			this.maxScroll = 0;
		}

		if (this.currentScroll > this.maxScroll) {
			this.currentScroll = this.maxScroll;
		}

		ContainerEasyCrafting c = (ContainerEasyCrafting) this.inventorySlots;
		c.scrollTo(this.currentScroll, rl);
	}
}