package net.lepko.util;

import net.minecraft.item.ItemStack;

public class StackUtil {

    public static boolean canStack(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.getMaxStackSize() < first.stackSize + second.stackSize) {
            return false;
        }
        if (first.itemID != second.itemID) {
            return false;
        }
        if (first.getHasSubtypes() && first.getItemDamage() != second.getItemDamage()) {
            return false;
        }
        if (!ItemStack.areItemStackTagsEqual(first, second)) {
            return false;
        }
        return true;
    }
}
