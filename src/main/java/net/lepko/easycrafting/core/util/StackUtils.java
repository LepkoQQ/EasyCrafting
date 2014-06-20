package net.lepko.easycrafting.core.util;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class StackUtils {

    /**
     * Checks if the two provided ItemStacks can stack together.
     *
     * @return -1 if not stackable or any stack is null, the number of items leftover in the second stack otherwise
     */
    public static int canStack(ItemStack first, ItemStack second) {
        if (first != null && second != null) {
            if (first.isStackable() && second.isStackable() && areEqualNoSize(first, second)) {
                int i = first.stackSize + second.stackSize - first.getMaxStackSize();
                return i < 0 ? 0 : i;
            }
        }
        return -1;
    }

    /**
     * Check if two stacks are strictly identical.
     */
    public static boolean areIdentical(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return first == second;
        }
        return areIdenticalNoSize(first, second) && first.stackSize != second.stackSize;
    }

    /**
     * Check if two stacks are strictly identical. Does NOT check sizes.
     */
    public static boolean areIdenticalNoSize(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return first == second;
        }
        if (first.getItem() != second.getItem()) {
            return false;
        }
        if (rawDamage(first) != rawDamage(second) || !areNBTsEqual(first, second)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if two ItemStack items are equal. Does NOT check sizes or NBT!
     */
    public static boolean areEqualNoSizeNoNBT(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return first == second;
        }
        if (first.getItem() != second.getItem()) {
            return false;
        }
        if (first.getHasSubtypes() && first.getItemDamage() != second.getItemDamage()) {
            return false;
        }
        return true;
    }

    /**
     * Checks if two ItemStacks are equal. Does NOT check sizes!
     */
    public static boolean areEqualNoSize(ItemStack first, ItemStack second) {
        return areEqualNoSizeNoNBT(first, second) && areNBTsEqual(first, second);
    }

    /**
     * Checks if two ItemStack are equivalent for vanilla crafting
     */
    public static boolean areCraftingEquivalent(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return first == second;
        }
        if (first.getItem() != second.getItem()) {
            return false;
        }
        if (first.getHasSubtypes() && !isDamageEquivalent(first.getItemDamage(), second.getItemDamage())) {
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
     * Returns the raw value: stack.itemDamage
     */
    public static int rawDamage(ItemStack stack) {
        return Items.arrow.getDamage(stack);
    }

    public static boolean areNBTsEqual(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return first == second;
        }
        if (first.stackTagCompound == null || first.stackTagCompound.hasNoTags()) {
            return second.stackTagCompound == null || second.stackTagCompound.hasNoTags();
        }
        if (second.stackTagCompound == null || second.stackTagCompound.hasNoTags()) {
            return false;
        }
        return first.stackTagCompound.equals(second.stackTagCompound);
    }

    public static String toString(ItemStack stack) {
        if (stack == null) {
            return "ItemStack [null]";
        }
        String name = stack.getItem() == null ? "null" : stack.getItem().getUnlocalizedName(stack);
        String nbt = stack.stackTagCompound == null ? "null" : stack.stackTagCompound.toString();
        return String.format("ItemStack [item=%s, meta=%d, size=%d, name=%s, nbt=%s]", stack.getItem(), rawDamage(stack), stack.stackSize, name, nbt);
    }

    /**
     * Assumes List contains an ItemStack or a List of ItemStacks with at least one entry!
     */
    @SuppressWarnings("unchecked")
    public static List<WrappedStack> collateStacks(List<? extends Object> inputs) {
        List<WrappedStack> collated = new ArrayList<WrappedStack>();
        inputs:
        for (Object o : inputs) {
            WrappedStack ws;
            if (o instanceof List) {
                ws = new WrappedStack(((List<ItemStack>) o));
            } else {
                ws = new WrappedStack((ItemStack) o);
            }
            for (WrappedStack stack : collated) {
                if (stack.equalsNoSize(ws)) {
                    stack.size++;
                    continue inputs;
                }
            }
            collated.add(ws);
        }
        return collated;
    }

    public static ItemStack copyStack(ItemStack stack, int size) {
        if (stack != null) {
            ItemStack stackCopy = stack.copy();
            stackCopy.stackSize = size;
            return stackCopy;
        }
        return null;
    }
}
