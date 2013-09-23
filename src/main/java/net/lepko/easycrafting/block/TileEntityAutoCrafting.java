package net.lepko.easycrafting.block;

import java.util.List;
import java.util.Locale;

import net.lepko.easycrafting.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityAutoCrafting extends TileEntity implements ISidedInventory {

    private class FakeContainer extends Container {
        private FakeContainer() {
        }

        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }
    }

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

    public Mode mode = null;
    private Mode[] VALID_MODES = Mode.values();

    private int UPDATE_INTERVAL = 5;
    private int lastUpdate = 0;

    private ItemStack[] inventory = new ItemStack[26];
    private final int[] SLOTS = InventoryUtils.createSlotArray(0, inventory.length);

    private boolean poweredNow = false;
    private boolean poweredPrev = false;
    private int pendingRequests = 0;
    private boolean inventoryChanged = false;

    private IRecipe currentRecipe = null;

    public void setMode(int index) {
        if (index >= 0 && index < VALID_MODES.length) {
            mode = VALID_MODES[index];
        }
    }

    public void cycleModes(int mouseButton) {
        if (mouseButton == 0) {
            if (mode == null || mode.ordinal() + 1 >= VALID_MODES.length) {
                setMode(0);
            } else {
                setMode(mode.ordinal() + 1);
            }
        } else if (mouseButton == 1) {
            if (mode == null || mode.ordinal() - 1 < 0) {
                setMode(VALID_MODES.length - 1);
            } else {
                setMode(mode.ordinal() - 1);
            }
        }
    }

    public void checkForRecipe() {
        System.out.println("check recipe");

        @SuppressWarnings("unchecked")
        List<IRecipe> recipeList = (List<IRecipe>) CraftingManager.getInstance().getRecipeList();

        currentRecipe = null;
        InventoryCrafting craftingGrid = new InventoryCrafting(new FakeContainer(), 3, 3);
        InventoryUtils.setContents(craftingGrid, this);

        for (IRecipe recipe : recipeList) {
            if (recipe.matches(craftingGrid, worldObj)) {
                currentRecipe = recipe;
                setInventorySlotContents(9, currentRecipe.getCraftingResult(craftingGrid));
                return;
            }
        }

        setInventorySlotContents(9, null);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        InventoryUtils.readStacksFromNBT(inventory, tag.getTagList("Inventory"));
        setMode(tag.getByte("Mode"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", InventoryUtils.writeStacksToNBT(inventory));
        tag.setByte("Mode", (byte) mode.ordinal());
    }

    @Override
    public void validate() {
        super.validate();

        // checkForRecipe();
    }

    @Override
    public void updateEntity() {
        poweredPrev = poweredNow;
        poweredNow = isGettingPowered(this);

        if (!poweredPrev && poweredNow) {
            pendingRequests++;
        }

        if (!worldObj.isRemote && ++lastUpdate > UPDATE_INTERVAL) {
            lastUpdate = 0;

            if (inventoryChanged) {
                // work here

                inventoryChanged = false;
            }

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
