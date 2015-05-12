package net.lepko.easycrafting.core.recipe.handler;

import java.util.List;

import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public interface IRecipeHandler {

    public List<Object> getInputs(IRecipe recipe);

    public boolean matchItem(ItemStack target, ItemStack candidate, WrappedRecipe recipe);

    public ItemStack getCraftingResult(WrappedRecipe recipe, List<ItemStack> usedIngredients);
    
}
