package net.lepko.minecraft.easycrafting;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
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
			EntityPlayer player1 = (EntityPlayer) Minecraft.getMinecraft().thePlayer;
			if (player1.craftingInventory instanceof ContainerEasyCrafting) {
				ContainerEasyCrafting c = (ContainerEasyCrafting) player1.craftingInventory;
				c.refreshCraftingOutput(player1);
				updateEasyCraftingOutput = false;
				// System.out.println("Updated crafting output (tick)");
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
