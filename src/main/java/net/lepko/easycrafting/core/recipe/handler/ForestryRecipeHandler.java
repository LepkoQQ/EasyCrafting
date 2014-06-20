package net.lepko.easycrafting.core.recipe.handler;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class ForestryRecipeHandler implements IRecipeHandler {

    private static Class<? super IRecipe> shapedRecipeClass = null;

    static {
        if (Loader.isModLoaded("Forestry")) {
            try {
                shapedRecipeClass = (Class<? super IRecipe>) Class.forName("forestry.core.utils.ShapedRecipeCustom");
            } catch (Exception e) {
                Ref.LOGGER.warn("[Forestry Recipe Scan] Forestry ShapedRecipeCustom.class could not be obtained!", e);
            }
        } else {
            Ref.LOGGER.info("[Forestry Recipe Scan] Disabled.");
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
                Ref.LOGGER.warn("[Forestry Recipe Scan] " + recipe.getClass().getName() + " failed!", e);
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
        if (recipe.inputs.size() == usedIngredients.size()) {
            if (ReflectionHelper.getPrivateValue(shapedRecipeClass, recipe.recipe, "preserveNBT")) {
                for (ItemStack is : usedIngredients) {
                    if (is.hasTagCompound()) {
                        ItemStack crafted = recipe.getOutput();
                        crafted.setTagCompound((NBTTagCompound) is.getTagCompound().copy());
                        return crafted;
                    }
                }
            }
        }
        return recipe.getOutput();
    }
}
