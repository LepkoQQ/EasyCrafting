package net.lepko.easycrafting.recipe.handler;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lepko.easycrafting.core.EasyLog;
import net.lepko.easycrafting.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Loader;

@SuppressWarnings("unchecked")
public class IC2RecipeHandler implements IRecipeHandler {

    private static Class<? super IRecipe> shapedRecipeClass = null;
    private static Class<? super IRecipe> shapelessRecipeClass = null;
    private static Method resolveOreDict = null;
    static {
        if (Loader.isModLoaded("IC2")) {
            try {
                shapedRecipeClass = (Class<? super IRecipe>) Class.forName("ic2.core.AdvRecipe");
                shapelessRecipeClass = (Class<? super IRecipe>) Class.forName("ic2.core.AdvShapelessRecipe");
                resolveOreDict = shapedRecipeClass.getMethod("resolveOreDict", Object.class);
            } catch (Exception e) {
                EasyLog.warning("[IC2 Recipe Scan] Adv(Shapeless)Recipe.class could not be obtained!", e);
            }
        } else {
            EasyLog.log("[IC2 Recipe Scan] Disabled.");
        }
    }

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        List<Object> ingredients = null;
        Object[] input = null;
        try {
            if (shapedRecipeClass != null && shapedRecipeClass.isInstance(recipe)) {
                input = (Object[]) shapedRecipeClass.getField("input").get(recipe);
            } else if (shapelessRecipeClass != null && shapelessRecipeClass.isInstance(recipe)) {
                input = (Object[]) shapelessRecipeClass.getField("input").get(recipe);
            }

            if (input != null) {
                ingredients = new ArrayList<Object>(Arrays.asList(input));
                for (int i = 0; i < ingredients.size(); i++) {
                    Object o = ingredients.get(i);
                    if (o instanceof String) {
                        if (resolveOreDict == null) {
                            return null;
                        }
                        List<ItemStack> resolved = (List<ItemStack>) resolveOreDict.invoke(null, o);
                        if (resolved == null || resolved.isEmpty()) {
                            EasyLog.warning("[IC2 Recipe Scan] could not resolve one of the recipe inputs [string=" + (String) o + "]");
                            return null;
                        }
                        ingredients.set(i, resolved);
                    }
                }
            }
        } catch (Exception e) {
            EasyLog.warning("[IC2 Recipe Scan] " + recipe.getClass().getName() + " failed!", e);
            return null;
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
        if (candidate.getItem() instanceof IElectricItem) {
            return true;
        }
        if (target.getItemDamage() != OreDictionary.WILDCARD_VALUE && target.getItemDamage() != candidate.getItemDamage()) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(WrappedRecipe recipe, List<ItemStack> usedIngredients) {
        if (recipe.inputs.size() == usedIngredients.size()) {
            if (recipe.output.stack.getItem() instanceof IElectricItem) {
                ItemStack crafted = recipe.output.stack.copy();
                int charge = 0;
                for (ItemStack is : usedIngredients) {
                    if (is.getItem() instanceof IElectricItem) {
                        charge += ElectricItem.manager.getCharge(is);
                    }
                }
                if (charge > 0) {
                    ElectricItem.manager.charge(crafted, charge, Integer.MAX_VALUE, true, false);
                }
                return crafted;
            }
        }
        return recipe.output.stack.copy();
    }
}
