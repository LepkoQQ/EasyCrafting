package net.lepko.minecraft.easycrafting.helpers;

import java.util.ArrayList;
import java.util.List;

import net.lepko.minecraft.easycrafting.ModEasyCrafting;
import net.lepko.minecraft.easycrafting.easyobjects.EasyItemStack;
import net.lepko.minecraft.easycrafting.easyobjects.EasyRecipe;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;

public class InventoryHelper {

	/**
	 * Check if an inventory contains the items from the supplied EasyItemStack. Doesn't work on split stacks. Inventory must have a stack big enough to take from.
	 * 
	 * @param inventory
	 *            IInventory to check
	 * @param eis
	 *            EasyItemStack to find
	 * @return slot index of item in inventory, -1 if not found
	 */
	public static int isItemStackInInventory(IInventory inventory, EasyItemStack eis) {
		int invSize = inventory.getSizeInventory();
		if (inventory instanceof InventoryPlayer) {
			invSize -= 4; // Remove Armor slots
		}

		for (int i = 0; i < invSize; i++) {
			if (eis.canTakeFrom(inventory.getStackInSlot(i))) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Decreases the stack size in the inventoryIndex slot in the inventory by 1 and gives back any container items (bucket, etc.).
	 * 
	 * @param inventory
	 *            The inventory to take from
	 * @param inventoryIndex
	 *            The slot index to take from
	 * @return true if successful, false if there is no space for container items
	 */
	public static boolean consumeItemForCrafting(InventoryPlayer inventory, int inventoryIndex, List usedIngredients) {
		ItemStack stack = inventory.getStackInSlot(inventoryIndex);
		usedIngredients.add(inventory.decrStackSize(inventoryIndex, 1));
		if (stack.getItem().hasContainerItem()) {
			ItemStack containerStack = stack.getItem().getContainerItemStack(stack);
			// TODO: damage Items that take damage when crafting with them
			if (containerStack.isItemStackDamageable() && containerStack.getItemDamage() > containerStack.getMaxDamage()) {
				containerStack = null;
			}
			if (containerStack != null && !inventory.addItemStackToInventory(containerStack)) {
				return false;
			}
		}
		return true;
	}

	public static int checkIngredients(EasyRecipe recipe, InventoryPlayer inventory, boolean takeIngredients, int maxTimes, int recursionCount) {
		if (recursionCount >= ModEasyCrafting.instance.allowMultiStepRecipes) {
			// TODO: implement recursion in this new method
			return 0;
		}

		recipe.getResult().setCharge(null);

		InventoryPlayer tmp = new InventoryPlayer(inventory.player);
		InventoryPlayer tmp2 = new InventoryPlayer(inventory.player);
		tmp.copyInventory(inventory);

		List usedIngredients = new ArrayList<ItemStack>();

		int amountCrafted = 0;
		timesCraftedLoop: while (amountCrafted < maxTimes) {
			ingredientsLoop: for (int ingredientIndex = 0; ingredientIndex < recipe.getIngredientsSize(); ingredientIndex++) {
				if (recipe.getIngredient(ingredientIndex) instanceof EasyItemStack) {
					EasyItemStack ingredient = (EasyItemStack) recipe.getIngredient(ingredientIndex);
					int inventoryIndex = InventoryHelper.isItemStackInInventory(tmp, ingredient);
					if (inventoryIndex != -1 && InventoryHelper.consumeItemForCrafting(tmp, inventoryIndex, usedIngredients)) {
						continue ingredientsLoop;
					}
					break timesCraftedLoop;
				} else if (recipe.getIngredient(ingredientIndex) instanceof List) {
					List possibleIngredients = (List) recipe.getIngredient(ingredientIndex);
					possibleIngredientsLoop: for (int i = 0; i < possibleIngredients.size(); i++) {
						if (possibleIngredients.get(i) instanceof ItemStack) {
							EasyItemStack ingredient = EasyItemStack.fromItemStack((ItemStack) possibleIngredients.get(i));
							int inventoryIndex = InventoryHelper.isItemStackInInventory(tmp, ingredient);
							if (inventoryIndex != -1 && InventoryHelper.consumeItemForCrafting(tmp, inventoryIndex, usedIngredients)) {
								continue ingredientsLoop;
							} else {
								continue possibleIngredientsLoop;
							}
						}
					}
					break timesCraftedLoop;
				}
			}
			amountCrafted++;
			tmp2.copyInventory(tmp);
		}

		recipe.getResult().setCharge(usedIngredients);

		if (takeIngredients) {
			inventory.copyInventory(tmp2);
		}
		return amountCrafted;
	}
}
