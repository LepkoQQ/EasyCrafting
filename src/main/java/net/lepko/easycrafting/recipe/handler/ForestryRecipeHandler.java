package net.lepko.easycrafting.recipe.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class ForestryRecipeHandler implements IRecipeHandler {

    private static Class<? super IRecipe> shapedRecipeClass = null;
    static {
        try {
            shapedRecipeClass = (Class<? super IRecipe>) Class.forName("forestry.core.utils.ShapedRecipeCustom");
        } catch (Exception e) {
            EasyLog.warning("[Forestry Recipe Scan] Forestry ShapedRecipeCustom.class could not be obtained!", e);
        }
    }

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        List<Object> ingredients = null;
        if (shapedRecipeClass != null && shapedRecipeClass.isInstance(recipe)) {
            try {
                Object[] input = (Object[]) shapedRecipeClass.getMethod("getIngredients").invoke(recipe);
                ingredients = new ArrayList<Object>(Arrays.asList(input));
            } catch (Exception e) {
                EasyLog.warning("[Forestry Recipe Scan] " + recipe.getClass().getName() + " failed!", e);
                return null;
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
        if (target.getItemDamage() != OreDictionary.WILDCARD_VALUE && target.getItemDamage() != candidate.getItemDamage()) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(WrappedRecipe recipe, List<ItemStack> usedIngredients) {
        if (recipe.inputs.size() == usedIngredients.size()) {
            if (ReflectionHelper.getPrivateValue(shapedRecipeClass, recipe.recipe, "preserveNBT")) {
                for (ItemStack is : usedIngredients) {
                    if (is.hasTagCompound()) {
                        ItemStack crafted = recipe.output.stack.copy();
                        crafted.setTagCompound((NBTTagCompound) is.getTagCompound().copy());
                        return crafted;
                    }
                }
            }
        }
        return recipe.output.stack.copy();
    }
}
