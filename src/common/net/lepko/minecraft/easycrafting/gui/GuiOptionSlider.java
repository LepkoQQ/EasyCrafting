//package net.lepko.minecraft.easycrafting.gui;
//
//import net.lepko.minecraft.easycrafting.helpers.EasyConfig.Option;
//import net.minecraft.client.Minecraft;
//
//import org.lwjgl.opengl.GL11;
//
//import cpw.mods.fml.common.Side;
//import cpw.mods.fml.common.asm.SideOnly;
//
//@SideOnly(Side.CLIENT)
//public class GuiOptionSlider extends GuiOptionButton {
//	public float sliderValue = 1.0F;
//	public boolean dragging = false;
//
//	public GuiOptionSlider(int id, int x, int y, Option option) {
//		super(id, x, y, 150, 20, option);
//		this.sliderValue = (float) option.getIntegerValue() / (float) option.getMax();
//		this.displayString = option.getDisplayName() + ": " + (int) (this.sliderValue * option.getMax());
//	}
//
//	@Override
//	protected int getHoverState(boolean isMouseOver) {
//		return 0;
//	}
//
//	@Override
//	protected void mouseDragged(Minecraft mc, int x, int y) {
//		if (this.drawButton) {
//			if (this.dragging) {
//				this.sliderValue = (float) (x - (this.xPosition + 4)) / (float) (this.width - 8);
//
//				if (this.sliderValue < 0.0F) {
//					this.sliderValue = 0.0F;
//				}
//
//				if (this.sliderValue > 1.0F) {
//					this.sliderValue = 1.0F;
//				}
//
//				int value = (int) (this.sliderValue * option.getMax());
//				option.setValue(value);
//				this.displayString = option.getDisplayName() + ": " + value;
//			}
//
//			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//			this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
//			this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
//		}
//	}
//
//	@Override
//	public boolean mousePressed(Minecraft mc, int x, int y) {
//		if (super.mousePressed(mc, x, y)) {
//			this.sliderValue = (float) (x - (this.xPosition + 4)) / (float) (this.width - 8);
//
//			if (this.sliderValue < 0.0F) {
//				this.sliderValue = 0.0F;
//			}
//
//			if (this.sliderValue > 1.0F) {
//				this.sliderValue = 1.0F;
//			}
//
//			int value = (int) (this.sliderValue * option.getMax());
//			option.setValue(value);
//			this.displayString = option.getDisplayName() + ": " + value;
//			this.dragging = true;
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//	@Override
//	public void mouseReleased(int x, int y) {
//		this.dragging = false;
//	}
// }
