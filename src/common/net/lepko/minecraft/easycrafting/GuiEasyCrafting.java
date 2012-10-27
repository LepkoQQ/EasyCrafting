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
	protected ArrayList<EasyRecipe> rl = new ArrayList<EasyRecipe>();

	public GuiEasyCrafting(InventoryPlayer player_inventory, TileEntityEasyCrafting tile_entity) {
		super(new ContainerEasyCrafting(tile_entity, player_inventory));
		ySize = 235;
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
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
		float scrollbarOffset = (float) this.currentScroll / (float) this.maxScroll;
		int xTex = this.maxScroll == 0 ? 12 : 0;
		this.drawTexturedModalRect(x + 156, y + 17 + (int) (scrollbarOffset * 73.0F), xTex, 240, 12, 16);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		int delta = Mouse.getEventDWheel();

		if (delta != 0) {
			int rlSize = rl.size();

			this.maxScroll = (rlSize / 8 + 1 - 5);
			if (this.maxScroll < 0) {
				this.maxScroll = 0;
			}

			if (rlSize > 40) {
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

				ContainerEasyCrafting c = (ContainerEasyCrafting) this.inventorySlots;
				c.scrollTo(this.currentScroll, rl);
			}
		}
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