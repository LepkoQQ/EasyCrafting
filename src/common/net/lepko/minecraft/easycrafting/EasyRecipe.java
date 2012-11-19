package net.lepko.minecraft.easycrafting;

import java.util.ArrayList;

import net.minecraft.src.ItemStack;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class defines a single EasyRecipe.
 */
public class EasyRecipe {

	/** What items (and how many of them) does this recipe use when crafted? */
	public Object[] ingredients;
	//public ItemStack[] ingredients;
	/** What item (and how many of it) does this recipe make when crafted? */
	public ItemStack result;
	/** Does this recipe use the forge oredictionary? */
	public boolean usesOreDict;
	/** What categories does this recipe belong to? */
	public ArrayList<Integer> categories;

	/**
	 * Creates an instance of the class.
	 *
	 * @param  recipeOutput		What itemstack this recipe should create if crafted.
	 * @param  ingredients		What itemstacks this recipe requires as ingredients.
	 * @return N/A
	 */
	public EasyRecipe(ItemStack recipeOutput, ItemStack[] ingredients) {
		this.result = recipeOutput;
		this.ingredients = ingredients;
		this.usesOreDict = false;
	}
	
	/**
	 * Creates an instance of the class.
	 *
	 * @param  recipeOutput		What itemstack this recipe should create if crafted.
	 * @param  ingredients		What itemstacks this recipe requires as ingredients.
	 * @param  oreDictNames		The strings to use as keys for the ore dictionary lookups.
	 * @return N/A
	 */
	public EasyRecipe(ItemStack recipeOutput, ItemStack[] ingredients, boolean pUsesOreDict) {
		this.result = recipeOutput;
		this.ingredients = ingredients;
		this.usesOreDict = pUsesOreDict;
	}

	/**
	 * Compares this recipe with the given object, checking if it is an equal easyrecipe.
	 *
	 * @param  obj	The object to compare this recipe with.
	 * @return 		True, if the object also an easyrecipe that is equal in all regards; False, if not.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EasyRecipe)) {
			return false;
		}
		EasyRecipe tmp = (EasyRecipe) obj;
		if (this.ingredients.length != tmp.ingredients.length) {
			return false;
		}
		if (!ItemStack.areItemStacksEqual(this.result, tmp.result)) {
			return false;
		}
		for (int i = 0; i < this.ingredients.length; i++) {
			ItemStack thisIngredient = (ItemStack) this.ingredients[i];
			ItemStack otherIngredient = (ItemStack) tmp.ingredients[i];
			if (!ItemStack.areItemStacksEqual(thisIngredient, otherIngredient)) {
				return false;
			}
		}
		if(this.categories.size() != tmp.categories.size()) {
			return false;
		}
		for (int i = 0; i < this.categories.size(); i++) {
			if (this.categories.get(i) != tmp.categories.get(i)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks that all of this recipe's categories are within the provided list.
	 *
	 * @param	categoriesAllowed	The list to check that this recipe's categories fall within.
	 * @return						True if all of this recipe's categories are within the given list. False if not.
	 */
	public boolean allowedBy(ArrayList<Integer> categoriesAllowed) {
		for (int i = 0; i < this.categories.size(); i++) {
			boolean found = false;
			
			for (int j = 0; j < categoriesAllowed.size(); j++) {
				if (this.categories.get(i) == categoriesAllowed.get(j)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Checks if any of this recipe's categories are within the provided list.
	 *
	 * @param	categoriesDenied	The list to check that any this recipe's categories fall within.
	 * @return						True if any of this recipe's categories are within the given list. False if not.
	 */
	public boolean deniedBy(ArrayList<Integer> categoriesDenied) {
		for (int i = 0; i < this.categories.size(); i++) {
			for (int j = 0; j < categoriesDenied.size(); j++) {
				if (this.categories.get(i) == categoriesDenied.get(j)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if any of this recipe's categories are within the provided list.
	 *
	 * @param	categoriesDenied	The list to check that any this recipe's categories fall within.
	 * @return						True if any of this recipe's categories are within the given list. False if not.
	 */
	public String tooltipString() {
		String tooltip = this.result.getItemName();
		
		for (int i = 0; i < this.ingredients.length; i++) {
			ItemStack thisIngredient = (ItemStack) this.ingredients[i];
			if (thisIngredient != null) {
				tooltip += "\n" + thisIngredient.stackSize + "x" + thisIngredient.getItemName();
			}
		}
		return tooltip;
	}
}
