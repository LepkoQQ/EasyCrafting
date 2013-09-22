package net.lepko.easycrafting.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotDummyResult extends SlotDummy {

    public SlotDummyResult(IInventory inv, int slotIndex, int x, int y) {
        super(inv, slotIndex, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return false;
    }

    @Override
    public void clickSlot(int button, int modifier, ItemStack stack) {
        if (button == 1) {
            for (int i = 0; i < 9; i++) {
                inventory.setInventorySlotContents(i, null);
            }
            putStack(null);
        }
    }

    @Override
    public int getSlotStackLimit() {
        return 64;
    }
}
