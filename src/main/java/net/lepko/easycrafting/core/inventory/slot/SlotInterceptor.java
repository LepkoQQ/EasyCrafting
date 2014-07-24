package net.lepko.easycrafting.core.inventory.slot;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.lepko.easycrafting.core.recipe.RecipeChecker;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotInterceptor extends Slot {

    public SlotInterceptor(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            onSlotChangedClient();
        }
    }

    @SideOnly(Side.CLIENT)
    private void onSlotChangedClient() {
        RecipeChecker.INSTANCE.requested = true;
    }
}
