package net.lepko.minecraft.easycrafting.helpers;

import java.util.ArrayList;

import net.minecraft.src.ItemStack;

public class ItemHelper {

	/**
	 * Checks if two ItemStacks hold the same item.
	 * 
	 * @param is
	 * @param is2
	 * @return true if items match, false otherwise
	 */
	public static boolean doStacksHoldSameItem(ItemStack is, ItemStack is2) {
		if (is == null || is2 == null) {
			return false;
		}
		if (is.itemID != is2.itemID) {
			return false;
		}
		if (is.getItemDamage() != is2.getItemDamage()) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if two ItemStacks are holding the same item and if fromStack holds more or equal number of items than stackToTake.
	 * 
	 * @param fromStack
	 *            ItemStack against which to test
	 * @param stackToTake
	 *            ItemStack with which to test
	 * @return true if fromStack holds more or equal number of the same items than stackToTake, false otherwise
	 */
	public static boolean canTakeFromStack(ItemStack fromStack, ItemStack stackToTake) {
		if (fromStack == null || stackToTake == null) {
			return false;
		}
		if (fromStack.itemID != stackToTake.itemID) {
			return false;
		}
		if (stackToTake.getItemDamage() != -1 && fromStack.getItemDamage() != stackToTake.getItemDamage()) {
			return false;
		}
		if (fromStack.stackSize < stackToTake.stackSize) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if any of the ItemStacks in possibleStacks evaluate true using {@link #canTakeFromStack(ItemStack, ItemStack)}.
	 * 
	 * @param fromStack
	 *            ItemStack against which to test
	 * @param possibleStacks
	 *            ArrayList of ItemStacks with which to test
	 * @return the index of the first matching ItemStack in possibleStacks or -1 if none matched
	 */
	public static int canTakeFromStack(ItemStack fromStack, ArrayList possibleStacks) {
		for (int i = 0; i < possibleStacks.size(); i++) {
			if (possibleStacks.get(i) instanceof ItemStack) {
				if (canTakeFromStack(fromStack, (ItemStack) possibleStacks.get(i))) {
					return i;
				}
			}
		}
		return -1;
	}
}
