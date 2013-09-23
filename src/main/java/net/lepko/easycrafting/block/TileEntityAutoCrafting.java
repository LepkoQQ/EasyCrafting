package net.lepko.easycrafting.block;

import java.util.Locale;

import net.lepko.easycrafting.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityAutoCrafting extends TileEntity implements ISidedInventory {

    public enum Mode {
        PULSE,
        ALWAYS,
        POWERED,
        UNPOWERED;

        public final String tooltip;

        Mode() {
            tooltip = String.format("mode.easycrafting:%s.tooltip", this.toString().toLowerCase(Locale.ENGLISH));
        }
    }

    private int UPDATE_INTERVAL = 5;
    private int lastUpdate = 0;

    private ItemStack[] inventory = new ItemStack[26];
    private final int[] SLOTS = InventoryUtils.createSlotArray(0, inventory.length);

    private boolean poweredNow = false;
    private boolean poweredPrev = false;
    private int pendingRequests = 0;
    private boolean inventoryChanged = false;

    private IRecipe currentRecipe = null;
    public Mode mode = null;

    public void cycleModes() {
        Mode[] values = Mode.values();
        if (mode == null || mode.ordinal() + 1 >= values.length) {
            mode = values[0];
        } else {
            mode = values[mode.ordinal() + 1];
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        InventoryUtils.readStacksFromNBT(inventory, tag.getTagList("Inventory"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", InventoryUtils.writeStacksToNBT(inventory));
    }

    @Override
    public void validate() {
        super.validate();
        // TODO:
        // checkForRecipe();
    }

    @Override
    public void updateEntity() {
        poweredPrev = poweredNow;
        poweredNow = isGettingPowered(this);

        if (!poweredPrev && poweredNow) {
            pendingRequests++;
        }

        if (inventoryChanged) {
            inventoryChanged = false;
            // TODO:
            // checkForRecipe();
        }

        if (!worldObj.isRemote && ++lastUpdate > UPDATE_INTERVAL) {
            lastUpdate = 0;

            // TODO:
            // tryCrafting();
        }
    }

    public static boolean isGettingPowered(TileEntity te) {
        if (te.worldObj.isBlockIndirectlyGettingPowered(te.xCoord, te.yCoord, te.zCoord)) {
            return true;
        }
        return false;
    }

    /* IInventory */

    @Override
    public void onInventoryChanged() {
        super.onInventoryChanged();
        inventoryChanged = true;
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory[slotIndex];
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        return InventoryUtils.decrStackSize(this, slotIndex, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        return InventoryUtils.getStackInSlotOnClosing(this, slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack stack) {
        inventory[slotIndex] = stack;
        onInventoryChanged();
    }

    @Override
    public String getInvName() {
        return "container.easycrafting:table.auto_crafting";
    }

    @Override
    public boolean isInvNameLocalized() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
    }

    @Override
    public void openChest() {
    }

    @Override
    public void closeChest() {
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack stack) {
        return slotIndex >= 10 && slotIndex < 18;
    }

    /* ISidedInventory */

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack stack, int side) {
        return slotIndex >= 10 && slotIndex < 18;
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack stack, int side) {
        return slotIndex >= 18 && slotIndex < inventory.length;
    }
}
