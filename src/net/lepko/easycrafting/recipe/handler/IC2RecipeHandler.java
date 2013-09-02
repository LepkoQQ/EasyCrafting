package net.lepko.easycrafting.recipe.handler;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;

public class IC2RecipeHandler implements IRecipeHandler {

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        List<Object> ingredients = null;
        String className = recipe.getClass().getName();
        if (className.equals("ic2.core.AdvRecipe") || className.equals("ic2.core.AdvShapelessRecipe")) {
            try {
                Object[] input = (Object[]) Class.forName(className).getField("input").get(recipe);
                ingredients = new ArrayList<Object>(Arrays.asList(input));
            } catch (Exception e) {
                EasyLog.warning("[IC2 Recipe Scan] " + className + " failed!", e);
            }
        }
        return ingredients;
    }

    @Override
    public boolean matchItem(ItemStack target, ItemStack candidate, WrappedRecipe recipe) {
        if (candidate == null || target == null) {
            return candidate == target;
        }
        if (target.itemID != candidate.itemID) {
            return false;
        }
        if (candidate.getItem() instanceof IElectricItem) {
            if (recipe != null && recipe.output.stack.getItem() instanceof IElectricItem) {
                int charge = ElectricItem.manager.getCharge(candidate);
                ElectricItem.manager.charge(recipe.output.stack, charge, Integer.MAX_VALUE, true, false);
            }
            return true;
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
