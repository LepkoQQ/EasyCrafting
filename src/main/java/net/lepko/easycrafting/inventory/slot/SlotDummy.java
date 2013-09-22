package net.lepko.easycrafting.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDummy extends Slot {

    public SlotDummy(IInventory inv, int slotIndex, int x, int y) {
        super(inv, slotIndex, x, y);
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        return super.decrStackSize(0);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

    @Override
    public void putStack(ItemStack stack) {
        if (stack != null && stack.stackSize > getSlotStackLimit()) {
            stack = stack.copy();
            stack.stackSize = getSlotStackLimit();
        }
        super.putStack(stack);
    }

    public void clickSlot(int button, int modifier, ItemStack stack) {
        if (stack == null && getStack() != null) {
            putStack(null);
        } else if (stack != null) {
            putStack(stack.copy());
        }
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
