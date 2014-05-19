package net.lepko.easycrafting.core.inventory;

import net.lepko.easycrafting.core.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.core.inventory.slot.SlotEasyCraftingOutput;
import net.lepko.easycrafting.core.inventory.slot.SlotInterceptor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEasyCrafting extends Container {

    private TileEntityEasyCrafting tileEntity;

    public ContainerEasyCrafting(InventoryPlayer playerInventory, TileEntityEasyCrafting tileEntity) {
        this.tileEntity = tileEntity;
        int offset2 = 5 * 18 + 4;
        int offset = offset2 + 2 * 18 + 4;
        int offset3 = offset + 3 * 18 + 5;

        int count = 0;

        // Crafting output slots
        for (int g = 0; g < 5; ++g) {
            for (int h = 0; h < 8; ++h) {
                addSlotToContainer(new SlotEasyCraftingOutput(tileEntity, count++, 8 + h * 18, 18 + g * 18));
            }
        }

        // Table inventory slots
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(tileEntity, count++, 8 + j * 18, 18 + i * 18 + offset2));
            }
        }

        // Player inventory
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 9; ++l) {
                addSlotToContainer(new SlotInterceptor(playerInventory, l + k * 9 + 9, 8 + l * 18, 18 + k * 18 + offset));
            }
        }

        // Player hotbar
        for (int m = 0; m < 9; ++m) {
            addSlotToContainer(new SlotInterceptor(playerInventory, m, 8 + m * 18, 18 + offset3));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileEntity.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot_index) {
        ItemStack stack = null;
        Slot slot_object = (Slot) inventorySlots.get(slot_index);

        if (slot_object != null && slot_object.getHasStack()) {
            ItemStack stack_in_slot = slot_object.getStack();
            stack = stack_in_slot.copy();

            if (slot_index < 40) {
                return null;
            } else if (slot_index >= 40 && slot_index <= 57) {
                if (!mergeItemStack(stack_in_slot, 58, inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!mergeItemStack(stack_in_slot, 40, 58, false)) {
                return null;
            }

            if (stack_in_slot.stackSize == 0) {
                slot_object.putStack(null);
            } else {
                slot_object.onSlotChanged();
            }
        }

        return stack;
    }

    @Override
    public ItemStack slotClick(int slot_index, int mouse_button, int modifier, EntityPlayer player) {
        if (slot_index >= 0 && inventorySlots.get(slot_index) instanceof SlotInterceptor) {
            if (!((Slot) inventorySlots.get(slot_index)).getHasStack() && player.inventory.getItemStack() == null) {
                return null;
            }
        }
        return super.slotClick(slot_index, mouse_button, modifier, player);
    }
}