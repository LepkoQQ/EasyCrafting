package net.lepko.minecraft.easycrafting;

import java.util.EnumSet;

import net.minecraft.src.GuiScreen;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class handles updating the GUI if something occured that requires a recheck.
 * (Such as an inventory change.)
 */
public class TickHandlerClient implements ITickHandler {

	/** Is an update of some easycraft GUI client required? */
	private static boolean updateEasyCraftingOutput = false;
	/** The number of ticks we delay the update by */
	private static int count = 2;

	/**
	 * Flags the craftable recipelist to update, delayed by 2 ticks
	 *
	 * @param  N/A
	 * @return N/A
	 */
	public static void updateEasyCraftingOutput() {
		updateEasyCraftingOutput(2);
	}

	/**
	 * Flags the craftable recipelist to update, delayed by c ticks
	 *
	 * @param  c	How many ticks to delay before flagging the recipelist to update.
	 * @return N/A
	 */
	public static void updateEasyCraftingOutput(int c) {
		updateEasyCraftingOutput = true;
		count = c;
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	/**
	 * Updates the available recipelist if the update flag is set.
	 *
	 * @param  type			What type of tick this is (client or server). Not used; Requirement from ITickHandler
	 * @param  tickData		The data associated with this tick. Not used; Requirement from ITickHandler
	 * @return N/A
	 */
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (updateEasyCraftingOutput && count <= 0 && type.equals(EnumSet.of(TickType.CLIENT))) {
			GuiScreen gs = FMLClientHandler.instance().getClient().currentScreen;
			if (gs instanceof GuiEasyCrafting) {
				GuiEasyCrafting gec = (GuiEasyCrafting) gs;
				gec.refreshCraftingOutput();
				updateEasyCraftingOutput = false;
			}
		} else if (count > 0) {
			count--;
		}
	}

	/**
	 * Returns that this is client ticktype.
	 *
	 * @param  N/A
	 * @return 		Always returns that this is a client ticktype.
	 */
	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	/**
	 * Returns the label for this tickhandler.
	 *
	 * @param  N/A
	 * @return 		Always returns a blank label.
	 */
	@Override
	public String getLabel() {
		return null;
	}
}
