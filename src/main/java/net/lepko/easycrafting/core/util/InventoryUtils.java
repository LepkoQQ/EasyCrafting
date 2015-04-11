package net.lepko.easycrafting.core.util;

import net.lepko.easycrafting.Ref;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InventoryUtils {

	private static final Random RNG = new Random();
	private static final int PLAYER_INVENTORY_SIZE = 36;

	/**
	 * Get the size of the main inventory, without armor slots.
	 *
	 * @param inv - the inventory to check
	 * @return size of the inventory, or in case of {@link InventoryPlayer} PLAYER_INVENTORY_SIZE
	 */
	public static int getMainInventorySize(IInventory inv) {
		if (inv instanceof InventoryPlayer) {
			return PLAYER_INVENTORY_SIZE;
		}
		return inv.getSizeInventory();
	}

	/**
	 * Get the first empty slot in the inventory.
	 *
	 * @param inventory - inventory to check
	 * @return slot index of the first empty slot, -1 if none found
	 */
	public static int getEmptySlot(IInventory inventory) {
		int invSize = getMainInventorySize(inventory);
		return getEmptySlot(inventory, 0, invSize);
	}

	/**
	 * Get the first empty slot in the inventory inside the provided slot index range.
	 *
	 * @param inventory - inventory to check
	 * @param start     - first slot index (inclusive)
	 * @param end       - last slot index (exclusive)
	 * @return slot index of the first empty slot, -1 if none found
	 */
	public static int getEmptySlot(IInventory inventory, int start, int end) {
		for (int i = start; i < end; i++) {
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
	 * @param list      - list of itemstacks
	 */
	public static void setContents(IInventory inventory, List<ItemStack> list) {
		if (inventory.getSizeInventory() != list.size()) {
			Ref.LOGGER.warn("Tried to set inventory contents from a list that is not the same size as the inventory; Aborted!");
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			inventory.setInventorySlotContents(i, ItemStack.copyItemStack(list.get(i)));
		}
	}

	/**
	 * Replace the contents of an inventory with the contents of another inventory.
	 *
	 * @param to   - inventory to set contents to
	 * @param from - inventory to read contents from
	 */
	public static void setContents(IInventory to, IInventory from) {
		int invSize = Math.min(to.getSizeInventory(), from.getSizeInventory());
		for (int i = 0; i < invSize; i++) {
			to.setInventorySlotContents(i, ItemStack.copyItemStack(from.getStackInSlot(i)));
		}
	}

	/**
	 * Add the specified itemstack to inventory. Try stacking it with existing stacks first. If that fails try to put it in an empty slot.
	 *
	 * @param inventory - inventory to add to
	 * @param itemstack - item to add
	 * @return whether or not the itemstack was added to the inventory
	 */
	public static boolean addItemToInventory(IInventory inventory, ItemStack itemstack) {
		int invSize = getMainInventorySize(inventory);
		return addItemToInventory(inventory, itemstack, 0, invSize);
	}

	/**
	 * Add the specified itemstack to inventory. Try stacking it with existing stacks first. If that fails try to put it in an empty slot. Constrain
	 * to the provided slot index range.
	 *
	 * @param inventory - inventory to add to
	 * @param itemstack - item to add
	 * @param start     - first slot index (inclusive)
	 * @param end       - last slot index (exclusive)
	 * @return whether or not the itemstack was added to the inventory
	 */
	public static boolean addItemToInventory(IInventory inventory, ItemStack itemstack, int start, int end) {
		List<ItemStack> contents = InventoryUtils.storeContents(inventory);
		int maxStack = Math.min(inventory.getInventoryStackLimit(), itemstack.getMaxStackSize());
		for (int i = start; i < end; i++) {
			if (StackUtils.areEqualNoSize(itemstack, inventory.getStackInSlot(i))) {
				ItemStack is = inventory.getStackInSlot(i);
				if (is.stackSize >= maxStack) {
					continue;
				}
				if (is.stackSize + itemstack.stackSize <= maxStack) {
					is.stackSize += itemstack.stackSize;
					inventory.markDirty();
					return true;
				} else {
					itemstack.stackSize -= maxStack - is.stackSize;
					is.stackSize = maxStack;
				}
			}
		}
		while (true) {
			int slot = InventoryUtils.getEmptySlot(inventory, start, end);
			if (slot != -1) {
				if (itemstack.stackSize <= maxStack) {
					inventory.setInventorySlotContents(slot, itemstack.copy());
					inventory.markDirty();
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
		InventoryUtils.setContents(inventory, contents);
		return false;
	}

	/**
	 * Decreases the stack size in the inventoryIndex slot in the inventory by 1 and gives back any container items (bucket, etc.). Also adds the
	 * consumed item to the usedIngredients list.
	 *
	 * @param inventory       - inventory to take from
	 * @param inventoryIndex  - slot index to take from
	 * @param usedIngredients - a list to which the consumed ingredient will be added
	 * @return true if successful, false if there is no space for container items or cannot take from stack
	 */
	public static boolean consumeItemForCrafting(IInventory inventory, int inventoryIndex, List<ItemStack> usedIngredients) {
		ItemStack stack = inventory.decrStackSize(inventoryIndex, 1);
		if (stack != null) {
			if (stack.getItem().hasContainerItem(stack)) {
				ItemStack containerStack = stack.getItem().getContainerItem(stack);
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
			inventory.markDirty();
			return true;
		}
		return false;
	}

	public static void readStacksFromNBT(ItemStack[] stacks, NBTTagList nbt) {
		for (int i = 0; i < nbt.tagCount(); i++) {
			NBTTagCompound tag = nbt.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < stacks.length) {
				stacks[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}

	public static NBTTagList writeStacksToNBT(ItemStack[] stacks) {
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < stacks.length; i++) {
			ItemStack stack = stacks[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		return itemList;
	}

	public static void dropItems(TileEntity te) {
		if (te instanceof IInventory) {
			dropItems(te, createSlotArray(0, ((IInventory) te).getSizeInventory()));
		}
	}

	public static void dropItems(TileEntity te, int[] slots) {
		if (te instanceof IInventory) {
			double x = te.xCoord + 0.5;
			double y = te.yCoord + 0.5;
			double z = te.zCoord + 0.5;
			IInventory inv = (IInventory) te;

			for (int slot : slots) {
				dropItem(te.getWorldObj(), x, y, z, inv.getStackInSlot(slot));
			}
		}
	}

	public static void dropItem(World world, double x, double y, double z, ItemStack stack) {
		if (stack != null) {
			EntityItem drop = new EntityItem(world, x, y, z, stack.copy());
			float speed = 0.05F;
			drop.motionX = (float) RNG.nextGaussian() * speed;
			drop.motionY = (float) RNG.nextGaussian() * speed + 0.2F;
			drop.motionZ = (float) RNG.nextGaussian() * speed;
			world.spawnEntityInWorld(drop);
		}
	}

	public static ItemStack decreaseStackSize(IInventory inv, int slotIndex, int amount) {
		ItemStack stack = inv.getStackInSlot(slotIndex);
		if (stack != null) {
			if (stack.stackSize <= amount) {
				inv.setInventorySlotContents(slotIndex, null);
			} else {
				ItemStack is = stack.splitStack(amount);
				if (stack.stackSize == 0) {
					inv.setInventorySlotContents(slotIndex, null);
				} else {
					inv.markDirty();
				}
				inv.markDirty();
				return is;
			}
			inv.markDirty();
		}
		return stack;
	}

	public static ItemStack getStackInSlotOnClosing(IInventory inv, int slotIndex) {
		ItemStack stack = inv.getStackInSlot(slotIndex);
		inv.setInventorySlotContents(slotIndex, null);
		return stack;
	}

	/**
	 * Create an array of slot indices (int[]) for use with ISidedInventory. First slot index is inclusive, last index is exclusive.
	 */
	 public static int[] createSlotArray(int first, int last) {
		 int[] slots = new int[last - first];
		 for (int i = first; i < last; i++) {
			 slots[i - first] = i;
		 }
		 return slots;
	 }
}
