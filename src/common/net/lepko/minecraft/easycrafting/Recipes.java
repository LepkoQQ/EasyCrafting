package net.lepko.minecraft.easycrafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.CraftingManager;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import net.minecraft.src.ShapelessRecipes;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class Recipes {

	private static ArrayList<EasyRecipe> recipes = new ArrayList<EasyRecipe>();

	public static ArrayList<EasyRecipe> getVanillaRecipes() {
		if (recipes.isEmpty()) {
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
				} else {
					// It's a special recipe (map extending, armor dyeing, ...) - ignore
					continue;
				}
				recipes.add(new EasyRecipe(r.getRecipeOutput(), ingredients));
			}
		}

		return recipes;
	}

	public static ArrayList<EasyRecipe> getCraftableRecipes(InventoryPlayer player_inventory) {
		ArrayList<EasyRecipe> r = new ArrayList<EasyRecipe>();

		ArrayList<EasyRecipe> all = getVanillaRecipes();
		for (int i = 0; i < all.size(); i++) {
			if (hasIngredients(all.get(i).ingredients, player_inventory)) {
				r.add(all.get(i));
			}
		}

		return r;
	}

	public static boolean hasIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory) {
		return checkIngredients(ingredients, player_inventory, false, 1) == 0 ? false : true;
	}

	public static boolean takeIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory) {
		return checkIngredients(ingredients, player_inventory, true, 1) == 0 ? false : true;
	}

	public static int hasIngredientsMaxStack(ItemStack[] ingredients, InventoryPlayer player_inventory, int maxTimes) {
		return checkIngredients(ingredients, player_inventory, false, maxTimes);
	}

	public static int takeIngredientsMaxStack(ItemStack[] ingredients, InventoryPlayer player_inventory, int maxTimes) {
		return checkIngredients(ingredients, player_inventory, true, maxTimes);
	}

	private static int checkIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory, boolean take_ingredients, int maxTimes) {
		InventoryPlayer tmp = new InventoryPlayer(null);
		InventoryPlayer tmp2 = new InventoryPlayer(null);
		tmp.copyInventory(player_inventory);

		int k = 0;
		timesLoop: while (k < maxTimes) {
			ingLoop: for (int i = 0; i < ingredients.length; i++) {
				if (ingredients[i] != null) {
					for (int j = 0; j < tmp.mainInventory.length; j++) {
						if (tmp.mainInventory[j] != null) {
							if (tmp.mainInventory[j].itemID == ingredients[i].itemID && (tmp.mainInventory[j].getItemDamage() == ingredients[i].getItemDamage() || ingredients[i].getItemDamage() == -1)) {
								ItemStack stack = tmp.getStackInSlot(j);
								tmp.decrStackSize(j, 1);
								if (stack.getItem().hasContainerItem()) {
									ItemStack containerStack = stack.getItem().getContainerItemStack(stack);
									// TODO: damage Items that take damage when crafting with them
									if (containerStack.isItemStackDamageable() && containerStack.getItemDamage() > containerStack.getMaxDamage()) {
										containerStack = null;
									}
									if (containerStack != null && !tmp.addItemStackToInventory(containerStack)) {
										break timesLoop;
									}
								}
								continue ingLoop;
							}
						}
					}
					break timesLoop;
				}
			}
			tmp2.copyInventory(tmp);
			k++;
		}

		if (take_ingredients && k > 0) {
			player_inventory.copyInventory(tmp2);
		}
		return k;
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
					if (count == ingredients.length) {
						return r;
					}
				}
			}
		}

		return null;
	}

	public static int calculateCraftingMultiplierUntilMaxStack(ItemStack recipe_result, ItemStack inHand) {
		// TODO: there has to be a better way to calculate this
		int maxTimes = (int) ((double) recipe_result.getMaxStackSize() / (double) recipe_result.stackSize);
		if (inHand != null) {
			int diff = recipe_result.getMaxStackSize() - (maxTimes * recipe_result.stackSize);
			if (inHand.stackSize > diff) {
				maxTimes -= (int) (((double) (inHand.stackSize - diff) / (double) recipe_result.stackSize) + 1);
			}
		}
		//
		return maxTimes;
	}

	@SideOnly(Side.CLIENT)
	public static EasyRecipe getValidRecipe(GuiEasyCrafting gui, int slot_index, ItemStack inHand, ItemStack is) {
		int i = slot_index + (gui.currentScroll * 8);
		ArrayList<EasyRecipe> rl = gui.renderList;
		if (i < rl.size() && rl.get(i) != null) {
			EasyRecipe r = rl.get(i);
			if (r.result.itemID == is.itemID && r.result.getItemDamage() == is.getItemDamage() && gui.craftableList.contains(r)) {
				if (inHand == null && r.result.stackSize == is.stackSize) {
					return r;
				} else if (inHand != null && (inHand.stackSize + r.result.stackSize) == is.stackSize) {
					return r;
				}
			}
		}
		return null;
	}
}
