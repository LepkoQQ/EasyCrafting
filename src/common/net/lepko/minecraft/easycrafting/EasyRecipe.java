package net.lepko.minecraft.easycrafting;

import net.minecraft.src.ItemStack;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class defines a single EasyRecipe.
 */
public class EasyRecipe {

	/** What items (and how many of them) does this recipe use when crafted? */
	public ItemStack[] ingredients;
	/** What item (and how many of it) does this recipe make when crafted? */
	public ItemStack result;

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
			if (!ItemStack.areItemStacksEqual(this.ingredients[i], tmp.ingredients[i])) {
				return false;
			}
		}
		return true;
	}
}
