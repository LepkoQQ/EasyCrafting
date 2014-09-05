package net.lepko.easycrafting.core.recipe.handler;

import cpw.mods.fml.common.Loader;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class MekanismRecipeHandler implements IRecipeHandler {

    private static Class<? super IRecipe> recipeClass = null;
    private static Method checkItemEquals = null;
    private static Field inputField = null;

    static {
        if (Loader.isModLoaded("Mekanism")) {
            try {
                recipeClass = (Class<? super IRecipe>) Class.forName("mekanism.common.recipe.MekanismRecipe");
                checkItemEquals = recipeClass.getDeclaredMethod("checkItemEquals", ItemStack.class, ItemStack.class);
                checkItemEquals.setAccessible(true);
                inputField = recipeClass.getDeclaredField("input");
                inputField.setAccessible(true);
            } catch (Exception e) {
                Ref.LOGGER.warn("[Mekanism Recipe Scan] MekanismRecipe.class could not be obtained!", e);
            }
        } else {
            Ref.LOGGER.info("[Mekanism Recipe Scan] Disabled.");
        }
    }

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        List<Object> ingredients = null;
        if (recipeClass != null && recipeClass.isInstance(recipe) && inputField != null && checkItemEquals != null) {
            try {
                Object[] input = (Object[]) inputField.get(recipe);
                ingredients = new ArrayList<Object>(Arrays.asList(input));
            } catch (Exception e) {
                Ref.LOGGER.warn("[Mekanism Recipe Scan] " + recipe.getClass().getName() + " failed!", e);
                return null;
            }
        }
        return ingredients;
    }

    @Override
    public boolean matchItem(ItemStack target, ItemStack candidate, WrappedRecipe recipe) {
        boolean b;
        try {
            b = (Boolean) checkItemEquals.invoke(recipe.recipe, target, candidate);
        } catch (Exception e) {
            Ref.LOGGER.warn("[Mekanism Recipe Handler] failed to match item!", e);
            return false;
        }
        return b;
    }

    @Override
    public ItemStack getCraftingResult(WrappedRecipe recipe, List<ItemStack> usedIngredients) {
        return recipe.recipe.getCraftingResult(RecipeManager.getCraftingInventory(usedIngredients));
    }
}
