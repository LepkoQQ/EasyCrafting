package net.lepko.easycrafting.core.recipe;

import com.google.common.collect.ImmutableList;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.recipe.handler.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RecipeManager {

    public static final List<IRecipeHandler> HANDLERS = new LinkedList<IRecipeHandler>();

    static {
        // Mod recipe classes could extend vanilla classes so scan them first
        HANDLERS.add(new IC2RecipeHandler());
        HANDLERS.add(new ForestryRecipeHandler());
        HANDLERS.add(new MekanismRecipeHandler());

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
        // XXX: Investigate issues with mods adding recipes too late at some point in the future.
        if (prevListSize != recipes.size() || (!recipes.isEmpty() && prevLastElement != recipes.get(recipes.size() - 1))) {
            Ref.LOGGER.warn("|~| A MOD IS ADDING RECIPES TOO LATE |~| Class=" + recipes.get(recipes.size() - 1).getClass().getCanonicalName());
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

        Collections.sort(allRecipes, WrappedRecipe.Sorter.INSTANCE);

        Ref.LOGGER.info(String.format("Scanned %d recipes (%d failed) in %.8f seconds", recipes.size(), fails, (System.nanoTime() - startTime) / 1000000000.0D));
    }

    public static List<WrappedRecipe> getAllRecipes() {
        return ImmutableList.copyOf(allRecipes);
    }

    /**
     * Only use the inventory that is returned from this method if IRecipe.getCraftingResult() does not check for validity of pattern in crafting inventory slots.
     */
    public static InventoryCrafting getCraftingInventory(List<ItemStack> usedIngredients) {
        InventoryCrafting ic = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer p_75145_1_) {
                return false;
            }

            @Override
            public void onCraftMatrixChanged(IInventory p_75130_1_) {
                //NO-OP
            }
        }, 3, 3);
        for (int i = 0; i < 9; i++) {
            if (i < usedIngredients.size()) {
                ic.setInventorySlotContents(i, usedIngredients.get(i));
            } else {
                ic.setInventorySlotContents(i, null);
            }
        }
        return ic;
    }
}
