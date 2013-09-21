package net.lepko.easycrafting.inventory;

import net.lepko.easycrafting.block.TileEntityAutoCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class ContainerAutoCrafting extends Container {

    public ContainerAutoCrafting(InventoryPlayer playerInventory, TileEntityAutoCrafting tileEntity) {
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
