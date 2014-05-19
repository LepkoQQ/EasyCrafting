package net.lepko.easycrafting.core.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotEasyCraftingOutput extends Slot {

    public SlotEasyCraftingOutput(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public void putStack(ItemStack stack) {
        return;
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        return super.decrStackSize(0);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }
}
