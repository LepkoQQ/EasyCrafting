package net.lepko.minecraft.easycrafting;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;

import org.lwjgl.opengl.GL11;

public class GuiEasyCrafting extends GuiContainer {
	public GuiEasyCrafting(InventoryPlayer player_inventory, TileEntityEasyCrafting tile_entity) {
		super(new ContainerEasyCrafting(tile_entity, player_inventory));
		ySize = 236;
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
	}
}