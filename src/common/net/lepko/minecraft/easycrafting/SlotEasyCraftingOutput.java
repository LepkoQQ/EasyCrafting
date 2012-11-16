package net.lepko.minecraft.easycrafting;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * Slot class for an easycraft output slot.
 * Functions as a placeholder to display recipe outputs, not allowing items to be placed into it except by code.
 * And triggers the correct recipe recheck calls when its held recipe is crafted.
 */
public class SlotEasyCraftingOutput extends Slot {

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
	public SlotEasyCraftingOutput(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	/**
	 * Returns that this slot is not a valid place to put any item.
	 *
	 * @param  par1ItemStack	The itemstack that wants to know if it can fit into this slot.
	 * @return 					Always returns false, meaning that this slot cannot accept the item.
	 */
	@Override
	public boolean isItemValid(ItemStack par1ItemStack) {
		return false;
	}

	/**
	 * Prevents any item from being put into this slot.
	 *
	 * @param  par1ItemStack	The itemstack that wants to be put into the slot.
	 * @return N/A
	 */
	@Override
	public void putStack(ItemStack par1ItemStack) {
		return;
	}

	/**
	 * Prevents slot contents from decreasing if user grabs the recipe from it.
	 *
	 * When something is grabbed from this slot by the player,
	 * then don't decrease the item count. The easycraft GUI code
	 * will handle that.
	 *
	 * @param  par1		The amount of items to be removed from the stack.
	 * @return 			The itemstack that was taken from the slot. Which will always be an empty itemstack.
	 */
	@Override
	public ItemStack decrStackSize(int par1) {
		return super.decrStackSize(0);
	}
}
