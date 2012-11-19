package net.lepko.minecraft.easycrafting;

import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * Slot class for a slot for which any interaction with it is intercepted (tracked so the code knows it happened).
 * 
 * This is required so that the easycraft object can know to recalculate the available recipelist based on
 * the change in available ingredients.
 */
public class SlotInterceptor extends Slot {

	/**
	 * Creates and instance of the class.
	 * @see http://jd.minecraftforge.net/src-html/net/minecraft/src/Slot.html
	 *
	 * @param  par1iInventory	The inventory in which the slot is created.
	 * @param  par2				The slot index in the inventory.
	 * @param  par3				The x display coordinate of the slot.
	 * @param  par4				The y display coordinate of the slot.
	 * @return N/A
	 */
	public SlotInterceptor(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	/**
	 * Handles changes to the slot contents.
	 * 
	 * This is where we catch the event of the contents changing, and inform easycraft.
	 *
	 * @param  N/A
	 * @return N/A
	 */
	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		TickHandlerClient.updateEasyCraftingOutput();
	}
}
