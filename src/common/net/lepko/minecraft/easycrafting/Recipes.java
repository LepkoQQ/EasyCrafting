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

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class handles the checking and crafting of the easycraft recipes.
 */
public class Recipes {

	/** Stores a recipelist of all the craftable recipes (independent of ingredient availability) */
	private static ArrayList<EasyRecipe> recipes = new ArrayList<EasyRecipe>();

	/**
	 * Fetches all recipes from the standard crafting manager, and converts them to easycraft recipes.
	 *
	 * @param  N/A
	 * @return 		A list of all standard crafting manager recipes converted to easycraft recipes
	 */
	public static ArrayList<EasyRecipe> getAllRecipes() {
		if (recipes.isEmpty()) {
			long beforeTime = System.nanoTime();

			//Get all standard crafting manager recipes.
			List temp_recipes = CraftingManager.getInstance().getRecipeList();
			int skipped = 0;

			//Change fetched recipes to easycraft recipes
			for (int i = 0; i < temp_recipes.size(); i++) {
				IRecipe r = (IRecipe) temp_recipes.get(i);
				ItemStack[] ingredients = null;
				if (r instanceof ShapedRecipes) {
					ingredients = ReflectionHelper.<ItemStack[], ShapedRecipes> getPrivateValue(ShapedRecipes.class, (ShapedRecipes) r, 2);
				} else if (r instanceof ShapelessRecipes) {
					List<ItemStack> tmp = ReflectionHelper.<List<ItemStack>, ShapelessRecipes> getPrivateValue(ShapelessRecipes.class, (ShapelessRecipes) r, 1);
					ingredients = tmp.toArray(new ItemStack[0]);
				} else {
					// It's a special recipe (map extending, armor dyeing, ...) - ignore
					// TODO: Handle OreDict recipes
					skipped++;
					System.out.println(skipped + ": Skipped recipe: " + r);
					continue;
				}
				if (r.getRecipeOutput().toString().contains("item.cart.tank")) {
					skipped++;
					System.out.println(skipped + ": Skipped recipe with Tank Cart: " + r.getRecipeOutput());
					continue;
				}
				recipes.add(new EasyRecipe(r.getRecipeOutput(), ingredients));
			}

			System.out.println(String.format("Returning %d available recipes! ---- Total time: %.8f", recipes.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
		}
		return recipes;
	}

	/**
	 * Determines if recipes are craftable based on the availability of ingredients in the player's inventory
	 *
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @return 						A list of all craftable easy recipes (based on ingredient availability)
	 */
	public static ArrayList<EasyRecipe> getCraftableRecipes(InventoryPlayer player_inventory) {
		long beforeTime = System.nanoTime();

		ArrayList<EasyRecipe> r = new ArrayList<EasyRecipe>();
		ArrayList<EasyRecipe> all = getAllRecipes();
		for (int i = 0; i < all.size(); i++) {
			if (hasIngredients(all.get(i).ingredients, player_inventory, 0)) {
				r.add(all.get(i));
			}
		}

		System.out.println(String.format("Returning %d craftable out of %d available recipes! ---- Total time: %.8f", r.size(), recipes.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
		return r;
	}

	/**
	 * Determines if the ingredients to craft the recipe, 1x times, is available.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as a list of itemstacks
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						True, if the player has the ingredients (or can intermediately craft them based on recursioncount) to make this recipe. False, if not.
	 */
	public static boolean hasIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory, int recursionCount) {
		return checkIngredients(ingredients, player_inventory, false, 1, recursionCount) == 0 ? false : true;
	}

	/**
	 * Removes the ingredients from the player's inventory to craft the recipe, 1x times.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as a list of itemstacks
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						True, if the ingredients to make this recipe was removed. False, if not.
	 */
	public static boolean takeIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory, int recursionCount) {
		return checkIngredients(ingredients, player_inventory, true, 1, recursionCount) == 0 ? false : true;
	}

	/**
	 * Determines if the ingredients to craft the recipe, maxTimes times, is available.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as a list of itemstacks
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  maxTimes				Check if the recipe can be crafted as much as possible, up to this amount.
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						The maximum number of times the recipe can be crafted (up to maxTimes).
	 */
	public static int hasIngredientsMaxStack(ItemStack[] ingredients, InventoryPlayer player_inventory, int maxTimes, int recursionCount) {
		return checkIngredients(ingredients, player_inventory, false, maxTimes, recursionCount);
	}

	/**
	 * Removes the ingredients from the player's inventory to craft the recipe, as many times as possible, up to maxTimes times.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as a list of itemstacks
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  maxTimes				Craft the recipe as much as possible, up to this amount.
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						The number of times the recipe was 'crafted' and the ingredients were removed (up to maxTimes).
	 */
	public static int takeIngredientsMaxStack(ItemStack[] ingredients, InventoryPlayer player_inventory, int maxTimes, int recursionCount) {
		return checkIngredients(ingredients, player_inventory, true, maxTimes, recursionCount);
	}

	/**
	 * Checks how many times the requested recipe can be crafted, given the available ingredients. Allowing up to (recursionCount - 1) intermediate steps.
	 * 
	 * This function checks through each slot of the player inventory, and removes an item from the slot if it matches the ingredient from the requested recipe.
	 * If all ingredients can in this manner be found and 'removed' from the inventory, the player has enough ingredients to craft the recipe.
	 * If it is flagged that we should actually take the ingredients (the recipe is being crafted, not just being checked) then we update the given player inventory
	 * to reflect the taken items.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as a list of itemstacks
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  maxTimes				Craft the recipe as much as possible, up to this amount.
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						The number of times the recipe was 'crafted' and the ingredients were removed (up to maxTimes).
	 */
	private static int checkIngredients(ItemStack[] ingredients, InventoryPlayer player_inventory, boolean take_ingredients, int maxTimes, int recursionCount) {
		//How many intermediate steps are allowed (recursionCount - 1)
		//Ex: Player wants sticks, but has only oak wood. This is to allow for checking Oak Wood -> Planks and then Planks -> Sticks.
		if (recursionCount >= ModEasyCrafting.instance.allowMultiStepRecipes) {
			return 0;
		}

		//We setup two fake inventories. tmp is to check if we can craft the requested item,
		//and tmp2 is to remember what the inventory looked like after the last successful craft (i.e. after the last easyrecipe check that found all needed ingredients).
		InventoryPlayer tmp = new InventoryPlayer(null);
		InventoryPlayer tmp2 = new InventoryPlayer(null);

		tmp.copyInventory(player_inventory);

		int k = 0;
		timesLoop: while (k < maxTimes) {
			//Check if all ingredients of the recipe are available.
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
					//If we get here, we didn't find the ingredient in the inventory. So we check if we can do an intermediary recipe to provide us
					//with the ingredients we need.
					if ((recursionCount + 1) < ModEasyCrafting.instance.allowMultiStepRecipes) {
						//Find a valid recipe that outputs the ingredients we need.
						ArrayList<EasyRecipe> rList = getValidRecipe(ingredients[i]);
						if (!rList.isEmpty()) {
							for (int l = 0; l < rList.size(); l++) {
								EasyRecipe ingRecipe = rList.get(l);
								//[Note: Possible cause of the repeatable bug here. This "tmp.addItemStackToInventory" might be causing the crash if no valid slot is available.]
								if (takeIngredients(ingRecipe.ingredients, tmp, recursionCount + 1) && tmp.addItemStackToInventory(ingRecipe.result.copy())) {
									// Try to take the same ingredient again.
									i--;
									continue ingLoop;
								}
							}
						}
					}
					//If we get to this point, we didn't have enough ingredients, and could craft more with what the player had. So we stop trying to make more.
					break timesLoop;
				}
			}
			//After the last check for making a recipe, remember what the inventory looked like, in case we cannot repeat the recipe craft again and need to revert.
			tmp2.copyInventory(tmp);
			k++;
		}
		
		if (take_ingredients && k > 0) {
			player_inventory.copyInventory(tmp2);
		}
		return k;
	}

	/**
	 * Find all recipse that result in the requested itemstack.
	 *
	 * @param  result	The itemstack that we want. All recipes that craft into these will be returned.
	 * @return 			A list of all the recipes that output the requested itemstack.
	 */
	public static ArrayList<EasyRecipe> getValidRecipe(ItemStack result) {
		ArrayList<EasyRecipe> list = new ArrayList<EasyRecipe>();
		ArrayList<EasyRecipe> all = getAllRecipes();
		for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.result.itemID == result.itemID && (r.result.getItemDamage() == result.getItemDamage() || result.getItemDamage() == -1)) {
				list.add(r);
			}
		}
		return list;
	}

	/**
	 * Finds the first recipe that results in the requested itemstack, while using the requested ingredients.
	 *
	 * @param  result		The itemstack the recipe should return.
	 * @param  ingredients	A list of itemstacks that the recipe should use as ingredients.
	 * @return 				An easyrecipe that meets the criteria, or null if nno valid recipe found.
	 */
	public static EasyRecipe getValidRecipe(ItemStack result, ItemStack[] ingredients) {
		ArrayList<EasyRecipe> all = getAllRecipes();
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

	/**
	 * Determines how many times the requested itemstack can be stacked before it passes its maxstack limit.
	 *
	 * @param  recipe_result	The itemstack that should be stacked multiple times.
	 * @param  inHand			The itemstack it should be stacked atop of (essentially, the starting number in the stack).
	 * @return 					The number of times the requested itemstack could be stacked atop the starting stack, before the max stack size was reached.
	 */
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

	/**
	 * Find if recipe from the gui, from the given slot, produces the stack that the player has in their hand.
	 * 
	 * Essentially, check that the player had an empty hand, or was holding the same type of item as the recipe, when they clicked on the outputslot recipe.
	 *
	 * @param  gui			The gui to fetch the recipe form
	 * @param  slot_index	The slot in the gui to fetch the recipe from
	 * @param  inHand		The itemstack in the player's hand
	 * @param  is			The itemstack result that the player should obtain in the end.
	 * @return 				The recipe in the requested gui+slot if the conditions are met, null if not.
	 */
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
