package net.lepko.easycrafting.core.recipe.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;

public class VanillaRecipeHandler implements IRecipeHandler {

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        List<Object> ingredients = null;
        if (recipe instanceof ShapedRecipes) {
            ingredients = new ArrayList<Object>(Arrays.asList(((ShapedRecipes) recipe).recipeItems));
        } else if (recipe instanceof ShapelessRecipes) {
            ingredients = new ArrayList<Object>((List<?>) ((ShapelessRecipes) recipe).recipeItems);
        }
        return ingredients;
    }

    @Override
    public boolean matchItem(ItemStack target, ItemStack candidate, WrappedRecipe recipe) {
        if (candidate == null || target == null) {
            return candidate == target;
        }
        if (target.getItem() != candidate.getItem()) {
            return false;
        }
        if (target.getItemDamage() != OreDictionary.WILDCARD_VALUE && target.getItemDamage() != candidate.getItemDamage()) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(WrappedRecipe recipe, List<ItemStack> usedIngredients) {
        return recipe.output.stack.copy();
    }
}
