package net.lepko.easycrafting.helpers;

import java.util.ArrayList;
import java.util.List;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryHelper {

    /**
     * Check if an inventory contains a single item matching the supplied EasyItemStack.
     * 
     * @param inventory - inventory to check
     * @param eis - item to find
     * @return slot index of item in inventory, -1 if not found
     */
    public static int isItemInInventory(IInventory inventory, EasyItemStack eis) {
        int invSize = inventory.getSizeInventory();
        if (inventory instanceof InventoryPlayer) {
            invSize -= 4; // Remove Armor slots
        }
        for (int i = 0; i < invSize; i++) {
            if (eis.equalsItemStack(inventory.getStackInSlot(i), true)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check if an inventory contains any of the items in the supplied list of ItemStacks
     * 
     * @param inventory - inventory to check
     * @param ingredients - list of items
     * @return slot index of the first item found, -1 if none found
     * @see #isItemInInventory(IInventory, EasyItemStack)
     */
    public static int isItemInInventory(IInventory inventory, List<ItemStack> ingredients) {
        for (ItemStack is : ingredients) {
            int slot = isItemInInventory(inventory, EasyItemStack.fromItemStack(is));
            if (slot != -1) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Get the first empty slot in the inventory.
     * 
     * @param inventory - inventory to check
     * @return slot index of the first empty slot, -1 if none found
     */
    public static int getEmptySlot(IInventory inventory) {
        int invSize = inventory.getSizeInventory();
        if (inventory instanceof InventoryPlayer) {
            invSize -= 4; // Remove Armor slots
        }
        for (int i = 0; i < invSize; i++) {
            if (inventory.getStackInSlot(i) == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Store the contents of an inventory in a list.
     * 
     * @param inventory - inventory to store
     */
    public static List<ItemStack> storeContents(IInventory inventory) {
        List<ItemStack> copy = new ArrayList<ItemStack>(inventory.getSizeInventory());
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            copy.add(i, ItemStack.copyItemStack(inventory.getStackInSlot(i)));
        }
        return copy;
    }

    /**
     * Replace the contents of an inventory with the ones of the list. List and inventory sizes must be the same!
     * 
     * @param inventory - inventory to replace
     * @param list - list of itemstacks
     */
    public static void setContents(IInventory inventory, List<ItemStack> list) {
        if (inventory.getSizeInventory() != list.size()) {
            return;
        }
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            inventory.setInventorySlotContents(i, ItemStack.copyItemStack(list.get(i)));
        }
    }

    /**
     * Add the specified itemstack to inventory.
     * 
     * @param inventory - inventory to add to
     * @param itemstack - item to add
     */
    public static boolean addItemToInventory(IInventory inventory, ItemStack itemstack) {
        List<ItemStack> contents = storeContents(inventory);
        int invSize = inventory.getSizeInventory();
        if (inventory instanceof InventoryPlayer) {
            invSize -= 4; // Remove Armor slots
        }
        int maxStack = Math.min(inventory.getInventoryStackLimit(), itemstack.getMaxStackSize());
        for (int i = 0; i < invSize; i++) {
            if (ItemStack.areItemStacksEqual(itemstack, inventory.getStackInSlot(i))) {
                ItemStack is = inventory.getStackInSlot(i);
                if (is.stackSize >= maxStack) {
                    continue;
                }
                if ((is.stackSize + itemstack.stackSize) <= maxStack) {
                    is.stackSize += itemstack.stackSize;
                    return true;
                } else {
                    itemstack.stackSize -= (maxStack - is.stackSize);
                    is.stackSize = maxStack;
                }
            }
        }
        while (true) {
            int slot = getEmptySlot(inventory);
            if (slot != -1) {
                if (itemstack.stackSize <= maxStack) {
                    inventory.setInventorySlotContents(slot, itemstack.copy());
                    return true;
                } else {
                    ItemStack is = itemstack.copy();
                    itemstack.stackSize -= maxStack;
                    is.stackSize = maxStack;
                    inventory.setInventorySlotContents(slot, is);
                }
            } else {
                break;
            }
        }
        setContents(inventory, contents);
        return false;
    }

    /**
     * Decreases the stack size in the inventoryIndex slot in the inventory by 1 and gives back any container items (bucket, etc.). Also adds the
     * consumed item to the usedIngredients list.
     * 
     * @param inventory - inventory to take from
     * @param inventoryIndex - slot index to take from
     * @param usedIngredients - a list to which the consumed ingredient will be added
     * @return true if successful, false if there is no space for container items or cannot take from stack
     */
    public static boolean consumeItemForCrafting(IInventory inventory, int inventoryIndex, List<ItemStack> usedIngredients) {
        ItemStack stack = inventory.decrStackSize(inventoryIndex, 1);
        if (stack != null) {
            if (stack.getItem().hasContainerItem()) {
                ItemStack containerStack = stack.getItem().getContainerItemStack(stack);
                if (containerStack.isItemStackDamageable() && containerStack.getItemDamage() > containerStack.getMaxDamage()) {
                    containerStack = null;
                }
                if (containerStack != null && !addItemToInventory(inventory, containerStack)) {
                    if (inventory.getStackInSlot(inventoryIndex) != null) {
                        inventory.getStackInSlot(inventoryIndex).stackSize++;
                    } else {
                        inventory.setInventorySlotContents(inventoryIndex, stack);
                    }
                    return false;
                }
            }
            usedIngredients.add(stack);
            return true;
        }
        return false;
    }
}
