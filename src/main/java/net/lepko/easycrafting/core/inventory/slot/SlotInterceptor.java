package net.lepko.easycrafting.core.inventory.slot;

import net.lepko.easycrafting.core.recipe.RecipeChecker;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SlotInterceptor extends Slot {

    public SlotInterceptor(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    @SideOnly (Side.CLIENT)
    public void onSlotChanged() {
        super.onSlotChanged();
        RecipeChecker.INSTANCE.requested = true;
    }
}
