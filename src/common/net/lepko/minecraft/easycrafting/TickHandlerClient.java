package net.lepko.minecraft.easycrafting;

import java.util.EnumSet;

import net.lepko.minecraft.easycrafting.block.GuiEasyCrafting;
import net.lepko.minecraft.easycrafting.helpers.VersionHelper;
import net.minecraft.src.GuiScreen;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient implements ITickHandler {

	private static boolean updateEasyCraftingOutput = false;
	private static boolean showUpdateInChat = true;
	private static int count = 2;

	public static void updateEasyCraftingOutput() {
		updateEasyCraftingOutput(2);
	}

	public static void updateEasyCraftingOutput(int c) {
		updateEasyCraftingOutput = true;
		count = c;
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (updateEasyCraftingOutput && count <= 0 && type.equals(EnumSet.of(TickType.CLIENT))) {
			GuiScreen gs = FMLClientHandler.instance().getClient().currentScreen;
			if (gs != null && gs instanceof GuiEasyCrafting) {
				GuiEasyCrafting gec = (GuiEasyCrafting) gs;
				gec.refreshCraftingOutput();
				updateEasyCraftingOutput = false;
				if (showUpdateInChat) {
					VersionHelper.printToChat();
					showUpdateInChat = false;
				}
			}
		} else if (count > 0) {
			count--;
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() {
		return VersionHelper.MOD_ID + "-" + this.getClass().getSimpleName();
	}
}
