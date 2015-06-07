package net.lepko.easycrafting.core.block;

import cpw.mods.fml.common.FMLCommonHandler;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.inventory.ContainerAutoCrafting;
import net.lepko.easycrafting.core.inventory.gui.GuiAutoCrafting;
import net.lepko.easycrafting.core.inventory.gui.IGuiTile;
import net.lepko.easycrafting.core.util.InventoryUtils;
import net.lepko.easycrafting.core.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.List;
import java.util.Locale;

public class TileEntityAutoCrafting extends TileEntity implements ISidedInventory, IGuiTile {

    private static class FakeContainer extends Container {
        private FakeContainer() {
        }

        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }
    }

    private static class StackReference {
        public final IInventory inv;
        public final int slot;

        public StackReference(IInventory inv, int slot) {
            this.inv = inv;
            this.slot = slot;
        }

        public ItemStack getCopy(int size) {
            return StackUtils.copyStack(inv.getStackInSlot(slot), size);
        }

        public ItemStack decreaseStackSize(int amt) {
            return InventoryUtils.decreaseStackSize(inv, slot, amt);
        }
    }

    public static enum Mode {
        PULSE,
        ALWAYS,
        POWERED,
        UNPOWERED;

        public final String tooltip;

        Mode() {
            tooltip = String.format("mode.easycrafting:%s.tooltip", this.toString().toLowerCase(Locale.ENGLISH));
        }
    }

    private static final Mode[] VALID_MODES = Mode.values();
    private static final int UPDATE_INTERVAL = 5;

    private Mode mode = Mode.ALWAYS;
    private int lastUpdate = 0;

    private ItemStack[] inventory = new ItemStack[26];
    public final int[] SLOTS = InventoryUtils.createSlotArray(10, inventory.length);

    private boolean poweredNow = false;
    private boolean poweredPrev = false;
    private boolean inventoryChanged = false;
    private int pendingRequests = 0;

    public boolean scheduledRecipeCheck = false;
    private InventoryCrafting craftingGrid = new InventoryCrafting(new FakeContainer(), 3, 3);
    private IRecipe currentRecipe = null;
    private boolean lastCraftingSuccess = true;

    public void setMode(int index) {
        if (index >= 0 && index < VALID_MODES.length) {
            mode = VALID_MODES[index];
        }
    }

    public Mode getMode() {
        return mode;
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

    private void checkForRecipe() {
        @SuppressWarnings("unchecked")
        List<IRecipe> recipeList = (List<IRecipe>) CraftingManager.getInstance().getRecipeList();

        InventoryUtils.setContents(craftingGrid, this);

        for (IRecipe recipe : recipeList) {
            if (recipe.matches(craftingGrid, worldObj)) {
                currentRecipe = recipe;
                setInventorySlotContents(9, currentRecipe.getCraftingResult(craftingGrid));
                return;
            }
        }

        currentRecipe = null;
        setInventorySlotContents(9, null);
    }

    private boolean isReplaceableInCraftingGridSlot(int slot, ItemStack stack) {
        craftingGrid.setInventorySlotContents(slot, stack);
        boolean result = currentRecipe.matches(craftingGrid, worldObj) && StackUtils.areIdentical(currentRecipe.getCraftingResult(craftingGrid), getStackInSlot(9));
        craftingGrid.setInventorySlotContents(slot, getStackInSlot(slot));
        return result;
    }

    private boolean tryCrafting() {
        if (currentRecipe == null || getStackInSlot(9) == null) {
            return false;
        }

        boolean[] found = new boolean[9];
        StackReference[] refs = new StackReference[9];

        for (int o = 0; o < 9; o++) {
            found[o] = craftingGrid.getStackInSlot(o) == null;
        }

        invLoop:
        for (int invSlot = 10; invSlot < 18; invSlot++) {
            ItemStack stack = ItemStack.copyItemStack(getStackInSlot(invSlot));
            if (stack != null && stack.stackSize > 0) {
                for (int gridSlot = 0; gridSlot < 9; gridSlot++) {
                    if (!found[gridSlot]) {
                        if (isReplaceableInCraftingGridSlot(gridSlot, stack)) {
                            refs[gridSlot] = new StackReference(this, invSlot);
                            found[gridSlot] = true;
                            if (--stack.stackSize <= 0) {
                                continue invLoop;
                            }
                        }
                    }
                }
            }
        }

        for (boolean b : found) {
            if (!b) {
                return false;
            }
        }

        // replace all ingredients with found stacks
        for (int i = 0; i < 9; i++) {
            if (craftingGrid.getStackInSlot(i) != null) {
                ItemStack is = refs[i].getCopy(1);
                craftingGrid.setInventorySlotContents(i, is);
            }
        }

        boolean craftingCompleted = false;

        // test the recipe to make sure all replacements play nice with each other
        ItemStack result = currentRecipe.getCraftingResult(craftingGrid);
        if (currentRecipe.matches(craftingGrid, worldObj) && StackUtils.areIdentical(result, getStackInSlot(9))) {
            if (InventoryUtils.addItemToInventory(this, result, 18, 26)) {
                FakePlayer fakePlayer = FakePlayerFactory.get((WorldServer) worldObj, Ref.GAME_PROFILE);
                FMLCommonHandler.instance().firePlayerCraftingEvent(fakePlayer, result, craftingGrid);
                result.onCrafting(worldObj, fakePlayer, result.stackSize);

                for (StackReference ref : refs) {
                    if (ref != null) {
                        ItemStack stack = ref.decreaseStackSize(1);
                        if (stack != null && stack.getItem() != null && stack.getItem().hasContainerItem(stack)) {
                            ItemStack container = stack.getItem().getContainerItem(stack);
                            if (container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage()) {
                                container = null;
                            }
                            if (container != null && !InventoryUtils.addItemToInventory(this, container, 18, 26)) {
                                InventoryUtils.dropItem(worldObj, xCoord + 0.5, yCoord + 1, zCoord + 0.5, container);
                                // XXX: try other inventories
                            }
                        }
                    }
                }

                craftingCompleted = true;
            }
        }

        // restore original items from ghost slots
        InventoryUtils.setContents(craftingGrid, this);
        return craftingCompleted;
    }

    /* TileEntity */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        InventoryUtils.readStacksFromNBT(inventory, tag.getTagList("Inventory", Constants.NBT.TAG_COMPOUND));
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
        checkForRecipe();
    }

    @Override
    public void updateEntity() {
        if (scheduledRecipeCheck) {
            scheduledRecipeCheck = false;
            checkForRecipe();
        }

        poweredPrev = poweredNow;
        poweredNow = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

        if (!poweredPrev && poweredNow) {
            pendingRequests++;
        }

        if (!worldObj.isRemote && ++lastUpdate > UPDATE_INTERVAL) {
            lastUpdate = 0;

            if (lastCraftingSuccess || inventoryChanged) {
                inventoryChanged = false;

                if (mode == Mode.ALWAYS || (mode == Mode.POWERED && poweredNow) || (mode == Mode.UNPOWERED && !poweredNow)) {
                    lastCraftingSuccess = tryCrafting();
                } else if (mode == Mode.PULSE && pendingRequests > 0) {
                    if (lastCraftingSuccess = tryCrafting()) {
                        pendingRequests--;
                    }
                }
            }
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        inventoryChanged = true;
    }

    /* IInventory */

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
        return InventoryUtils.decreaseStackSize(this, slotIndex, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        return InventoryUtils.getStackInSlotOnClosing(this, slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack stack) {
        inventoryChanged = true;
        inventory[slotIndex] = stack;
    }

    @Override
    public String getInventoryName() {
        return "container.easycrafting:table.auto_crafting";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
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

    @Override
    public Object getServerGuiElement(EntityPlayer player, TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityAutoCrafting) {
            return new ContainerAutoCrafting(player.inventory, ((TileEntityAutoCrafting) tileEntity));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(EntityPlayer player, TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityAutoCrafting) {
            return new GuiAutoCrafting(player.inventory, ((TileEntityAutoCrafting) tileEntity));
        }
        return null;
    }
}
