package net.lepko.easycrafting.helpers;

import java.util.ArrayList;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;

public class InventoryHelper {

	/**
	 * Check if an inventory contains a single item matching the supplied EasyItemStack.
	 * 
	 * @param inventory
	 *            IInventory to check
	 * @param eis
	 *            EasyItemStack to find
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
	 * Decreases the stack size in the inventoryIndex slot in the inventory by 1 and gives back any container items (bucket, etc.). Also adds the
	 * consumed item to the usedIngredients list.
	 * 
	 * @param inventory
	 *            inventory to take from
	 * @param inventoryIndex
	 *            slot index to take from
	 * @param usedIngredients
	 *            an ArrayList to which the consumed ingredient will be added
	 * @return true if successful, false if there is no space for container items or cannot take from stack
	 */
	public static boolean consumeItemForCrafting(InventoryPlayer inventory, int inventoryIndex, ArrayList usedIngredients) {
		ItemStack stack = inventory.decrStackSize(inventoryIndex, 1);
		if (stack != null) {
			if (stack.getItem().hasContainerItem()) {
				ItemStack containerStack = stack.getItem().getContainerItemStack(stack);
				// TODO: damage Items that take damage when crafting with them
				if (containerStack.isItemStackDamageable() && containerStack.getItemDamage() > containerStack.getMaxDamage()) {
					containerStack = null;
				}
				if (containerStack != null && !inventory.addItemStackToInventory(containerStack)) {
					inventory.addItemStackToInventory(stack);
					return false;
				}
			}
			usedIngredients.add(stack);
			return true;
		}
		return false;
	}

	// XXX: JavaDoc
	public static int checkIngredients(EasyRecipe recipe, InventoryPlayer inventory, boolean takeIngredients, int maxTimes, int recursionCount) {
		if (recursionCount >= EasyConfig.instance().recipeRecursion.getInt(0)) {
			return 0;
		}

		recipe.getResult().setCharge(null);

		InventoryPlayer tmp = new InventoryPlayer(inventory.player);
		InventoryPlayer tmp2 = new InventoryPlayer(inventory.player);
		tmp.copyInventory(inventory);

		ArrayList<ItemStack> usedIngredients = new ArrayList<ItemStack>();

		int amountCrafted = 0;
		timesCraftedLoop: while (amountCrafted < maxTimes) {
			ingredientsLoop: for (int ingredientIndex = 0; ingredientIndex < recipe.getIngredientsSize(); ingredientIndex++) {
				if (recipe.getIngredient(ingredientIndex) instanceof EasyItemStack) {
					EasyItemStack ingredient = (EasyItemStack) recipe.getIngredient(ingredientIndex);
					int inventoryIndex = InventoryHelper.isItemInInventory(tmp, ingredient);
					if (inventoryIndex != -1 && InventoryHelper.consumeItemForCrafting(tmp, inventoryIndex, usedIngredients)) {
						continue ingredientsLoop;
					}
					// TODO: make method or sth to avoid code dupe
					if ((recursionCount + 1) < EasyConfig.instance().recipeRecursion.getInt(0)) {
						ArrayList<EasyRecipe> rList = RecipeHelper.getValidRecipes(ingredient);
						if (!rList.isEmpty()) {
							for (int l = 0; l < rList.size(); l++) {
								EasyRecipe ingRecipe = rList.get(l);
								if (RecipeHelper.takeIngredients(ingRecipe, tmp, recursionCount + 1) && tmp.addItemStackToInventory(ingRecipe.getResult().toItemStack())) {
									// Try to take the same ingredient again
									ingredientIndex--;
									continue ingredientsLoop;
								}
							}
						}
					}
					//
					break timesCraftedLoop;
				} else if (recipe.getIngredient(ingredientIndex) instanceof ArrayList) {
					ArrayList<ItemStack> possibleIngredients = (ArrayList<ItemStack>) recipe.getIngredient(ingredientIndex);
					possibleIngredientsLoop: for (int i = 0; i < possibleIngredients.size(); i++) {
						if (possibleIngredients.get(i) instanceof ItemStack) {
							EasyItemStack ingredient = EasyItemStack.fromItemStack((ItemStack) possibleIngredients.get(i));
							int inventoryIndex = InventoryHelper.isItemInInventory(tmp, ingredient);
							if (inventoryIndex != -1 && InventoryHelper.consumeItemForCrafting(tmp, inventoryIndex, usedIngredients)) {
								continue ingredientsLoop;
							} else {
								continue possibleIngredientsLoop;
							}
						}
					}
					// TODO: make method or sth to avoid code dupe
					if ((recursionCount + 1) < EasyConfig.instance().recipeRecursion.getInt(0)) {
						ArrayList<EasyRecipe> rList = RecipeHelper.getValidRecipes(possibleIngredients);
						if (!rList.isEmpty()) {
							for (int l = 0; l < rList.size(); l++) {
								EasyRecipe ingRecipe = rList.get(l);
								if (RecipeHelper.takeIngredients(ingRecipe, tmp, recursionCount + 1) && tmp.addItemStackToInventory(ingRecipe.getResult().toItemStack())) {
									// Try to take the same ingredient again
									ingredientIndex--;
									continue ingredientsLoop;
								}
							}
						}
					}
					//
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
