package net.lepko.minecraft.easycrafting;

import java.util.List;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class ContainerEasyCrafting extends Container {
	protected TileEntityEasyCrafting tile_entity;

	public ContainerEasyCrafting(TileEntityEasyCrafting tile_entity, InventoryPlayer player_inventory) {

		this.tile_entity = tile_entity;
		int offset2 = 5 * 18 + 4;
		int offset = offset2 + (2 * 18) + 4;
		int offset3 = offset + (3 * 18) + 5;

		int count = 0;

		// Crafting output slots
		for (int g = 0; g < 5; ++g) {
			for (int h = 0; h < 8; ++h) {
				this.addSlotToContainer(new FakeSlot(tile_entity, count++, 8 + h * 18, 18 + g * 18));
			}
		}

		// Table inventory slots
		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(tile_entity, count++, 8 + j * 18, 18 + i * 18 + offset2));
			}
		}

		// Player inventory
		for (int k = 0; k < 3; ++k) {
			for (int l = 0; l < 9; ++l) {
				this.addSlotToContainer(new Slot(player_inventory, l + k * 9 + 9, 8 + l * 18, 18 + k * 18 + offset));
			}
		}

		// Player hotbar
		for (int m = 0; m < 9; ++m) {
			this.addSlotToContainer(new Slot(player_inventory, m, 8 + m * 18, 18 + offset3));
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
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (slot_index >= 0 && inventorySlots.get(slot_index) instanceof FakeSlot) {
			// is a crafting output slot

			if (side == Side.CLIENT) {
				// get in hand stack info for later
				ItemStack inHand = null;
				if (player.inventory.getItemStack() != null) {
					inHand = player.inventory.getItemStack().copy();
				}

				// handle click on fake slot
				boolean isShiftDown = (modifier == 1);
				ItemStack is = handleFakeSlotClick(slot_index, mouse_button, isShiftDown, player);
				if (is != null) {
					// need to send inHand data on player as it was before handleClick
					PacketSender.sendEasyCraftingPacketToServer(is, slot_index, player.inventory, inHand);
					// set the returned IS in hand
					player.inventory.setItemStack(is);
				}
				return is;
			}

			return null;
		} else {
			// normal storage slot
			ItemStack is = super.slotClick(slot_index, mouse_button, modifier, player);
			if (side == Side.CLIENT) {
				// refreshCraftingOutput(player);
				TickHandlerClient.updateEasyCraftingOutput = true;
			}
			return is;
		}
	}

	private ItemStack handleFakeSlotClick(int slot_index, int mouse_button, boolean isShiftDown, EntityPlayer player) {
		if (mouse_button != 0 && mouse_button != 1) {
			return null;
		}

		Slot clicked_slot = (Slot) this.inventorySlots.get(slot_index);
		if (clicked_slot == null) {
			return null;
		}

		InventoryPlayer player_inventory = player.inventory;

		ItemStack stack_in_slot = clicked_slot.getStack();
		if (stack_in_slot == null) {
			return null;
		}

		ItemStack stack_in_hand = player_inventory.getItemStack();
		ItemStack return_stack = null;

		// TODO: Shift clicking to transfer stack to inventory and right click to craft whole stack to hand

		if (stack_in_hand == null) {
			return_stack = stack_in_slot.copy();
			// clicked_slot.onPickupFromSlot(return_stack);
			clicked_slot.func_82870_a(player, return_stack);
		} else {
			if (stack_in_slot.itemID == stack_in_hand.itemID && stack_in_hand.getMaxStackSize() > 1 && (!stack_in_slot.getHasSubtypes() || stack_in_slot.getItemDamage() == stack_in_hand.getItemDamage()) && ItemStack.func_77970_a(stack_in_slot, stack_in_hand)) {
				int numberOfItemsToMove = stack_in_slot.stackSize;
				if (numberOfItemsToMove > 0 && numberOfItemsToMove + stack_in_hand.stackSize <= stack_in_hand.getMaxStackSize()) {
					stack_in_hand.stackSize += numberOfItemsToMove;
					return_stack = stack_in_hand.copy();
					// clicked_slot.onPickupFromSlot(player_inventory.getItemStack());
					clicked_slot.func_82870_a(player, player_inventory.getItemStack());
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