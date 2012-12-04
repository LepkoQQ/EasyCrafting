package net.lepko.easycrafting.helpers;

import java.util.ArrayList;
import java.util.List;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

@SideOnly(Side.CLIENT)
public class RecipeHelperNew {

	public static ArrayList<EasyRecipe> getCraftableRecipes(InventoryPlayer inventory, int maxRecursion, List<EasyRecipe> recipesToCheck) {
		ArrayList<EasyRecipe> tmpCraftable = new ArrayList<EasyRecipe>();
		ArrayList<EasyRecipe> tmpAll = new ArrayList<EasyRecipe>(recipesToCheck);

		for (EasyRecipe er : tmpAll) {
			if (canCraft(er, inventory)) {
				tmpCraftable.add(er);
			}
		}
		tmpAll.removeAll(tmpCraftable);

		if (!tmpCraftable.isEmpty()) {
			for (int recursion = 0; recursion < maxRecursion; recursion++) {
				if (tmpAll.isEmpty()) {
					break;
				}

				ImmutableList<EasyRecipe> immutableCraftable = ImmutableList.copyOf(tmpCraftable);
				for (EasyRecipe er : tmpAll) {
					if (canCraft(er, inventory, immutableCraftable, maxRecursion)) {
						tmpCraftable.add(er);
					}
				}
				tmpAll.removeAll(tmpCraftable);

				if (immutableCraftable.size() == tmpCraftable.size()) {
					break;
				}
			}

		}
		return tmpCraftable;
	}

	private static boolean canCraft(EasyRecipe recipe, InventoryPlayer inventory) {
		return canCraft(recipe, inventory, null, 0);
	}

	private static boolean canCraft(EasyRecipe recipe, InventoryPlayer inventory, ImmutableList<EasyRecipe> recipesToCheck, int recursion) {
		return canCraft(recipe, inventory, recipesToCheck, false, recursion);
	}

	private static boolean canCraft(EasyRecipe recipe, InventoryPlayer inventory, ImmutableList<EasyRecipe> recipesToCheck, boolean take, int recursion) {
		if (recursion < 0) {
			return false;
		}

		InventoryPlayer tmp = new InventoryPlayer(inventory.player);
		tmp.copyInventory(inventory);

		ArrayList<ItemStack> usedIngredients = new ArrayList<ItemStack>();

		iiLoop: for (int ii = 0; ii < recipe.getIngredientsSize(); ii++) {
			if (recipe.getIngredient(ii) instanceof EasyItemStack) {
				EasyItemStack ingredient = (EasyItemStack) recipe.getIngredient(ii);
				int inventoryIndex = InventoryHelper.isItemInInventory(tmp, ingredient);
				if (inventoryIndex != -1 && InventoryHelper.consumeItemForCrafting(tmp, inventoryIndex, usedIngredients)) {
					continue;
				}
				//
				if (recipesToCheck != null && (recursion - 1) >= 0) {
					ArrayList<EasyRecipe> list = getRecipesForIngredientFromList(ingredient, recipesToCheck);
					for (EasyRecipe er : list) {
						if (canCraft(er, tmp, recipesToCheck, true, (recursion - 1))) {
							if (!tmp.addItemStackToInventory(er.getResult().toItemStack())) {
								return false;
							}
							continue iiLoop;
						}
					}
				}
				//
				return false;
			} else if (recipe.getIngredient(ii) instanceof ArrayList) {
				ArrayList<ItemStack> ingredients = (ArrayList<ItemStack>) recipe.getIngredient(ii);
				int inventoryIndex = InventoryHelper.isAnyItemInInventory(tmp, ingredients);
				if (inventoryIndex != -1 && InventoryHelper.consumeItemForCrafting(tmp, inventoryIndex, usedIngredients)) {
					continue;
				}
				//
				if (recipesToCheck != null && (recursion - 1) >= 0) {
					ArrayList<EasyRecipe> list = getRecipesForIngredientsFromList(ingredients, recipesToCheck);
					for (EasyRecipe er : list) {
						if (canCraft(er, tmp, recipesToCheck, true, (recursion - 1))) {
							if (!tmp.addItemStackToInventory(er.getResult().toItemStack())) {
								return false;
							}
							continue iiLoop;
						}
					}
				}
				//
				return false;
			}
		}

		if (take) {
			inventory.copyInventory(tmp);
		}
		return true;
	}

	private static ArrayList<EasyRecipe> getRecipesForIngredientFromList(EasyItemStack ingredient, ImmutableList<EasyRecipe> recipesToCheck) {
		ArrayList<EasyRecipe> returnList = new ArrayList<EasyRecipe>();
		for (EasyRecipe er : recipesToCheck) {
			if (er.getResult().equals(ingredient, true)) {
				returnList.add(er);
			}
		}
		return returnList;
	}

	private static ArrayList<EasyRecipe> getRecipesForIngredientsFromList(ArrayList<ItemStack> ingredients, ImmutableList<EasyRecipe> recipesToCheck) {
		ArrayList<EasyRecipe> returnList = new ArrayList<EasyRecipe>();
		for (ItemStack is : ingredients) {
			returnList.addAll(getRecipesForIngredientFromList(EasyItemStack.fromItemStack(is), recipesToCheck));
		}
		return returnList;
	}

}
