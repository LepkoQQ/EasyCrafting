package net.lepko.minecraft.easycrafting.gui;

import net.lepko.minecraft.easycrafting.helpers.EasyConfig;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOptionsScreen extends GuiScreen {

	private String screenTitle = "Easy Crafting Options";

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 0xFFFFFF);
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 200) {
				EasyConfig.saveConfig();
				this.mc.displayGuiScreen(null);
			}
		}
	}

	@Override
	public void initGui() {
		int i = 0;
		this.controlList.add(new GuiOptionSlider(50, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i++ >> 1), EasyConfig.RECIPE_RECURSION));

		this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, "Save"));
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
