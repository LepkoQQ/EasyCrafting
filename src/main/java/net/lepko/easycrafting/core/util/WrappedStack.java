package net.lepko.easycrafting.core.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WrappedStack {

    public final List<Integer> oreIDs = ImmutableList.of(); //XXX: user oreIDs in WrappedRecipes?
    public final ItemStack stack;
    public final List<ItemStack> stacks;

    public WrappedStack(ItemStack stack) {
        this.stack = stack == null ? null : stack.copy();
        this.stacks = ImmutableList.of();
    }

    public WrappedStack(List<ItemStack> stacks) {
        this.stack = stacks.isEmpty() ? null : stacks.get(0);
        this.stacks = stacks;
    }

    @Override
    public String toString() {
        return "WrappedStack [oreIDs=" + oreIDs + ", stack=" + StackUtils.toString(stack) + ", nbt=" + (stack != null ? stack.stackTagCompound : "null") + "]\n";
    }

    @Override
    public int hashCode() {
        final int prime = 92821;
        int result = 1;
        result = prime * result + Item.getIdFromItem(stack.getItem());
        result = prime * result + StackUtils.rawDamage(stack);
        result = prime * result + stack.stackSize;
        result = prime * result + (stack.stackTagCompound == null ? 0 : stack.stackTagCompound.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        WrappedStack other = (WrappedStack) obj;
        return StackUtils.areIdentical(stack, other.stack);
    }

    public boolean isEqualItem(WrappedStack other) {
        if (this == other) {
            return true;
        }
        if (!StackUtils.areEqual(stack, other.stack)) {
            return false;
        }
        return StackUtils.rawDamage(stack) == StackUtils.rawDamage(other.stack);
    }

    public boolean isEquivalent(WrappedStack other) {
        if (other == null) {
            return false;
        }
        //
        List<Integer> ids = new ArrayList<Integer>(oreIDs);
        if (!ids.isEmpty()) {
            ids.retainAll(other.oreIDs);
            if (!ids.isEmpty()) {
                return true;
            }
        }
        //
        if (stack.getItem() != other.stack.getItem()) {
            return false;
        }
        if (!StackUtils.isDamageEquivalent(StackUtils.rawDamage(stack), StackUtils.rawDamage(other.stack))) {
            return false;
        }
        return true;
    }
}
