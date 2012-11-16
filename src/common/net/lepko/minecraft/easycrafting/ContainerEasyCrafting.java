package net.lepko.minecraft.easycrafting;

import java.util.List;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class defines the container that is linked to the Easycraft table.
 * It consists of 4 container parts, namely:
 * Output area to take recipe results from, table storage area,
 * player inventory, and player hotbar.
 */
public class ContainerEasyCrafting extends Container {
	/** The easycraft table tile entity linked to this container. */
	protected TileEntityEasyCrafting tile_entity;
	/** The easycraft table gui. */
	protected GuiEasyCrafting gui;

	/**
	 * Creates an instance of this class.
	 *
	 * This setups the class and also the slots for all the inventories. There are 4 inventory sections.
	 * Crafting output; Easycraft table storage; Player inventory; Player hotbar;
	 *
	 * @see		net.lepko.minecraft.easycrafting.SlotEasyCraftingOutput
	 * @see		net.lepko.minecraft.easycrafting.SlotInterceptor
	 *
	 * @param  	tile_entity			the tile (easycraft table) that this container belongs to
	 * @param  	player_inventory	the inventory of the player that is using the easycraft table
	 * @return 	N/A	
	 */
	public ContainerEasyCrafting(TileEntityEasyCrafting tile_entity, InventoryPlayer player_inventory) {

		this.tile_entity = tile_entity;
		int offset2 = 5 * 18 + 4;
		int offset = offset2 + (2 * 18) + 4;
		int offset3 = offset + (3 * 18) + 5;

		int count = 0;

		// Crafting output slots
		for (int g = 0; g < 5; ++g) {
			for (int h = 0; h < 8; ++h) {
				this.addSlotToContainer(new SlotEasyCraftingOutput(tile_entity, count++, 8 + h * 18, 18 + g * 18));
			}
		}

		// Table inventory slots
		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new SlotInterceptor(tile_entity, count++, 8 + j * 18, 18 + i * 18 + offset2));
			}
		}

		// Player inventory
		for (int k = 0; k < 3; ++k) {
			for (int l = 0; l < 9; ++l) {
				this.addSlotToContainer(new SlotInterceptor(player_inventory, l + k * 9 + 9, 8 + l * 18, 18 + k * 18 + offset));
			}
		}

		// Player hotbar
		for (int m = 0; m < 9; ++m) {
			this.addSlotToContainer(new SlotInterceptor(player_inventory, m, 8 + m * 18, 18 + offset3));
		}
	}

	/**
	 * Determines if the player can interact with this container
	 *
	 * @param  	player		the player that is trying to interact with it
	 * @return 				true if player can interact, false if not
	 */
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tile_entity.isUseableByPlayer(player);
	}

	/**
	 * Moves a itemstack between the major inventory areas.
	 *
	 * Can move itemstacks between the table inventory and player inventory/hotbar. 
	 *
	 * @param  	player			the player that is interacting with the easycraft table
	 * @param  	slot_index		the inventory slot that must be transferred
	 * @return 					the itemstack that was in the target slot before we transferred it
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot_index) {
		ItemStack stack = null;
		Slot slot_object = (Slot) inventorySlots.get(slot_index);

		if (slot_object != null && slot_object.getHasStack()) {
			ItemStack stack_in_slot = slot_object.getStack();
			stack = stack_in_slot.copy();

			//Crafting output slots
			//These slots cannot be transferred with this function, as the items in them technically do not exist.
			if (slot_index < 40) {
				return null;
			//Table inventory slots
			} else if (slot_index >= 40 && slot_index <= 57) {
				//Try to transfer to the player inventory/hotbar
				if (!mergeItemStack(stack_in_slot, 58, inventorySlots.size(), true)) {
					return null;
				}
			//Player inventory or hotbar
			//Try to transfer to the easycraft table storage
			} else if (!mergeItemStack(stack_in_slot, 40, 58, false)) {
				return null;
			}

			//Adjust the number of items that remains in the target slot (where we transferred from)
			if (stack_in_slot.stackSize == 0) {
				slot_object.putStack(null);
			} else {
				slot_object.onSlotChanged();
			}
		}

		return stack;
	}

	/**
	 * Properly handles a user mouse-click based on what slot it is
	 *
	 * If it is a easycraft output slot, we need to run the easycraft recipe that is in that slot.
	 * Otherwise, if it is a normal slot, we just let the super (parent) slot code run.
	 *
	 * @param  	slot_index		The slot that was clicked on
	 * @param  	mouse_button	What mouse button was clicked (left-click, right-click, etc.)
	 * @param  	modifier		What modifiers are being applied to the click (is Shift is held?, etc.)
	 * @param  	player			What player clicked on the slot
	 * @return 					The itemstack that is in the slot that was clicked. <--Unsure about this. Double check.
	 */
	@Override
	public ItemStack slotClick(int slot_index, int mouse_button, int modifier, EntityPlayer player) {
		if (slot_index >= 0 && inventorySlots.get(slot_index) instanceof SlotEasyCraftingOutput) {
			return this.slotClickEasyCraftingOutput(slot_index, mouse_button, modifier, player);
		} else {
			return super.slotClick(slot_index, mouse_button, modifier, player);
		}
	}

	/**
	 * Gives the user the results of the selected easycraft recipe, and executes said easycraft recipe.
	 *
	 * This method also informs the server to make the change in the player's inventory. 
	 *
	 * @see		net.lepko.minecraft.easycrafting.Recipes
	 *
	 * @param  	slot_index		The slot that was clicked on
	 * @param  	mouse_button	What mouse button was clicked (left-click, right-click, etc.)
	 * @param  	modifier		What modifiers are being applied to the click (is Shift is held?, etc.)
	 * @param  	player			What player clicked on the slot
	 * @return 					The itemstack result of the easycraft recipe selected.
	 */
	private ItemStack slotClickEasyCraftingOutput(int slot_index, int mouse_button, int modifier, EntityPlayer player) {
		if (!ProxyCommon.proxy.isClient()) {
			return null;
		}

		if ((mouse_button != 0 && mouse_button != 1) || (modifier != 0 && modifier != 1)) {
			return null;
		}

		Slot clicked_slot = (Slot) this.inventorySlots.get(slot_index);
		if (clicked_slot == null) {
			return null;
		}

		ItemStack stack_in_slot = clicked_slot.getStack();
		if (stack_in_slot == null) {
			return null;
		}

		InventoryPlayer player_inventory = player.inventory;

		ItemStack stack_in_hand = player_inventory.getItemStack();
		ItemStack stack_in_hand_to_send = stack_in_hand != null ? stack_in_hand.copy() : null;
		ItemStack return_stack = null;

		// TODO: Shift clicking to transfer stack to inventory

		//If the player has nothing is hand (the mousecursor is not dragging an itemstack around)
		if (stack_in_hand == null) {
			return_stack = stack_in_slot.copy();
			clicked_slot.onPickupFromSlot(player, return_stack);
		} else {
			//If the player has something in hand
			if (stack_in_slot.itemID == stack_in_hand.itemID && stack_in_hand.getMaxStackSize() > 1 && (!stack_in_slot.getHasSubtypes() || stack_in_slot.getItemDamage() == stack_in_hand.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack_in_slot, stack_in_hand)) {
				int numberOfItemsToMove = stack_in_slot.stackSize;
				if (numberOfItemsToMove > 0 && numberOfItemsToMove + stack_in_hand.stackSize <= stack_in_hand.getMaxStackSize()) {
					stack_in_hand.stackSize += numberOfItemsToMove;
					return_stack = stack_in_hand.copy();
					clicked_slot.onPickupFromSlot(player, player_inventory.getItemStack());
				}
			}
		}

		if (return_stack != null && this.gui != null) {
			int ident = mouse_button == 0 ? 1 : 2;

			EasyRecipe r = Recipes.getValidRecipe(this.gui, slot_index, stack_in_hand_to_send, return_stack);

			if (r != null) {
				//Inform the server to give the player the result of the recipe, and remove the ingredients from the player's inventory.
				ProxyCommon.proxy.sendEasyCraftingPacketToServer(return_stack, slot_index, player.inventory, stack_in_hand_to_send, ident, r);

				if (ident == 2) { // Right click; craft until max stack
					int maxTimes = Recipes.calculateCraftingMultiplierUntilMaxStack(stack_in_slot, stack_in_hand_to_send);
					int timesCrafted = Recipes.hasIngredientsMaxStack(r.ingredients, player_inventory, maxTimes, 0);
					if (timesCrafted > 0) {
						return_stack.stackSize += (timesCrafted - 1) * r.result.stackSize;
						player.inventory.setItemStack(return_stack);
					}
				} else { // Left click; craft once
					player.inventory.setItemStack(return_stack);
				}
			}
		}

		clicked_slot.onSlotChanged();
		return return_stack;
	}

	/**
	 * Updates the client GUI to display the correct easycraft recipes based on the scroll bar position.
	 *
	 * @param  	currentScroll	The position of the scroll bar
	 * @param  	rl				The list of easycraft recipes that is currently craftable
	 * @return 	N/A
	 */
	@SideOnly(Side.CLIENT)
	public void scrollTo(int currentScroll, List<EasyRecipe> rl) {
		int offset = currentScroll * 8;
		for (int i = 0; i < 40; i++) {
			if (i + offset >= rl.size() || i + offset < 0) {
				tile_entity.setInventorySlotContents(i, null);
			} else {
				ItemStack is = rl.get(i + offset).result;
				tile_entity.setInventorySlotContents(i, is.copy());
			}
		}
	}
}