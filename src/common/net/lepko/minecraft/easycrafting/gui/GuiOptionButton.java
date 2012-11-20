package net.lepko.minecraft.easycrafting.gui;

import net.lepko.minecraft.easycrafting.helpers.EasyConfig.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOptionButton extends GuiButton {

	protected Option option;

	public GuiOptionButton(int id, int x, int y, int w, int h, Option option) {
		super(id, x, y, w, h, option.getDisplayName());
		this.option = option;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) {
		super.drawButton(mc, x, y);

		// Draw hover text
		if (this.field_82253_i) {
			FontRenderer fontRenderer = mc.fontRenderer;
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			String s = option.getDescription();
			int width = fontRenderer.getStringWidth(s);
			int rows = 1;
			if (width > 250) {
				rows = width / 250 + 1;
				width = 250;
			}
			int posX = x + 5;
			int posY = y + 12;
			int height = 8 * rows + (rows - 1) * 1;
			this.zLevel = 300.0F;
			int color = 0xFF000000;
			this.drawGradientRect(posX - 4, posY - 5, posX + width + 4, posY - 4, color, color);
			this.drawGradientRect(posX - 4, posY + height + 4, posX + width + 4, posY + height + 5, color, color);
			this.drawGradientRect(posX - 5, posY - 4, posX + width + 5, posY + height + 4, color, color);
			int clr1 = 0x505000FF;
			int clr2 = (clr1 & 0xFEFEFE) >> 1 | clr1 & -0xFEFEFE;
			this.drawGradientRect(posX - 3, posY - 4, posX + width + 3, posY - 3, clr1, clr2);
			this.drawGradientRect(posX - 3, posY + height + 3, posX + width + 3, posY + height + 4, clr1, clr2);
			this.drawGradientRect(posX - 4, posY - 4, posX - 3, posY + height + 4, clr1, clr2);
			this.drawGradientRect(posX + width + 3, posY - 4, posX + width + 4, posY + height + 4, clr1, clr2);
			fontRenderer.drawSplitString(s, posX, posY, 250, 0xFEFEFE);
			this.zLevel = 0.0F;
		}
	}
}
