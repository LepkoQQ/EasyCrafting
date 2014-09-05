package net.lepko.easycrafting.core.recipe.handler;

import cpw.mods.fml.common.Loader;
import ic2.api.item.ElectricItem;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.lepko.easycrafting.core.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class IC2RecipeHandler implements IRecipeHandler {

    private static Class<? super IRecipe> shapedRecipeClass = null;
    private static Class<? super IRecipe> shapelessRecipeClass = null;
    private static Method expandArray = null;

    static {
        if (Loader.isModLoaded("IC2")) {
            try {
                shapedRecipeClass = (Class<? super IRecipe>) Class.forName("ic2.core.AdvRecipe");
                shapelessRecipeClass = (Class<? super IRecipe>) Class.forName("ic2.core.AdvShapelessRecipe");
                expandArray = shapedRecipeClass.getMethod("expandArray", Object[].class);
            } catch (Exception e) {
                Ref.LOGGER.warn("[IC2 Recipe Scan] Adv(Shapeless)Recipe.class could not be obtained!", e);
            }
        } else {
            Ref.LOGGER.info("[IC2 Recipe Scan] Disabled.");
        }
    }

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        List<Object> ingredients = null;
        Object[] input = null;
        if (shapedRecipeClass != null && shapelessRecipeClass != null && expandArray != null) {
            try {
                if (shapedRecipeClass.isInstance(recipe)) {
                    input = (Object[]) shapedRecipeClass.getField("input").get(recipe);
                } else if (shapelessRecipeClass.isInstance(recipe)) {
                    input = (Object[]) shapelessRecipeClass.getField("input").get(recipe);
                }

                if (input != null) {
                    List<ItemStack>[] expandedInputs = (List<ItemStack>[]) expandArray.invoke(null, new Object[] { input });
                    ingredients = new ArrayList<Object>(Arrays.asList(expandedInputs));
                }
            } catch (Exception e) {
                Ref.LOGGER.warn("[IC2 Recipe Scan] " + recipe.getClass().getName() + " failed!", e);
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
        ItemStack crafted = recipe.getOutput();
        double charge = 0.0;
        for (ItemStack is : usedIngredients) {
            charge += ElectricItem.manager.getCharge(is);
        }
        ElectricItem.manager.charge(crafted, charge, Integer.MAX_VALUE, true, false);
        if ((crafted.getItem() instanceof IFluidContainerItem)) {
            StackUtils.getOrCreateNBT(crafted);
        }
        return crafted;
    }
}
