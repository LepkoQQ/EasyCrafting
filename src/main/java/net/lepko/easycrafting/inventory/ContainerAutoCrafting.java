package net.lepko.easycrafting.inventory;

import net.lepko.easycrafting.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.inventory.slot.SlotDummy;
import net.lepko.easycrafting.inventory.slot.SlotDummyResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAutoCrafting extends Container {

    private final TileEntityAutoCrafting tileEntity;

    public ContainerAutoCrafting(InventoryPlayer playerInventory, TileEntityAutoCrafting tileEntity) {
        this.tileEntity = tileEntity;

        int count = 0;

        int craftingInvOffset = -1;
        // Crafting inventory slots
        for (int q = 0; q < 3; q++) {
            for (int p = 0; p < 3; p++) {
                addSlotToContainer(new SlotDummy(tileEntity, count++, 8 + 2 * 18 + p * 18, 18 + q * 18 + craftingInvOffset));
            }
        }
        // Crafting output slot
        addSlotToContainer(new SlotDummyResult(tileEntity, count++, 8 + 2 * 18 + 81, 18 + 1 * 18 + craftingInvOffset));

        int tableInvOffset = 66;
        // Table input slots
        for (int o = 0; o < 2; o++) {
            for (int n = 0; n < 4; n++) {
                addSlotToContainer(new Slot(tileEntity, count++, 8 + n * 18, 18 + o * 18 + tableInvOffset));
            }
        }
        // Table output slots
        for (int m = 0; m < 2; m++) {
            for (int l = 0; l < 4; l++) {
                addSlotToContainer(new Slot(tileEntity, count++, 8 + 5 * 18 + l * 18, 18 + m * 18 + tableInvOffset));
            }
        }

        int playerInvOffset = 106;
        // Player inventory
        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 18 + k * 18 + playerInvOffset));
            }
        }
        // Player hotbar
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 18 + playerInvOffset + 3 * 18 + 5));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileEntity.isUseableByPlayer(player);
    }

    @Override
    public ItemStack slotClick(int slotIndex, int button, int modifier, EntityPlayer player) {
        if (slotIndex >= 0 && slotIndex < inventorySlots.size() && getSlot(slotIndex) instanceof SlotDummy) {
            ((SlotDummy) getSlot(slotIndex)).clickSlot(button, modifier, player.inventory.getItemStack());
            return null;
        }
        return super.slotClick(slotIndex, button, modifier, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack stack = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            if (slotIndex < 10) {
                return null;
            } else if (slotIndex >= 10 && slotIndex < 26) {
                if (!mergeItemStack(slotStack, 26, inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!mergeItemStack(slotStack, 10, 18, false)) {
                return null;
            }

            if (slotStack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }
        return stack;
    }
}
