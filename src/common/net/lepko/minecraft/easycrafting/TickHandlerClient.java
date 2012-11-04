package net.lepko.minecraft.easycrafting;

import java.util.EnumSet;

import net.minecraft.src.GuiScreen;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient implements ITickHandler {

	public static boolean updateEasyCraftingOutput = false;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (updateEasyCraftingOutput && type.equals(EnumSet.of(TickType.CLIENT))) {
			GuiScreen gs = FMLClientHandler.instance().getClient().currentScreen;
			if (gs instanceof GuiEasyCrafting) {
				GuiEasyCrafting gec = (GuiEasyCrafting) gs;
				gec.refreshCraftingOutput();
				updateEasyCraftingOutput = false;
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() {
		return null;
	}
}
