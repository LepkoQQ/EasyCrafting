package net.lepko.easycrafting.core.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotOutput extends Slot {

    public SlotOutput(IInventory inv, int slotIndex, int x, int y) {
        super(inv, slotIndex, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return false;
    }
}
