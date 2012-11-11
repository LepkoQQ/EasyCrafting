package net.lepko.minecraft.easycrafting;

import net.minecraft.src.ItemStack;

public class EasyRecipe {

	public ItemStack[] ingredients;
	public ItemStack result;

	public EasyRecipe(ItemStack recipeOutput, ItemStack[] ingredients) {
		this.result = recipeOutput;
		this.ingredients = ingredients;
	}

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
