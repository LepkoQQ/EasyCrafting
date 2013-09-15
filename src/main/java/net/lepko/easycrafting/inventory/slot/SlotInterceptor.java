package net.lepko.easycrafting.inventory.slot;

import net.lepko.easycrafting.handlers.TickHandlerClient;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotInterceptor extends Slot {

    public SlotInterceptor(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        TickHandlerClient.scheduleRecipeUpdate();
    }
}
