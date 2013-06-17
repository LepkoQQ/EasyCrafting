package net.lepko.easycrafting.inventory;

import net.lepko.easycrafting.block.SlotEasyCraftingOutput;
import net.lepko.easycrafting.block.SlotInterceptor;
import net.lepko.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.inventory.gui.GuiEasyCrafting;
import net.lepko.easycrafting.network.PacketHandler;
import net.lepko.easycrafting.network.packet.EasyPacket;
import net.lepko.easycrafting.network.packet.PacketEasyCrafting;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEasyCrafting extends Container {

    protected TileEntityEasyCrafting tileEntity;

    // TODO: remove
    public GuiEasyCrafting gui;

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
        if (slot_index >= 0 && inventorySlots.get(slot_index) instanceof SlotEasyCraftingOutput) {
            return slotClickEasyCraftingOutput(slot_index, mouse_button, modifier, player);
        } else {
            return super.slotClick(slot_index, mouse_button, modifier, player);
        }
    }

    private ItemStack slotClickEasyCraftingOutput(int slot_index, int mouse_button, int modifier, EntityPlayer player) {
        if (!Proxy.proxy.isClient()) {
            return null;
        }

        if (gui == null) {
            return null;
        }

        if (mouse_button != 0 && mouse_button != 1 || modifier != 0 && modifier != 1) {
            return null;
        }

        Slot clicked_slot = (Slot) inventorySlots.get(slot_index);
        if (clicked_slot == null) {
            return null;
        }

        ItemStack stack_in_slot = clicked_slot.getStack();
        if (stack_in_slot == null) {
            return null;
        }

        ItemStack stack_in_hand = player.inventory.getItemStack();

        ItemStack return_stack = null;
        int return_size = 0;

        // TODO: Shift clicking to transfer stack to inventory

        if (stack_in_hand == null) {
            return_stack = stack_in_slot.copy();
            return_size = stack_in_slot.stackSize;
        } else if (stack_in_slot.itemID == stack_in_hand.itemID && stack_in_hand.getMaxStackSize() >= stack_in_slot.stackSize + stack_in_hand.stackSize && (!stack_in_slot.getHasSubtypes() || stack_in_slot.getItemDamage() == stack_in_hand.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack_in_slot, stack_in_hand)) {
            return_stack = stack_in_slot.copy();
            return_size = stack_in_slot.stackSize + stack_in_hand.stackSize;
        }

        if (return_stack != null) {
            EasyRecipe recipe = RecipeHelper.getValidRecipe(gui, slot_index, return_stack);
            if (recipe != null) {
                boolean isRightClick = mouse_button != 0;

                EasyPacket packet = new PacketEasyCrafting(recipe, isRightClick);
                PacketHandler.sendPacket(packet);

                if (isRightClick) { // Right click; craft until max stack
                    int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(stack_in_slot, stack_in_hand);
                    int timesCrafted = RecipeHelper.canCraft(recipe, player.inventory, RecipeHelper.getAllRecipes(), false, maxTimes, ConfigHandler.MAX_RECURSION);
                    if (timesCrafted > 0) {
                        return_stack.stackSize = return_size + (timesCrafted - 1) * stack_in_slot.stackSize;
                        player.inventory.setItemStack(return_stack);
                        return return_stack;
                    }
                } else { // Left click; craft once
                    return_stack.stackSize = return_size;
                    player.inventory.setItemStack(return_stack);
                    return return_stack;
                }
            }
        }

        return null;
    }
}