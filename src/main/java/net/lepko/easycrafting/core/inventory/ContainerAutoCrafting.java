package net.lepko.easycrafting.core.inventory;

import net.lepko.easycrafting.core.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.core.inventory.slot.SlotDummy;
import net.lepko.easycrafting.core.inventory.slot.SlotDummyResult;
import net.lepko.easycrafting.core.inventory.slot.SlotOutput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerAutoCrafting extends Container {

    public final TileEntityAutoCrafting tileEntity;

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
                addSlotToContainer(new SlotOutput(tileEntity, count++, 8 + 5 * 18 + l * 18, 18 + m * 18 + tableInvOffset));
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
            tileEntity.scheduledRecipeCheck = true;
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

    @Override
    public void addCraftingToCrafters(ICrafting player) {
        super.addCraftingToCrafters(player);

        player.sendProgressBarUpdate(this, 0, tileEntity.getMode().ordinal());
    }

    private int oldMode = -1;

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object player : crafters) {
            if (tileEntity.getMode().ordinal() != oldMode) {
                ((ICrafting) player).sendProgressBarUpdate(this, 0, tileEntity.getMode().ordinal());
            }
        }

        oldMode = tileEntity.getMode().ordinal();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        if (id == 0) {
            tileEntity.setMode(value);
        }
    }
}
