package net.lepko.easycrafting.core.recipe.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class ForgeRecipeHandler implements IRecipeHandler {

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        List<Object> ingredients = null;
        if (recipe instanceof ShapedOreRecipe) {
            Object[] input = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe) recipe, "input");
            ingredients = new ArrayList<Object>(Arrays.asList(input));
        } else if (recipe instanceof ShapelessOreRecipe) {
            ArrayList<Object> input = ReflectionHelper.getPrivateValue(ShapelessOreRecipe.class, (ShapelessOreRecipe) recipe, "input");
            ingredients = new ArrayList<Object>(input);
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
        return recipe.getOutput();
    }
}
