package net.lepko.minecraft.easycrafting;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field; 
import java.lang.reflect.Method; 

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
				Object[] ingredients = null;
				if (r instanceof ShapedRecipes) {
					ingredients = ReflectionHelper.<ItemStack[], ShapedRecipes> getPrivateValue(ShapedRecipes.class, (ShapedRecipes) r, "recipeItems");
				} else if (r instanceof ShapelessRecipes) {
					List<ItemStack> tmp = ReflectionHelper.<List<ItemStack>, ShapelessRecipes> getPrivateValue(ShapelessRecipes.class, (ShapelessRecipes) r, "recipeItems");
					ingredients = tmp.toArray(new ItemStack[0]);
				} else if (r instanceof ShapedOreRecipe) {
					//Makes use of the forge ore dictionary
					usesOreDict = true;
					Object[] recipeInput;
					
					//Do the whole private variable hack thing.
					try {
						recipeInput = hackShapedOreRecipe(temp_recipes.get(i));
					}
					catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					
					for (int ingCounter = 0; ingCounter < recipeInput.length; ingCounter++) {
						if(Version.DEBUG) {
							System.out.println("Hack return value: " + recipeInput[ingCounter]);
						}
					}
					ingredients = recipeInput;

				}  else if (r instanceof ShapelessOreRecipe) {
					//Makes use of the forge ore dictionary
					usesOreDict = true;
					ArrayList recipeInput;
					
					//Do the whole private variable hack thing.
					try {
						recipeInput = hackShapelessOreRecipe(temp_recipes.get(i));
					}
					catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					
					Object[] returnVar = new Object[recipeInput.size()];
					for (int ingCounter = 0; ingCounter < recipeInput.size(); ingCounter++) {
						returnVar[ingCounter] = recipeInput.get(ingCounter);
						if(Version.DEBUG) {
							System.out.println("Hack return value: " + recipeInput.get(ingCounter));
						}
					}
					ingredients = returnVar;

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
				//itemstacks with sizes not equal to 1. I assume this is never a problem since nobody uses stacksize
				//when checking crafting recipes, until I tried to. So this debug code helps to spot the "odd-ones" so
				//we can decide how to handle it, if we go the itemstack.stacksize route again.
				if(Version.DEBUG) {
					for(int ingCounter = 0; ingCounter < ingredients.length; ingCounter++) {
						System.out.println(ingCounter + " of " + ingredients.length + " : " + ingredients[ingCounter]);
						if (!(ingredients[ingCounter] instanceof ArrayList)) {
							ItemStack tmpIngredient = (ItemStack) ingredients[ingCounter];
							if(tmpIngredient != null) {
								if(tmpIngredient.stackSize != 1) {
									System.out.println("!!!WARNING!!! Stacksize:" + tmpIngredient.stackSize);
								}
							}
						}
					}
				}
				if(usesOreDict) {
					finalList.add(new EasyRecipe(r.getRecipeOutput(), ingredients, true));
				} else {
					finalList.add(new EasyRecipe(r.getRecipeOutput(), ingredients, false));
				}
			}
			convertedStandardCraftingManagerRecipes = finalList;

			if(Version.DEBUG) {
				System.out.println(String.format("Returning %d available recipes! ---- Total time: %.8f", finalList.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
			}
		}
		return convertedStandardCraftingManagerRecipes;
	}
	
	public static Object[] hackShapedOreRecipe(Object theRecipeToHack) throws IllegalAccessException, IllegalArgumentException {
		ShapedOreRecipe temp = (ShapedOreRecipe) theRecipeToHack;
	
		Class shapedOreClass = temp.getClass();
		//  Print all the field names & values
		Field fields[] = shapedOreClass.getDeclaredFields();
		fields[3].setAccessible(true); 

		Object[] recipeInput = (Object[]) fields[3].get(theRecipeToHack);
		return recipeInput;
	}

	public static ArrayList hackShapelessOreRecipe(Object theRecipeToHack) throws IllegalAccessException, IllegalArgumentException {
		ShapelessOreRecipe temp = (ShapelessOreRecipe) theRecipeToHack;
	
		Class shapelessOreClass = temp.getClass();
		//  Print all the field names & values
		Field fields[] = shapelessOreClass.getDeclaredFields();
		fields[1].setAccessible(true); 

		ArrayList recipeInput = (ArrayList) fields[1].get(theRecipeToHack);
		return recipeInput;
	}
}
