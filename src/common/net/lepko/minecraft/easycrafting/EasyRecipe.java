package net.lepko.minecraft.easycrafting;

import net.minecraft.src.ItemStack;

public class EasyRecipe {

	public ItemStack[] ingredients;
	public ItemStack result;

	public EasyRecipe(ItemStack recipeOutput, ItemStack[] ingredients) {
		this.result = recipeOutput;
		this.ingredients = ingredients;
	}
}
