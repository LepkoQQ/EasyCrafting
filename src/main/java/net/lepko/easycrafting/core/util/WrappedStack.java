package net.lepko.easycrafting.core.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class WrappedStack {

    public int size;
    public final List<ItemStack> stacks;

    public WrappedStack(ItemStack stack) {
        if (stack == null) {
            throw new IllegalArgumentException("WrappedStack does not accept null ItemStacks!");
        }
        this.stacks = ImmutableList.of(stack);
        this.size = stack.stackSize;
    }

    public WrappedStack(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            throw new IllegalArgumentException("WrappedStack does not accept null or empty ItemStack Lists!");
        }
        this.stacks = ImmutableList.copyOf(stacks);
        this.size = 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("WrappedStack [");
        for (ItemStack stack : stacks) {
            if (stack != stacks.get(0)) {
                sb.append(", ");
            }
            sb.append(StackUtils.toString(stack));
        }
        return sb.append("]").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 92821;
        int result = 1;
        for (ItemStack stack : stacks) {
            result = prime * result + Item.getIdFromItem(stack.getItem());
            result = prime * result + StackUtils.rawDamage(stack);
            result = prime * result + stack.stackSize;
            result = prime * result + (stack.stackTagCompound == null ? 0 : stack.stackTagCompound.hashCode());
        }
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
        if (stacks.size() != other.stacks.size()) {
            return false;
        }
        for (int i = 0; i < stacks.size(); i++) {
            if (!StackUtils.areIdentical(stacks.get(i), other.stacks.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean equalsNoSize(WrappedStack other) {
        if (this == other) {
            return true;
        }
        if (stacks.size() != other.stacks.size()) {
            return false;
        }
        for (int i = 0; i < stacks.size(); i++) {
            if (!StackUtils.areIdenticalNoSize(stacks.get(i), other.stacks.get(i))) {
                return false;
            }
        }
        return true;
    }
}
