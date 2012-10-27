package net.lepko.minecraft.easycrafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.CraftingManager;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import net.minecraft.src.ShapelessRecipes;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class Recipes {

	private static ArrayList<EasyRecipe> recipes = new ArrayList<EasyRecipe>();

	public static ArrayList<EasyRecipe> getVanillaRecipes() {
		if (recipes.isEmpty()) {
			// recipes = ReflectionHelper.<ArrayList, CraftingManager> getPrivateValue(CraftingManager.class, CraftingManager.getInstance(), 1);
			List temp_recipes = CraftingManager.getInstance().getRecipeList();

			for (int i = 0; i < temp_recipes.size(); i++) {
				IRecipe r = (IRecipe) temp_recipes.get(i);
				ItemStack[] ingredients = null;
				if (r instanceof ShapedRecipes) {
					ingredients = ReflectionHelper.<ItemStack[], ShapedRecipes> getPrivateValue(ShapedRecipes.class, (ShapedRecipes) r, 2);
				} else if (r instanceof ShapelessRecipes) {
					List tmp = ReflectionHelper.<List, ShapelessRecipes> getPrivateValue(ShapelessRecipes.class, (ShapelessRecipes) r, 1);
					ingredients = new ItemStack[tmp.size()];
					for (int j = 0; j < tmp.size(); j++) {
						ingredients[j] = (ItemStack) tmp.get(j);
					}
				}
				recipes.add(new EasyRecipe(r.getRecipeOutput(), ingredients));
			}
		}

		// System.out.println("Returning " + recipes.size() + " recipes!");
		return recipes;
	}

	public static ArrayList<EasyRecipe> getCraftableItems(InventoryPlayer player_inventory) {
		ArrayList<EasyRecipe> r = new ArrayList<EasyRecipe>();

		ArrayList<EasyRecipe> all = getVanillaRecipes();
		for (int i = 0; i < all.size(); i++) {
			if (hasIngredients(all.get(i).ingredients, player_inventory)) {
				r.add(all.get(i));
			}
		}

		// System.out.println("Returning " + r.size() + " craftable recipes!");
		return r;
	}

	public static boolean hasIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory) {
		return checkIngredients(ingredients, player_inventory, false);
	}

	public static boolean takeIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory) {
		return checkIngredients(ingredients, player_inventory, true);
	}

	private static boolean checkIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory, boolean take_ingredients) {
		InventoryPlayer tmp = new InventoryPlayer(null);
		tmp.copyInventory(player_inventory);

		ingLoop: for (int i = 0; i < ingredients.length; i++) {
			if (ingredients[i] != null) {
				for (int j = 0; j < tmp.mainInventory.length; j++) {
					if (tmp.mainInventory[j] != null) {
						if (tmp.mainInventory[j].itemID == ingredients[i].itemID && (tmp.mainInventory[j].getItemDamage() == ingredients[i].getItemDamage() || ingredients[i].getItemDamage() == -1)) {
							tmp.decrStackSize(j, 1);
							continue ingLoop;
						}
					}
				}
				return false;
			}
		}

		if (take_ingredients) {
			player_inventory.copyInventory(tmp);
		}

		return true;
	}

	public static EasyRecipe getValidRecipe(ItemStack result, ItemStack[] ingredients) {

		ArrayList<EasyRecipe> all = getVanillaRecipes();
		allLoop: for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.result.itemID == result.itemID && r.result.getItemDamage() == result.getItemDamage()) {
				int j = 0;
				int count = 0;
				while (j < r.ingredients.length) {
					if (r.ingredients[j] == null) {
						j++;
						continue;
					}

					if (r.ingredients[j].itemID != ingredients[count].itemID || r.ingredients[j].getItemDamage() != ingredients[count].getItemDamage()) {
						continue allLoop;
					}

					j++;
					count++;

					// Completed the last check and didn't fail
					if (j == r.ingredients.length) {
						return r;
					}
				}
			}
		}

		return null;
	}
}
