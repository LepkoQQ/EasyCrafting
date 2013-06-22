package net.lepko.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class StackUtils {

    /**
     * Checks if the two provided ItemStacks can stack together.
     * 
     * @return -1 if not stackable or any stack is null, the number of items leftover in the second stack otherwise
     */
    public static int canStack(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            if (first.isStackable() && second.isStackable() && first.itemID == second.itemID) {
                if (!first.getHasSubtypes() || first.getItemDamage() == second.getItemDamage()) {
                    if (ItemStack.areItemStackTagsEqual(first, second)) {
                        int i = first.stackSize + second.stackSize - first.getMaxStackSize();
                        return i < 0 ? 0 : i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Checks if two ItemStacks are equal. Does NOT check sizes!
     */
    public static boolean areEqual(ItemStack first, ItemStack second) {
        if (first == null) {
            return second == null;
        }
        if (second == null || first.itemID != second.itemID) {
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

    /**
     * Checks if two ItemStacks are equivalent (Same OreDictionary ID or damage wildcard). Does NOT check sizes or NBT!
     */
    public static boolean areEquivalent(ItemStack first, ItemStack second) {
        if (first == null) {
            return second == null;
        }
        if (second == null) {
            return false;
        }
        int oreID = OreDictionary.getOreID(first);
        if (oreID != -1 && oreID == OreDictionary.getOreID(second)) {
            return true;
        }
        if (first.itemID != second.itemID) {
            return false;
        }
        if (!isDamageEquivalent(first.getItemDamage(), second.getItemDamage())) {
            return false;
        }
        return true;
    }

    /**
     * Check if the two damage values are the same or any of them is a wildcard.
     */
    public static boolean isDamageEquivalent(int first, int second) {
        if (first == second || first == OreDictionary.WILDCARD_VALUE || second == OreDictionary.WILDCARD_VALUE) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a WrappedStack and an ItemStack have equal NBT.
     */
    // public static boolean equalNBT(WrappedStack ws, ItemStack is) {
    // if (ws == null || ws.nbt == null) {
    // return is == null || is.stackTagCompound == null;
    // }
    // if (is == null || is.stackTagCompound == null) {
    // return false;
    // }
    // return ws.nbt.equals(is.stackTagCompound);
    // }
}
