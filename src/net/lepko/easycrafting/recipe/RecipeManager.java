package net.lepko.easycrafting.recipe;

import java.util.LinkedList;
import java.util.List;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.recipe.handler.ForgeRecipeHandler;
import net.lepko.easycrafting.recipe.handler.IRecipeHandler;
import net.lepko.easycrafting.recipe.handler.VanillaRecipeHandler;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import com.google.common.collect.ImmutableList;

public class RecipeManager {

    public static final List<IRecipeHandler> HANDLERS = new LinkedList<IRecipeHandler>();
    static {
        // Mod recipe classes could extend vanilla classes so scan them first
        // HANDLERS.add(new IC2RecipeHandler());
        // HANDLERS.add(new EE3RecipeHandler());
        // HANDLERS.add(new ForestryRecipeHandler());

        // At the end scan vanilla and forge
        HANDLERS.add(new VanillaRecipeHandler());
        HANDLERS.add(new ForgeRecipeHandler());
    }

    private static final List<WrappedRecipe> allRecipes = new LinkedList<WrappedRecipe>();
    private static int prevListSize = 0;
    private static IRecipe prevLastElement = null;

    private static boolean shouldScan(List<IRecipe> recipes) {
        if (allRecipes.isEmpty()) {
            return true;
        }
        if (prevListSize != recipes.size() || (!recipes.isEmpty() && prevLastElement != recipes.get(recipes.size() - 1))) {
            return true;
        }
        return false;
    }

    public static void scanRecipes() {
        @SuppressWarnings("unchecked")
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        if (!shouldScan(recipes)) {
            return;
        }
        prevListSize = recipes.size();
        prevLastElement = recipes.get(recipes.size() - 1);

        allRecipes.clear();

        long startTime = System.nanoTime();
        int fails = 0;

        for (IRecipe r : recipes) {
            WrappedRecipe wr = WrappedRecipe.of(r);
            if (wr != null) {
                allRecipes.add(wr);
            } else {
                fails++;
            }
        }
        // TODO: Sort the list?

        EasyLog.log(String.format("Scanned %d recipes (%d failed) in %.8f seconds", recipes.size(), fails, (System.nanoTime() - startTime) / 1000000000.0D));
    }

    public static List<WrappedRecipe> getAllRecipes() {
        return ImmutableList.copyOf(allRecipes);
    }
}
