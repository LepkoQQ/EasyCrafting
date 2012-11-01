package net.lepko.minecraft.easycrafting;

import java.util.List;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class ContainerEasyCrafting extends Container {
	protected TileEntityEasyCrafting tile_entity;
	protected GuiEasyCrafting gui;

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

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tile_entity.isUseableByPlayer(player);
	}

	// public ItemStack transferStackInSlot(int slot_index)
	@Override
	public ItemStack func_82846_b(EntityPlayer player, int slot_index) {
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
			return this.slotClickEasyCraftingOutput(slot_index, mouse_button, modifier, player);
		} else {
			return super.slotClick(slot_index, mouse_button, modifier, player);
		}
	}

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

		if (stack_in_hand == null) {
			return_stack = stack_in_slot.copy();
			clicked_slot.func_82870_a(player, return_stack); // clicked_slot.onPickupFromSlot(return_stack);
		} else {
			if (stack_in_slot.itemID == stack_in_hand.itemID && stack_in_hand.getMaxStackSize() > 1 && (!stack_in_slot.getHasSubtypes() || stack_in_slot.getItemDamage() == stack_in_hand.getItemDamage()) && ItemStack.func_77970_a(stack_in_slot, stack_in_hand)) {
				int numberOfItemsToMove = stack_in_slot.stackSize;
				if (numberOfItemsToMove > 0 && numberOfItemsToMove + stack_in_hand.stackSize <= stack_in_hand.getMaxStackSize()) {
					stack_in_hand.stackSize += numberOfItemsToMove;
					return_stack = stack_in_hand.copy();
					clicked_slot.func_82870_a(player, player_inventory.getItemStack()); // clicked_slot.onPickupFromSlot(player_inventory.getItemStack());
				}
			}
		}

		if (return_stack != null && this.gui != null) {
			int ident = mouse_button == 0 ? 1 : 2;

			EasyRecipe r = Recipes.getValidRecipe(this.gui, slot_index, stack_in_hand_to_send, return_stack);

			if (r != null) {
				ProxyCommon.proxy.sendEasyCraftingPacketToServer(return_stack, slot_index, player.inventory, stack_in_hand_to_send, ident, r);

				if (ident == 2) { // Right click; craft until max stack
					int maxTimes = Recipes.calculateCraftingMultiplierUntilMaxStack(stack_in_slot, stack_in_hand_to_send);
					int timesCrafted = Recipes.hasIngredientsMaxStack(r.ingredients, player_inventory, maxTimes);
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