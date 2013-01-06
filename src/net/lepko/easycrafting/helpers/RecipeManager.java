package net.lepko.easycrafting.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.handlers.ModCompatibilityHandler;
import net.lepko.easycrafting.helpers.RecipeHelper.RecipeComparator;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

public class RecipeManager {

    private static int lastRecipeListSize = 0;
    public static List<EasyRecipe> scannedRecipes = new ArrayList<EasyRecipe>();

    public static void checkForNewRecipes() {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();

        if (lastRecipeListSize < recipes.size()) {
            List<IRecipe> newRecipes = recipes.subList(lastRecipeListSize, recipes.size());
            lastRecipeListSize = recipes.size();

            scanRecipes(newRecipes);
        }
    }

    public static void scanRecipes(List<IRecipe> recipes) {
        long beforeTime = System.nanoTime();
        int size = recipes.size();
        //

        ModCompatibilityHandler.scanRecipes(recipes);

        ArrayList<EasyRecipe> tmp = new ArrayList<EasyRecipe>();

        for (IRecipe r : recipes) {
            ArrayList ingredients = RecipeHelper.getIngredientList(r);
            if (ingredients != null) {
                tmp.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
            } else {
                EasyLog.log("Unknown Recipe: " + r.getClass().getName());
            }
        }

        scannedRecipes.addAll(tmp);
        Collections.sort(scannedRecipes, new RecipeComparator());

        //
        EasyLog.log(String.format("Scanned %d new recipes in %.8f seconds", size, ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
    }
}
