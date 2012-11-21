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
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class handles the checking and crafting of the easycraft recipes.
 */
public class RecipesManager {

	/** Stores a recipelist of all the craftable recipes (independent of ingredient availability) */
	public ArrayList<EasyRecipe> allowedRecipes;

	/**
	 * Creates an instance of this class
	 *
	 * @param  N/A
	 * @return N/A
	 */
	public RecipesManager() {
		this(RecipeConverter.fetchAllCraftmanagerRecipes());
	}
	
	/**
	 * Creates an instance of this class
	 *
	 * @param  	recipeList	The list of easycraft recipes that this manager can use;
	 * @return N/A
	 */
	public RecipesManager(ArrayList<EasyRecipe> recipeList) {
		this.allowedRecipes = recipeList;
	}
	
	/**
	 * Returns the allowedRecipes arraylist.
	 *
	 * @param  N/A
	 * @return 		The allowedRecipes arraylist for this instance.
	 */
	public ArrayList<EasyRecipe> getAllowedRecipes() {
		return this.allowedRecipes;
	}
	
	/**
	 * Adds the given recipe to the allowedRecipes arraylist.
	 *
	 * @param	recipeToAdd	The recipe to add to the allowed recipe list.
	 * @return 	Always returns true;
	 */
	public boolean addAllowedRecipe( EasyRecipe recipeToAdd) {
		this.allowedRecipes.add(recipeToAdd);
		//TODO: Add in checking of allowed/disallowed categories, and return false if some of the recipes in the specified list were rejected based on categories.
		return true;
	}
	
	/**
	 * Sets the allowedRecipes arraylist to the provided list.
	 *
	 * @param	recipeList	The recipelist to set as the allowed recipe list.
	 * @return 	Always returns true;
	 */
	public boolean setAllowedRecipes( ArrayList<EasyRecipe> recipeList) {
		this.allowedRecipes.clear();
		for (int i = 0; i < recipeList.size(); i++) {
			this.addAllowedRecipe(recipeList.get(i));
		}
		return true;
	}

	/**
	 * Determines if recipes are craftable based on the availability of ingredients in the player's inventory
	 *
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @return 						A list of all craftable easy recipes (based on ingredient availability)
	 */
	public ArrayList<EasyRecipe> getCraftableRecipes(InventoryPlayer player_inventory) {
		long beforeTime = System.nanoTime();

		ArrayList<EasyRecipe> r = new ArrayList<EasyRecipe>();
		ArrayList<EasyRecipe> all = this.allowedRecipes;
		for (int i = 0; i < all.size(); i++) {
			if (hasIngredients(all.get(i).ingredients, player_inventory, 0)) {
				r.add(all.get(i));
			}
		}
		if(Version.DEBUG) {
			System.out.println(String.format("Returning %d craftable out of %d available recipes! ---- Total time: %.8f", r.size(), this.allowedRecipes.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
		}
		return r;
	}

	/**
	 * Determines if the ingredients to craft the recipe, 1x times, is available.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as an object list
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						True, if the player has the ingredients (or can intermediately craft them based on recursioncount) to make this recipe. False, if not.
	 */
	public boolean hasIngredients(Object[] ingredients, InventoryPlayer player_inventory, int recursionCount) {
		return hasIngredientsMaxStack(ingredients, player_inventory, 1, recursionCount) == 0 ? false : true;
	}

	/**
	 * Removes the ingredients from the player's inventory to craft the recipe, 1x times.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as an object list
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						True, if the ingredients to make this recipe was removed. False, if not.
	 */
	public boolean takeIngredients(Object[] ingredients, InventoryPlayer player_inventory, int recursionCount) {
		return takeIngredientsMaxStack(ingredients, player_inventory, 1, recursionCount) == 0 ? false : true;
	}

	/**
	 * Determines if the ingredients to craft the recipe, maxTimes times, is available.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as an object list
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  maxTimes				Check if the recipe can be crafted as much as possible, up to this amount.
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						The maximum number of times the recipe can be crafted (up to maxTimes).
	 */
	public int hasIngredientsMaxStack(Object[] ingredients, InventoryPlayer player_inventory, int maxTimes, int recursionCount) {
		return checkIngredients(ingredients, player_inventory, false, maxTimes, recursionCount);
	}

	/**
	 * Removes the ingredients from the player's inventory to craft the recipe, as many times as possible, up to maxTimes times.
	 *
	 * @param  ingredients			The ingredients of the easycraft recipe as an object list
	 * @param  player_inventory		The inventory of the player to check for recipe ingredients
	 * @param  maxTimes				Craft the recipe as much as possible, up to this amount.
	 * @param  recursionCount		How many times the checker can run to find ingredients (including checking intermediate crafting steps)
	 * @return 						The number of times the recipe was 'crafted' and the ingredients were removed (up to maxTimes).
	 */
	public int takeIngredientsMaxStack(Object[] ingredients, InventoryPlayer player_inventory, int maxTimes, int recursionCount) {
		return checkIngredients(ingredients, player_inventory, true, maxTimes, recursionCount);
	}

	/**
	 * Finds the first slots within an inventory that contains the needed amount of the requested itemstack.
	 *
	 * Note, this function checks if at least ingredient.size of that itemtype is present in the inventory.
	 * As such, it can return multiple slots, if the needed ingredients are scattered across multiple slots.
	 *
	 * @param  ingredient			The itemstack to check for
	 * @param  player_inventory		The player inventory to look in
	 * @return 						The slot IDs the itemstacks were found in, if found. Null if not found.
	 */
	private int[] findSlotsWithIngredient(Object ingredient, InventoryPlayer player_inventory) {
		//int amountLeftToFind = ingredient.stackSize;
		//TODO: Ingot to CompactBlocks recipes request 9xIngot per each slot of the recipe. (Check up why)
		//		Note: This is how the recipe is received from the forge craftingmanager. Might just be how they are setup.
		int amountLeftToFind = 1;
		List<Integer> foundSlots = new ArrayList<Integer>();
		ArrayList<ItemStack> toFindIngredient = null;
		//For ore dictionary, we must allow a single ingredient to be a list (for oredict compatibility)
		//Meaning, it is still just one ingredient in the recipe, but any of the items in the list will suffice
		if (ingredient instanceof ArrayList) {
			toFindIngredient = (ArrayList) ingredient;
		} else {
			// Assume it is of instance ItemStack
			toFindIngredient = new ArrayList<ItemStack>();
			toFindIngredient.add( (ItemStack) ingredient );
		}
	
		if (ingredient != null) {
			for (int invPos = 0; invPos < player_inventory.mainInventory.length; invPos++) {
				if (player_inventory.mainInventory[invPos] != null) {
					//If we find a slot that has the correct itemtype (matching ID and metaID), then we check how much is within the slot, and reduce the amountLeftToFind by that amount.
					for (int ingCount = 0; ingCount < toFindIngredient.size(); ingCount++) {
						ItemStack currentIngredient = toFindIngredient.get(ingCount);
						if (player_inventory.mainInventory[invPos].itemID == currentIngredient.itemID && (player_inventory.mainInventory[invPos].getItemDamage() == currentIngredient.getItemDamage() || currentIngredient.getItemDamage() == -1)) {
							ItemStack stack = player_inventory.getStackInSlot(invPos);
							foundSlots.add(invPos);
							amountLeftToFind -= stack.stackSize;
							//If we already found enough, then stop looking through the inventory.
							if (amountLeftToFind <= 0) {
								invPos = player_inventory.mainInventory.length;
								ingCount = toFindIngredient.size();
								break;
							}
						}
						//TODO: Add in handling for oredictionary.
					}
				}
			}
		}
		
		if (amountLeftToFind > 0) {
			//Didn't find enough. Return a null.
			return null;
		} else {
			//Found enough, return the array of slot positions.
			//Yuck, ugly conversion from ArrayList<Integer> to int[].
			int[] ret = new int[foundSlots.size()];
			for(int i = 0; i < ret.length; i++) {
				ret[i] = foundSlots.get(i);
			}
			return ret;
		}
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
	private int checkIngredients(Object[] ingredients, InventoryPlayer player_inventory, boolean take_ingredients, int maxTimes, int recursionCount) {
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
					// if(Version.DEBUG) {
						// System.out.println(i + " of " + ingredients.length + " : " + ingredients[i]);
					// }
					int[] slotsFound = findSlotsWithIngredient(ingredients[i],tmp);
					// if(Version.DEBUG) {
						// System.out.println("slotsFound: " + slotsFound);
					// }
					if((slotsFound != null) && (slotsFound.length > 0))
					{
						// if(Version.DEBUG) {
							// System.out.println("Slots: " + slotsFound[0] + ";" + slotsFound.length);
						// }
						ItemStack stack = tmp.getStackInSlot(slotsFound[0]);
						//Found it, so strip it out and carry on with the next ingredient.
						tmp.decrStackSize(slotsFound[0], 1);
						continue ingLoop;
						//TODO: Change to handle stackSize properly.
					}
					/**
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
					*/
					//If we get here, we didn't find the ingredient in the inventory. So we check if we can do an intermediary recipe to provide us
					//with the ingredients we need.
					if ((recursionCount + 1) < ModEasyCrafting.instance.allowMultiStepRecipes) {
						//Find a valid recipe that outputs the ingredients we need.
						ArrayList<ItemStack> toFindIngredient = null;
						if (ingredients[i] instanceof ArrayList) {
							toFindIngredient = (ArrayList) ingredients[i];
						} else {
							// Assume it is of instance ItemStack
							toFindIngredient = new ArrayList<ItemStack>();
							toFindIngredient.add( (ItemStack) ingredients[i] );
						}
						for (int ingCount = 0; ingCount < toFindIngredient.size(); ingCount++) {
							ArrayList<EasyRecipe> rList = getValidRecipe((ItemStack) toFindIngredient.get(ingCount));
							if (!rList.isEmpty()) {
								for (int l = 0; l < rList.size(); l++) {
									EasyRecipe ingRecipe = rList.get(l);
									InventoryPlayer tmp3 = new InventoryPlayer(null);
									tmp3.copyInventory(tmp);
									//The "addItemStackToInventory" function needs a valid player, else the creativemode check does a null exception
									tmp3.player = player_inventory.player;
									if (takeIngredients(ingRecipe.ingredients, tmp3, recursionCount + 1)) {
										//If the ingredients are present, check that there is space to stick the recipe result in the resulting inventory
										//Note: Given the way that addItemStackToInventory works, there exists a bug in creaive mode when you have a full inventory.
										//      Hence the fancy footwork checking creative mode and empty slots.
										if ((tmp3.addItemStackToInventory(ingRecipe.result.copy()) && !tmp3.player.capabilities.isCreativeMode) || (tmp3.getFirstEmptyStack() > 0 && tmp3.player.capabilities.isCreativeMode)) {
											// Update the working inventory and try to take the same ingredient again.
											tmp.copyInventory(tmp3);
											i--;
											continue ingLoop;
										} else {
											if(Version.DEBUG) {
												System.out.println("!!!Insufficient inventory space: " + ingredients[i] + " ; " + ingRecipe.ingredients);
											}
										}
									}
								}
							}
						}
					}
					//If we get to this point, we didn't have enough ingredients, and couldn't craft more with what the player had. So we stop trying to make more.
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
	public ArrayList<EasyRecipe> getValidRecipe(Object result) {
		ArrayList<EasyRecipe> list = new ArrayList<EasyRecipe>();
		ArrayList<EasyRecipe> all = this.allowedRecipes;
		ItemStack toFindItem = (ItemStack) result;
		for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.result.itemID == toFindItem.itemID && (r.result.getItemDamage() == toFindItem.getItemDamage() || toFindItem.getItemDamage() == -1)) {
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
	public EasyRecipe getValidRecipe(ItemStack result, Object[] ingredients) {
		ArrayList<EasyRecipe> all = this.allowedRecipes;
		allLoop: for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.result.itemID == result.itemID && r.result.getItemDamage() == result.getItemDamage()) {
				int j = 0;
				int count = 0;
				while (j < r.ingredients.length) {
					ItemStack currentStack = (ItemStack) r.ingredients[j];
					ItemStack compareStack = (ItemStack) ingredients[count];
					if (currentStack == null) {
						j++;
						continue;
					}

					if (currentStack.itemID != compareStack.itemID || currentStack.getItemDamage() != compareStack.getItemDamage()) {
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
	public int calculateCraftingMultiplierUntilMaxStack(ItemStack recipe_result, ItemStack inHand) {
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
	public EasyRecipe getValidRecipe(GuiEasyCrafting gui, int slot_index, ItemStack inHand, ItemStack is) {
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
