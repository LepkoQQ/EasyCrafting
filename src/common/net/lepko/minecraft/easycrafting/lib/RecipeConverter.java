package net.lepko.minecraft.easycrafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.CraftingManager;
import net.minecraft.src.IRecipe;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import net.minecraft.src.ShapelessRecipes;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * @author      UberWaffe
 * 
 * This library fetches recipes from lists or crafting managers, and converts them to easyrecipe lists.
 */
public class RecipeConverter {

	/** Stores a recipelist of all the standard crafting manager recipes, converted to easyrecipes */
	public static ArrayList<EasyRecipe> convertedStandardCraftingManagerRecipes = new ArrayList<EasyRecipe>();

	/**
	 * Fetches all recipes from the standard crafting manager, and converts them to easycraft recipes.
	 *
	 * @param  N/A
	 * @return 		A list of all standard crafting manager recipes converted to easycraft recipes
	 */
	public static ArrayList<EasyRecipe> fetchAllCraftmanagerRecipes() {
		if(convertedStandardCraftingManagerRecipes.isEmpty()) {
			ArrayList<EasyRecipe> finalList = new ArrayList<EasyRecipe>();
			long beforeTime = System.nanoTime();

			//Get all standard crafting manager recipes.
			List temp_recipes = CraftingManager.getInstance().getRecipeList(); 
			int skipped = 0;

			//Change fetched recipes to easycraft recipes
			for (int i = 0; i < temp_recipes.size(); i++) {
				boolean usesOreDict = false;
				IRecipe r = (IRecipe) temp_recipes.get(i);
				if(Version.DEBUG) {
					System.out.println("Processing recipe " + i + ": " + r + "  outputting: " + r.getRecipeOutput());
				}
				ItemStack[] ingredients = null;
				if (r instanceof ShapedRecipes) {
					ingredients = ReflectionHelper.<ItemStack[], ShapedRecipes> getPrivateValue(ShapedRecipes.class, (ShapedRecipes) r, "recipeItems");
				} else if (r instanceof ShapelessRecipes) {
					List<ItemStack> tmp = ReflectionHelper.<List<ItemStack>, ShapelessRecipes> getPrivateValue(ShapelessRecipes.class, (ShapelessRecipes) r, "recipeItems");
					ingredients = tmp.toArray(new ItemStack[0]);
				} else if (r instanceof ShapedOreRecipe) {
					//Makes use of the forge ore dictionary
					ShapedOreRecipe tempRecipe = (ShapedOreRecipe) temp_recipes.get(i);
					usesOreDict = true;
					continue;
					//Object[] ingredientList = tempRecipe.input;
					//if(Version.DEBUG) {
					//	System.out.println("Oredict Test: " + ingredientList[0]);
					//}
				} else { 
					// It's a special recipe (map extending, armor dyeing, ...) - ignore
					skipped++;
					if(Version.DEBUG) {
						System.out.println(skipped + ": Skipped recipe: " + r);
					}
					continue;
				}
				if (r.getRecipeOutput().toString().contains("item.cart.tank")) {
					skipped++;
					if(Version.DEBUG) {
						System.out.println(skipped + ": Skipped recipe with Tank Cart: " + r.getRecipeOutput());
					}
					continue;
				}
				//The reason this debug output is interesting, is because apparently there are recipes that have
				//itemstacks with sizes not equal to 0. I assume this is never a problem since nobody uses stacksize
				//when checking crafting recipes, until I tried to. So this debug code helps to spot the "odd-ones" so
				//we can decide how to handle it, if we go the itemstack.stacksize route again.
				if(Version.DEBUG) {
					for(int ingCounter = 0; ingCounter < ingredients.length; ingCounter++) {
						System.out.println(ingCounter + " of " + ingredients.length + " : " + ingredients[ingCounter]);
						if(ingredients[ingCounter] != null) {
							if(ingredients[ingCounter].stackSize != 1) {
								System.out.println("!!!WARNING!!! Stacksize:" + ingredients[ingCounter].stackSize);
							}
						}
					}
				}
				if(usesOreDict) {
					finalList.add(new EasyRecipe(r.getRecipeOutput(), ingredients, true));
				} else {
					finalList.add(new EasyRecipe(r.getRecipeOutput(), ingredients));
				}
			}
			convertedStandardCraftingManagerRecipes = finalList;

			if(Version.DEBUG) {
				System.out.println(String.format("Returning %d available recipes! ---- Total time: %.8f", finalList.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
			}
		}
		return convertedStandardCraftingManagerRecipes;
	}
}
