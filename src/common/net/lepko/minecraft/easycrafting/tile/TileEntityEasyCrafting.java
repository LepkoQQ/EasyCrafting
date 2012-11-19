package net.lepko.minecraft.easycrafting;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class is the tile portion of the easycraft table.
 */
public class TileEntityEasyCrafting extends TileEntity implements IInventory {

	/** The tile's inventory, used in conjunction with the container.
     * @see net.lepko.minecraft.easycrafting.ContainerEasyCrafting
	 */
	private ItemStack[] inventory;
	public RecipesManager recipesManager;

	/**
	 * Creates an instance of this class
	 *
	 * @param  N/A
	 * @return N/A
	 */
	public TileEntityEasyCrafting() {
		this.inventory = new ItemStack[40 + 18]; // 40 = 5*8 crafting slots, 18 = 2*9 inventory slots
		this.recipesManager = new RecipesManager();
	}

	/**
	 * Returns how big the inventory of this class is
	 *
	 * @param  N/A
	 * @return 		the number of itemstack slots this tile has
	 */
	@Override
	public int getSizeInventory() {
		return this.inventory.length;
	}

	/**
	 * Gets a specific itemstack
	 *
	 * @param  	slotIndex	slot number to get the itemstack of
	 * @return 				the itemstack in the slot
	 */
	@Override
	public ItemStack getStackInSlot(int slotIndex) {
		return this.inventory[slotIndex];
	}

	/**
	 * Sets the contents of a specific inventory slot
	 *
	 * @param  	slot		the inventory slot that should be changed
	 * @param  	stack		what the itemstack in the slot should be changed to
	 * @return N/A
	 */
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.inventory[slot] = stack;

		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
	}

	/**
	 * Decreases the number of items in the itemstack of the specified slot
	 *
	 * @param  	slotIndex	the inventory slot that should be decreased
	 * @param  	amount		how many items must be subtracted for the target itemstack
	 * @return				an itemstack of the items removed from the target slot
	 */
	@Override
	public ItemStack decrStackSize(int slotIndex, int amount) {
		ItemStack stack = getStackInSlot(slotIndex);
		if (stack != null) {

			if (stack.stackSize <= amount) {
				setInventorySlotContents(slotIndex, null);
			} else {
				stack = stack.splitStack(amount);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slotIndex, null);
				}
			}
		}
		return stack;
	}

	/**
	 * Grabs the entire stack out of the target slot (the slot becomes empty, and it returns what was in it)
	 *
	 * @param  	slotIndex	the inventory slot that should be grabbed
	 * @return				an itemstack of the items in the target slot
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex) {
		ItemStack stack = getStackInSlot(slotIndex);
		if (stack != null) {
			setInventorySlotContents(slotIndex, null);
		}
		return stack;
	}

	/**
	 * Returns how much can be stacked in an itemstack in a single slot
	 *
	 * @param  	N/A
	 * @return				the max stack amount
	 */
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Checks if the player is close enough to the tile to use it.
	 *
	 * @param  	player		the player to check the distance to
	 * @return				true if the player is close enough, false if not
	 */
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

	/**
	 * Loads data from the harddrive when doing a world load
	 *
	 * @param  	tagCompound		the NBT complex tag to grab the data from
	 * @return	N/A
	 */
	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		NBTTagList tagList = tagCompound.getTagList("Inventory");
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.length) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}

	/**
	 * Saves data to the harddrive when doing a world load
	 *
	 * @param  	tagCompound		the NBT complex tag write the data to
	 * @return	N/A
	 */
	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack stack = inventory[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		tagCompound.setTag("Inventory", itemList);
	}

	/**
	 * Returns the name of the tile/inventory
	 *
	 * @param  	N/A
	 * @return			the name of the tile/inventory
	 */
	@Override
	public String getInvName() {
		return "TileEntityEasyCrafting";
	}
}