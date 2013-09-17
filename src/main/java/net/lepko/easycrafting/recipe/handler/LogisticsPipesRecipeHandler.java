package net.lepko.easycrafting.recipe.handler;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.recipe.WrappedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LogisticsPipesRecipeHandler implements IRecipeHandler {

    private static Class<? super IRecipe> recipeClass = null;
    private static Item moduleItem = null;
    private static int[] registeredModuleIDs = null;
    static {
        try {
            recipeClass = (Class<? super IRecipe>) Class.forName("logisticspipes.recipes.ShapelessResetRecipe");
            Class logisticsPipes = Class.forName("logisticspipes.LogisticsPipes");
            Field moduleItemField = logisticsPipes.getField("ModuleItem");
            moduleItem = (Item)moduleItemField.get(null);

            Method getRegisteredModulesMethod = moduleItem.getClass().getDeclaredMethod("getRegisteredModulesIDs");
            registeredModuleIDs = (int[])getRegisteredModulesMethod.invoke(moduleItem);
        } catch (Exception e) {
            EasyLog.warning("[Logistics Pipes Recipe Scan] LP ShapelessResetRecipe.class could not be obtained!", e);
            e.printStackTrace();
        }
    }

    private boolean isModule(int dmgValue) {
        for (int i = 0; i < registeredModuleIDs.length; i++) {
            int registeredModuleID = registeredModuleIDs[i];
            if( registeredModuleID==dmgValue )
                return true;
        }
        return false;
    }

    @Override
    public List<Object> getInputs(IRecipe recipe) {
        if( recipeClass==null || moduleItem==null ||
                registeredModuleIDs==null || !recipeClass.isInstance(recipe) )
            return null;

        if( recipe==null || recipe.getRecipeOutput()==null )
            return null;
        int dmg = recipe.getRecipeOutput().getItemDamage();
        if( ! isModule(dmg) )
            return null;

        List<Object> list = new ArrayList<Object>();
        list.add( new ItemStack(moduleItem, 1, dmg) );
        return list;
    }

    @Override
    public boolean matchItem(ItemStack target, ItemStack candidate, WrappedRecipe recipe) {
        if( candidate==null || target==null )
            return false;

        if( candidate.itemID==target.itemID && candidate.getItemDamage()==target.getItemDamage() )
            return true;

        return false;
    }

    @Override
    public ItemStack getCraftingResult(WrappedRecipe recipe, List<ItemStack> usedIngredients) {
        return recipe.output.stack.copy();
    }
}
