package net.lepko.minecraft.easycrafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lepko.minecraft.easycrafting.block.GuiEasyCrafting;
import net.lepko.minecraft.easycrafting.easyobjects.EasyItemStack;
import net.lepko.minecraft.easycrafting.easyobjects.EasyRecipe;
import net.lepko.minecraft.easycrafting.helpers.EasyLog;
import net.lepko.minecraft.easycrafting.helpers.InventoryHelper;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import net.minecraft.src.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class Recipes {

	private static ArrayList<EasyRecipe> recipes = new ArrayList<EasyRecipe>();

	public static ArrayList<EasyRecipe> getAllRecipes() {
		if (recipes.isEmpty()) {
			long beforeTime = System.nanoTime();

			List temp_recipes = CraftingManager.getInstance().getRecipeList();
			int skipped = 0;

			for (int i = 0; i < temp_recipes.size(); i++) {
				IRecipe r = (IRecipe) temp_recipes.get(i);
				List ingredients = null;
				// TODO: in future versions of forge you don't have to use reflections anymore, fields are exposed
				if (r instanceof ShapedRecipes) {
					ItemStack[] input = ReflectionHelper.<ItemStack[], ShapedRecipes> getPrivateValue(ShapedRecipes.class, (ShapedRecipes) r, 2);
					ingredients = new ArrayList(Arrays.asList(input));
				} else if (r instanceof ShapelessRecipes) {
					List input = ReflectionHelper.<List, ShapelessRecipes> getPrivateValue(ShapelessRecipes.class, (ShapelessRecipes) r, 1);
					ingredients = new ArrayList(input);
				} else if (r instanceof ShapedOreRecipe) {
					Object[] input = ReflectionHelper.<Object[], ShapedOreRecipe> getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe) r, 3);
					ingredients = new ArrayList(Arrays.asList(input));
				} else if (r instanceof ShapelessOreRecipe) {
					List input = ReflectionHelper.<List, ShapelessOreRecipe> getPrivateValue(ShapelessOreRecipe.class, (ShapelessOreRecipe) r, 1);
					ingredients = new ArrayList(input);
				} else {
					String className = r.getClass().getName();
					if (className.equals("ic2.common.AdvRecipe") || className.equals("ic2.common.AdvShapelessRecipe")) {
						try {
							Object[] input = (Object[]) Class.forName(className).getField("input").get(r);
							ingredients = new ArrayList(Arrays.asList(input));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						// It's a special recipe (map extending, armor dyeing, ...) - ignore
						// TODO: add IC2 and any other custom recipe classes
						skipped++;
						EasyLog.log(skipped + ": Skipped recipe: " + r);
						continue;
					}
				}
				if (r.getRecipeOutput().toString().contains("item.cart.tank")) {
					skipped++;
					EasyLog.log(skipped + ": Skipped recipe with Tank Cart: " + r.getRecipeOutput());
					continue;
				}
				recipes.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
			}

			EasyLog.log(String.format("Returning %d available recipes! ---- Total time: %.8f", recipes.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
		}
		return recipes;
	}

	public static ArrayList<EasyRecipe> getCraftableRecipes(InventoryPlayer player_inventory) {
		long beforeTime = System.nanoTime();

		ArrayList<EasyRecipe> craftableRecipes = new ArrayList<EasyRecipe>();
		ArrayList<EasyRecipe> allRecipes = getAllRecipes();
		for (int i = 0; i < allRecipes.size(); i++) {
			if (hasIngredients(allRecipes.get(i), player_inventory, 0)) {
				craftableRecipes.add(allRecipes.get(i));
			}
		}

		EasyLog.log(String.format("Returning %d craftable out of %d available recipes! ---- Total time: %.8f", craftableRecipes.size(), recipes.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
		return craftableRecipes;
	}

	public static boolean hasIngredients(EasyRecipe recipe, InventoryPlayer player_inventory, int recursionCount) {
		return InventoryHelper.checkIngredients(recipe, player_inventory, false, 1, recursionCount) == 0 ? false : true;
	}

	public static boolean takeIngredients(EasyRecipe recipe, InventoryPlayer player_inventory, int recursionCount) {
		return InventoryHelper.checkIngredients(recipe, player_inventory, true, 1, recursionCount) == 0 ? false : true;
	}

	public static int hasIngredientsMaxStack(EasyRecipe recipe, InventoryPlayer player_inventory, int maxTimes, int recursionCount) {
		return InventoryHelper.checkIngredients(recipe, player_inventory, false, maxTimes, recursionCount);
	}

	public static int takeIngredientsMaxStack(EasyRecipe recipe, InventoryPlayer player_inventory, int maxTimes, int recursionCount) {
		return InventoryHelper.checkIngredients(recipe, player_inventory, true, maxTimes, recursionCount);
	}

	public static ArrayList<EasyRecipe> getValidRecipe(ItemStack result) {
		ArrayList<EasyRecipe> list = new ArrayList<EasyRecipe>();
		ArrayList<EasyRecipe> all = getAllRecipes();
		for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.getResult().getID() == result.itemID && (r.getResult().getDamage() == result.getItemDamage() || result.getItemDamage() == -1)) {
				list.add(r);
			}
		}
		return list;
	}

	public static EasyRecipe getValidRecipe(int hashCode) {
		ArrayList<EasyRecipe> all = getAllRecipes();
		for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.hashCode() == hashCode) {
				return r;
			}
		}
		return null;
	}

	public static EasyRecipe getValidRecipe(ItemStack result, ItemStack[] ingredients) {
		ArrayList<EasyRecipe> all = getAllRecipes();
		allLoop: for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.getResult().getID() == result.itemID && r.getResult().getDamage() == result.getItemDamage()) {
				int j = 0;
				int count = 0;
				while (j < r.getIngredientsSize()) {
					if (r.getIngredient(j) instanceof EasyItemStack) {
						EasyItemStack eis = (EasyItemStack) r.getIngredient(j);
						if (eis.getID() != ingredients[count].itemID || eis.getDamage() != ingredients[count].getItemDamage()) {
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
			if (r.getResult().equalsItemStack(is, true) && gui.craftableList.contains(r)) {
				if (inHand == null && r.getResult().getSize() == is.stackSize) {
					return r;
				} else if (inHand != null && (inHand.stackSize + r.getResult().getSize()) == is.stackSize) {
					return r;
				}
			}
		}
		return null;
	}
}
