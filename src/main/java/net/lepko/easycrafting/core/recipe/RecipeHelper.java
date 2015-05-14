package net.lepko.easycrafting.core.recipe;

import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.util.InventoryUtils;
import net.lepko.easycrafting.core.util.StackUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RecipeHelper {

	/**
	 * Check if a recipe can be crafted with the ingredients from the inventory.
	 */
	public static boolean canCraft(WrappedRecipe recipe, IInventory inventory) {
		return canCraft(recipe, inventory, false, 1, 0) > 0;
	}

	/**
	 * Check if a recipe can be crafted with the ingredients from the inventory.
	 * If an ingredient is missing try to craft it from a list of recipes.
	 */
	public static boolean canCraft(WrappedRecipe recipe, IInventory inventory,
			List<WrappedRecipe> recipesToCheck, int recursion) {
		return canCraft(recipe, inventory, false, 1, recursion) > 0;
	}

	/**
	 * Check if a recipe can be crafted with the ingredients from the inventory.
	 * If an ingredient is missing try to craft it from a list of recipes.
	 *
	 * @param recipe
	 *            - recipe to check
	 * @param inventory
	 *            - inventory to use the ingredients from
	 * @param take
	 *            - whether or not to take the ingredients from the inventory
	 * @param recursion
	 *            - how deep to recurse while trying to craft an ingredient
	 *            (must be nonnegative)
	 */
	public static int canCraft(WrappedRecipe recipe, IInventory inventory,
			boolean take, int maxTimes, int recursion) {
		if (recursion < 0) {
			return 0;
		}

		recipe.usedIngredients.clear();
		int invSize = InventoryUtils.getMainInventorySize(inventory);
		InventoryBasic tmp = new InventoryBasic("tmp", true, invSize);
		InventoryBasic tmp2 = new InventoryBasic("tmp2", true, invSize);
		InventoryUtils.setContents(tmp, inventory);

		int timesCrafted = 0;
		timesLoop: while (timesCrafted < maxTimes) {
			iiLoop: for (int ii = 0; ii < recipe.inputs.size(); ii++) {
				if (recipe.inputs.get(ii) instanceof ItemStack) {
					ItemStack ingredient = (ItemStack) recipe.inputs.get(ii);
					int inventoryIndex = isItemInInventory(ingredient, recipe,
							tmp);
					if (inventoryIndex != -1
							&& InventoryUtils.consumeItemForCrafting(tmp,
									inventoryIndex, recipe.usedIngredients)) {
						continue iiLoop;
					}
					// ingredient is not in inventory, can we craft it?
					if (recursion > 0) {
						List<WrappedRecipe> list = getRecipesForItemFromList(
								ingredient, recipe);
						for (WrappedRecipe wr : list) {
							if (canCraft(wr, tmp, true, 1, recursion - 1) > 0) {
								ItemStack is = wr.handler.getCraftingResult(wr,
										wr.usedIngredients);
								is.stackSize--;
								if (is.stackSize > 0
										&& !InventoryUtils.addItemToInventory(
												tmp, is)) {
									break timesLoop;
								}
								ItemStack usedItemStack = is.copy();
								usedItemStack.stackSize = 1;
								recipe.usedIngredients.add(usedItemStack);
								continue iiLoop;
							}
						}
					}
					// ingredient is not in inventory and we can't craft it!
					break timesLoop;
				} else if (recipe.inputs.get(ii) instanceof List) {
					@SuppressWarnings("unchecked")
					List<ItemStack> ingredients = (List<ItemStack>) recipe.inputs
							.get(ii);
					int inventoryIndex = isItemInInventory(ingredients, recipe,
							tmp);
					if (inventoryIndex != -1
							&& InventoryUtils.consumeItemForCrafting(tmp,
									inventoryIndex, recipe.usedIngredients)) {
						continue iiLoop;
					}
					// ingredient is not in inventory, can we craft it?
					if (recursion > 0) {
						List<WrappedRecipe> list = getRecipesForItemFromList(
								ingredients, recipe);
						for (WrappedRecipe wr : list) {
							if (canCraft(wr, tmp, true, 1, recursion - 1) > 0) {
								ItemStack is = wr.handler.getCraftingResult(wr,
										wr.usedIngredients);
								is.stackSize--;
								if (is.stackSize > 0
										&& !InventoryUtils.addItemToInventory(
												tmp, is)) {
									break timesLoop;
								}
								ItemStack usedItemStack = is.copy();
								usedItemStack.stackSize = 1;
								recipe.usedIngredients.add(usedItemStack);
								continue iiLoop;
							}
						}
					}
					//
					break timesLoop;
				}
			}

			timesCrafted++;
			InventoryUtils.setContents(tmp2, tmp);

		}
		if (timesCrafted > 0) {
			if (take) {
				InventoryUtils.setContents(inventory, tmp2);
			}
		}
		return timesCrafted;
	}

	// / Checks inventory size and sees if all crafted components + recipe
	// output can fit in the inventory. Used for shift-click crafting
	public static int canCraftWithComponents(WrappedRecipe recipe,
			IInventory inventory, boolean take,
			int maxTimes, int recursion) {
		if (recursion < 0) {
			return 0;
		}

		recipe.usedIngredients.clear();
		int invSize = InventoryUtils.getMainInventorySize(inventory);
		InventoryBasic tmp = new InventoryBasic("tmp", true, invSize);
		InventoryBasic tmp2 = new InventoryBasic("tmp2", true, invSize);
		InventoryUtils.setContents(tmp, inventory);

		int timesCrafted = 0;
		timesLoop: while (timesCrafted < maxTimes) {
			iiLoop: for (int ii = 0; ii < recipe.inputs.size(); ii++) {
				if (recipe.inputs.get(ii) instanceof ItemStack) {
					ItemStack ingredient = (ItemStack) recipe.inputs.get(ii);
					int inventoryIndex = isItemInInventory(ingredient, recipe,
							tmp);
					if (inventoryIndex != -1
							&& InventoryUtils.consumeItemForCrafting(tmp,
									inventoryIndex, recipe.usedIngredients)) {
						continue iiLoop;
					}
					// ingredient is not in inventory, can we craft it?
					if (recursion > 0) {
						List<WrappedRecipe> list = getRecipesForItemFromList(
								ingredient, recipe);
						for (WrappedRecipe wr : list) {
							if (canCraft(wr, tmp, true, 1,
									recursion - 1) > 0) {
								ItemStack is = wr.handler.getCraftingResult(wr,
										wr.usedIngredients);
								is.stackSize--;
								if (is.stackSize > 0
										&& !InventoryUtils.addItemToInventory(
												tmp, is)) {
									break timesLoop;
								}
								ItemStack usedItemStack = is.copy();
								usedItemStack.stackSize = 1;
								recipe.usedIngredients.add(usedItemStack);
								continue iiLoop;
							}
						}
					}
					// ingredient is not in inventory and we can't craft it!
					break timesLoop;
				} else if (recipe.inputs.get(ii) instanceof List) {
					@SuppressWarnings("unchecked")
					List<ItemStack> ingredients = (List<ItemStack>) recipe.inputs
							.get(ii);
					int inventoryIndex = isItemInInventory(ingredients, recipe,
							tmp);
					if (inventoryIndex != -1
							&& InventoryUtils.consumeItemForCrafting(tmp,
									inventoryIndex, recipe.usedIngredients)) {
						continue iiLoop;
					}
					// ingredient is not in inventory, can we craft it?
					if (recursion > 0) {
						List<WrappedRecipe> list = getRecipesForItemFromList(
								ingredients, recipe);
						for (WrappedRecipe wr : list) {
							if (canCraft(wr, tmp, true, 1, recursion - 1) > 0) {
								ItemStack is = wr.handler.getCraftingResult(wr,
										wr.usedIngredients);
								is.stackSize--;
								if (is.stackSize > 0
										&& !InventoryUtils.addItemToInventory(
												tmp, is)) {
									break timesLoop;
								}
								ItemStack usedItemStack = is.copy();
								usedItemStack.stackSize = 1;
								recipe.usedIngredients.add(usedItemStack);
								continue iiLoop;
							}
						}
					}
					//
					break timesLoop;
				}
			}

			timesCrafted++;
			InventoryUtils.setContents(tmp2, tmp);

		}
		ItemStack result = recipe.getOutput();
		if (!InventoryUtils.addItemToInventory(tmp, result)) {
			timesCrafted = 0;
		}
		if (timesCrafted > 0) {
			if (take) {
				InventoryUtils.setContents(inventory, tmp2);
			}
		}
		return timesCrafted;
	}

	private static int isItemInInventory(ItemStack is, WrappedRecipe recipe,
			IInventory inv) {
		int size = InventoryUtils.getMainInventorySize(inv);
		for (int i = 0; i < size; i++) {
			ItemStack candidate = inv.getStackInSlot(i);
			if (candidate != null
					&& recipe.handler.matchItem(is, candidate, recipe)) {
				return i;
			}
		}
		return -1;
	}

	private static int isItemInInventory(List<ItemStack> ing,
			WrappedRecipe recipe, IInventory inv) {
		for (ItemStack is : ing) {
			int slot = isItemInInventory(is, recipe, inv);
			if (slot != -1) {
				return slot;
			}
		}
		return -1;
	}

	private static List<WrappedRecipe> getRecipesForItemFromList(
			ItemStack ingredient, WrappedRecipe recipe) {
		List<WrappedRecipe> list = new ArrayList<WrappedRecipe>();
		if(ingredient != null && ingredient.getItem() != null){
			List<WrappedRecipe> recipes=RecipeManager.getProducers(ingredient.getItem());
			if(recipes!=null)
				for (WrappedRecipe wr : recipes)
					if (recipe.handler.matchItem(ingredient, wr.getOutput(), recipe))
						list.add(wr);
		}
		return list;
	}
	

	private static List<WrappedRecipe> getRecipesForItemFromList(
			List<ItemStack> ingredients, WrappedRecipe recipe) {
		List<WrappedRecipe> list = new LinkedList<WrappedRecipe>();
		for (ItemStack is : ingredients) {
			list.addAll(getRecipesForItemFromList(is, recipe));
		}
		return list;
	}

	/**
	 * Get the recipe that matches provided result and ingredients.
	 */
	public static WrappedRecipe getValidRecipe(ItemStack result,
			ItemStack[] ingredients) {
		if(result==null)return null;
		List<WrappedRecipe> all = RecipeManager.getConsumers(result.getItem());
		if(all==null)return null;
		allLoop: for (WrappedRecipe wr : all) {
			if (wr.inputs.size() == ingredients.length) {
				ingLoop: for (int i = 0; i < ingredients.length; i++) {
					if (wr.inputs.get(i) instanceof ItemStack) {
						ItemStack is = (ItemStack) wr.inputs.get(i);
						if (!StackUtils.areCraftingEquivalent(is,
								ingredients[i])) {
							continue allLoop;
						}
					} else if (wr.inputs.get(i) instanceof List) {
						if (ingredients[i].getItem() == null) {
							return null;
						}
						@SuppressWarnings("unchecked")
						List<ItemStack> ingList = (List<ItemStack>) wr.inputs
								.get(i);
						if (ingList.isEmpty()) {
							return null;
						}
						for (ItemStack is : ingList) {
							if (StackUtils.areCraftingEquivalent(is,
									ingredients[i])) {
								continue ingLoop;
							}
						}
						continue allLoop;
					}
				}
				return wr;
			}
		}
		return null;
	}

	/**
	 * How many times can we fit the resulting itemstack in players hand.
	 *
	 * @param result
	 *            itemstack we are trying to fit
	 * @param inHand
	 *            itemstack currently in hand
	 */
	public static int calculateCraftingMultiplierUntilMaxStack(
			ItemStack result, ItemStack inHand) {
		int maxTimes = MathHelper.floor_double(result.getMaxStackSize()
				/ (double) result.stackSize);
		if (inHand != null) {
			int diff = result.getMaxStackSize() - (maxTimes * result.stackSize);
			while (inHand.stackSize > diff) {
				maxTimes--;
				diff += result.stackSize;
			}
		}
		return maxTimes;
	}
}
