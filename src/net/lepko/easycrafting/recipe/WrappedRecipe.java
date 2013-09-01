package net.lepko.easycrafting.recipe;

import java.util.Collections;
import java.util.List;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.recipe.handler.IRecipeHandler;
import net.lepko.util.StackUtils;
import net.lepko.util.WrappedStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class WrappedRecipe {

    public final IRecipe recipe;
    public final List<Object> inputs;
    public final List<WrappedStack> collatedInputs;
    public final WrappedStack output;
    public final IRecipeHandler handler;

    private WrappedRecipe(IRecipe recipe, List<Object> inputs, WrappedStack output, IRecipeHandler handler) {
        this.recipe = recipe;
        this.inputs = inputs;
        this.collatedInputs = StackUtils.collateStacks(inputs);
        this.output = output;
        this.handler = handler;
    }

    /**
     * If recipe is valid, adds the recipe to the list and returns the WrappedRecipe instance.
     */
    public static WrappedRecipe of(IRecipe recipe) {
        if (recipe == null) {
            warn("recipe is null");
            return null;
        }
        if (recipe.getRecipeOutput() == null) {
            warn("recipe output is null", recipe);
            return null;
        }
        if (recipe.getRecipeOutput().getItem() == null) {
            warn("recipe output item is null [id=" + recipe.getRecipeOutput().itemID + "]", recipe);
            return null;
        }
        List<Object> inputs = null;
        IRecipeHandler handler = null;
        for (IRecipeHandler h : RecipeManager.HANDLERS) {
            inputs = h.getInputs(recipe);
            if (inputs != null) {
                handler = h;
                break;
            }
        }

        if (inputs == null) {
            warn("failed to get recipe input list", recipe);
            return null;
        }
        inputs.removeAll(Collections.singleton(null));
        if (inputs.isEmpty()) {
            warn("recipe input list is empty", recipe);
            return null;
        }
        for (Object o : inputs) {
            if (o instanceof List) {
                List<?> list = (List<?>) o;
                if (list.isEmpty()) {
                    warn("one of recipe inputs is an empty list", recipe);
                    return null;
                } else {
                    for (Object p : list) {
                        if (p instanceof ItemStack) {
                            ItemStack stack = (ItemStack) p;
                            if (stack.getItem() == null) {
                                warn("one of recipe input items is null [id=" + stack.itemID + "]", recipe);
                                return null;
                            }
                            stack.stackSize = 1;
                        } else {
                            warn("one of recipe inputs is unknown", o, recipe);
                            return null;
                        }
                    }
                }
            } else {
                if (o instanceof ItemStack) {
                    ItemStack stack = (ItemStack) o;
                    if (stack.getItem() == null) {
                        warn("one of recipe input items is null [id=" + stack.itemID + "]", recipe);
                        return null;
                    }
                    stack.stackSize = 1;
                } else {
                    warn("one of recipe inputs is unknown", o, recipe);
                    return null;
                }
            }
        }
        WrappedStack output = new WrappedStack(recipe.getRecipeOutput());
        WrappedRecipe wr = new WrappedRecipe(recipe, inputs, output, handler);
        return wr;
    }

    private static void warn(String msg, Object... objs) {
        String s = "[WrappedRecipe] " + msg;
        for (Object o : objs) {
            s += " (" + o.getClass().getCanonicalName() + ")";
        }
        EasyLog.warning(s);
    }
}
