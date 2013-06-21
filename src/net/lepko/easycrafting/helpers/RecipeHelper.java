package net.lepko.easycrafting.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.handlers.ModCompatibilityHandler;
import net.lepko.util.InventoryUtil;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.liquids.LiquidContainerData;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class RecipeHelper {

    private static int lastRecipeListSize = 0;
    public static List<EasyRecipe> scannedRecipes = new ArrayList<EasyRecipe>();

    public static void checkForNewRecipes() {
        @SuppressWarnings("unchecked")
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();

        if (lastRecipeListSize < recipes.size()) {
            List<IRecipe> newRecipes = new ArrayList<IRecipe>();
            for (int i = lastRecipeListSize; i < recipes.size(); i++) {
                newRecipes.add(recipes.get(i));
            }
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
            ArrayList<Object> ingredients = RecipeHelper.getIngredientList(r);
            if (ingredients != null && r.getRecipeOutput() != null) {
                if (Item.itemsList[r.getRecipeOutput().itemID] != null) {
                    tmp.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
                } else {
                    EasyLog.warning("Invalid Recipe Output for " + r.getClass().getName() + ". Item with ID " + r.getRecipeOutput().itemID + " is null!");
                }
            } else {
                EasyLog.log("Unknown Recipe: " + r.getClass().getName());
            }
        }

        scannedRecipes.addAll(tmp);
        Collections.sort(scannedRecipes, new RecipeComparator());

        //
        EasyLog.log(String.format("Scanned %d new recipes in %.8f seconds", size, (System.nanoTime() - beforeTime) / 1000000000.0D));
    }

    /**
     * Get a list of all recipes that are scanned and available. If recipes are not yet scanned it will return an empty list.
     */
    public static ImmutableList<EasyRecipe> getAllRecipes() {
        return ImmutableList.copyOf(scannedRecipes);
    }

    /**
     * Returns a list of recipes that can be crafted using ingredients from the specified inventory.
     * 
     * @param inventory - inventory to check with
     * @param maxRecursion - how deep to recurse when trying to craft
     * @param recipesToCheck - a list of recipes to be checked
     */
    public static ArrayList<EasyRecipe> getCraftableRecipes(InventoryPlayer inventory, int maxRecursion, List<EasyRecipe> recipesToCheck) {
        ArrayList<EasyRecipe> tmpCraftable = new ArrayList<EasyRecipe>();
        ArrayList<EasyRecipe> tmpAll = new ArrayList<EasyRecipe>(recipesToCheck);

        // TODO: timeout
        for (EasyRecipe er : tmpAll) {
            if (canCraft(er, inventory)) {
                tmpCraftable.add(er);
            }
        }
        tmpAll.removeAll(tmpCraftable);

        if (!tmpCraftable.isEmpty()) {
            for (int recursion = 0; recursion < maxRecursion; recursion++) {
                if (tmpAll.isEmpty()) {
                    break;
                }

                ImmutableList<EasyRecipe> immutableCraftable = ImmutableList.copyOf(tmpCraftable);
                for (EasyRecipe er : tmpAll) {
                    if (canCraft(er, inventory, immutableCraftable, maxRecursion)) {
                        tmpCraftable.add(er);
                    }
                }
                tmpAll.removeAll(tmpCraftable);

                if (immutableCraftable.size() == tmpCraftable.size()) {
                    break;
                }
            }
        }
        return tmpCraftable;
    }

    /**
     * Check if a recipe can be crafted with the ingredients from the inventory.
     * 
     * @param recipe - recipe to check
     * @param inventory - inventory to use the ingredients from
     * 
     * @see #canCraft(EasyRecipe, InventoryPlayer, ImmutableList, boolean, int)
     */
    public static boolean canCraft(EasyRecipe recipe, InventoryPlayer inventory) {
        return canCraft(recipe, inventory, null, false, 1, 0) > 0;
    }

    /**
     * Check if a recipe can be crafted with the ingredients from the inventory. If an ingredient is missing try to craft it from a list of recipes.
     * 
     * @param recipe - recipe to check
     * @param inventory - inventory to use the ingredients from
     * @param recipesToCheck - a list of recipes to try and craft from if an ingredient is missing
     * @param recursion - how deep to recurse while trying to craft an ingredient (must be nonnegative)
     * 
     * @see #canCraft(EasyRecipe, InventoryPlayer, ImmutableList, boolean, int)
     */
    public static boolean canCraft(EasyRecipe recipe, InventoryPlayer inventory, ImmutableList<EasyRecipe> recipesToCheck, int recursion) {
        return canCraft(recipe, inventory, recipesToCheck, false, 1, recursion) > 0;
    }

    /**
     * Check if a recipe can be crafted with the ingredients from the inventory. If an ingredient is missing try to craft it from a list of recipes.
     * 
     * @param recipe - recipe to check
     * @param inventory - inventory to use the ingredients from
     * @param recipesToCheck - a list of recipes to try and craft from if an ingredient is missing
     * @param take - whether or not to take the ingredients from the inventory
     * @param recursion - how deep to recurse while trying to craft an ingredient (must be nonnegative)
     */
    public static int canCraft(EasyRecipe recipe, InventoryPlayer inventory, ImmutableList<EasyRecipe> recipesToCheck, boolean take, int maxTimes, int recursion) {
        if (recursion < 0) {
            return 0;
        }

        recipe.getResult().setCharge(null);

        InventoryPlayer tmp = new InventoryPlayer(inventory.player);
        InventoryPlayer tmp2 = new InventoryPlayer(inventory.player);
        tmp.copyInventory(inventory);

        List<ItemStack> usedIngredients = new ArrayList<ItemStack>();

        int timesCrafted = 0;
        timesLoop: while (timesCrafted < maxTimes) {

            iiLoop: for (int ii = 0; ii < recipe.getIngredientsSize(); ii++) {
                if (recipe.getIngredient(ii) instanceof EasyItemStack) {
                    EasyItemStack ingredient = (EasyItemStack) recipe.getIngredient(ii);
                    int inventoryIndex = InventoryUtil.isItemInInventory(tmp, ingredient);
                    if (inventoryIndex != -1 && InventoryUtil.consumeItemForCrafting(tmp, inventoryIndex, usedIngredients)) {
                        continue iiLoop;
                    }
                    //
                    if (recipesToCheck != null && recursion - 1 >= 0) {
                        ArrayList<EasyRecipe> list = getRecipesForItemFromList(ingredient, recipesToCheck);
                        for (EasyRecipe er : list) {
                            if (canCraft(er, tmp, recipesToCheck, true, 1, recursion - 1) > 0) {
                                ItemStack is = er.getResult().toItemStack();
                                is.stackSize--;
                                if (is.stackSize > 0 && !tmp.addItemStackToInventory(is)) {
                                    break timesLoop;
                                }
                                ItemStack usedItemStack = is.copy();
                                usedItemStack.stackSize = 1;
                                usedIngredients.add(usedItemStack);
                                continue iiLoop;
                            }
                        }
                    }
                    //
                    break timesLoop;
                } else if (recipe.getIngredient(ii) instanceof ArrayList) {
                    @SuppressWarnings("unchecked")
                    ArrayList<ItemStack> ingredients = (ArrayList<ItemStack>) recipe.getIngredient(ii);
                    int inventoryIndex = InventoryUtil.isItemInInventory(tmp, ingredients);
                    if (inventoryIndex != -1 && InventoryUtil.consumeItemForCrafting(tmp, inventoryIndex, usedIngredients)) {
                        continue iiLoop;
                    }
                    //
                    if (recipesToCheck != null && recursion - 1 >= 0) {
                        ArrayList<EasyRecipe> list = getRecipesForItemFromList(ingredients, recipesToCheck);
                        for (EasyRecipe er : list) {
                            if (canCraft(er, tmp, recipesToCheck, true, 1, recursion - 1) > 0) {
                                ItemStack is = er.getResult().toItemStack();
                                is.stackSize--;
                                if (is.stackSize > 0 && !tmp.addItemStackToInventory(is)) {
                                    break timesLoop;
                                }
                                ItemStack usedItemStack = is.copy();
                                usedItemStack.stackSize = 1;
                                usedIngredients.add(usedItemStack);
                                continue iiLoop;
                            }
                        }
                    }
                    //
                    break timesLoop;
                }
            }

            timesCrafted++;
            tmp2.copyInventory(tmp);
        }

        if (timesCrafted > 0) {
            recipe.getResult().setCharge(usedIngredients);
            if (take) {
                InventoryUtil.setContents(inventory, tmp2);
            }
        }
        return timesCrafted;
    }

    /**
     * Get a list of recipes that can be used to craft a specified item/block.
     * 
     * @param ingredient - the item/block we want to craft
     * @param recipesToCheck - a list of recipes to be checked
     */
    private static ArrayList<EasyRecipe> getRecipesForItemFromList(EasyItemStack ingredient, ImmutableList<EasyRecipe> recipesToCheck) {
        ArrayList<EasyRecipe> returnList = new ArrayList<EasyRecipe>();
        for (EasyRecipe er : recipesToCheck) {
            if (er.getResult().equals(ingredient, true)) {
                returnList.add(er);
            }
        }
        return returnList;
    }

    /**
     * Same as {@link #getRecipesForItemFromList(EasyItemStack, ImmutableList)} but for any of the items contained in the list.
     * 
     * @param ingredients - a list of itemstacks
     * @param recipesToCheck - a list of recipes to be checked
     */
    private static ArrayList<EasyRecipe> getRecipesForItemFromList(ArrayList<ItemStack> ingredients, ImmutableList<EasyRecipe> recipesToCheck) {
        ArrayList<EasyRecipe> returnList = new ArrayList<EasyRecipe>();
        for (ItemStack is : ingredients) {
            returnList.addAll(getRecipesForItemFromList(EasyItemStack.fromItemStack(is), recipesToCheck));
        }
        return returnList;
    }

    /**
     * Get the recipe that matches provided result and ingredients.
     * 
     * @param result
     * @param ingredients
     * @return the matched EasyRecipe instance, null if none of the recipes match
     */
    public static EasyRecipe getValidRecipe(EasyItemStack result, ItemStack[] ingredients) {
        ImmutableList<EasyRecipe> all = getAllRecipes();
        allLoop: for (EasyRecipe r : all) {
            if (r.getResult().equals(result) && r.getIngredientsSize() == ingredients.length) {
                ingLoop: for (int j = 0; j < r.getIngredientsSize(); j++) {
                    if (r.getIngredient(j) instanceof EasyItemStack) {
                        EasyItemStack eis = (EasyItemStack) r.getIngredient(j);
                        if (!eis.equalsItemStack(ingredients[j])) {
                            continue allLoop;
                        }
                    } else if (r.getIngredient(j) instanceof List) {
                        if (ingredients[j].itemID == 0) {
                            return null;
                        }
                        @SuppressWarnings("rawtypes")
                        List ingList = (List) r.getIngredient(j);
                        if (ingList.isEmpty()) {
                            return null;
                        }
                        for (Object is : ingList) {
                            if (is instanceof ItemStack) {
                                if (EasyItemStack.fromItemStack((ItemStack) is).equalsItemStack(ingredients[j])) {
                                    continue ingLoop;
                                }
                            }
                        }
                        continue allLoop;
                    }
                }
                return r;
            }
        }
        return null;
    }

    /**
     * How many times can we fit the resulting itemstack in players hand.
     * 
     * @param result itemstack we are trying to fit
     * @param inHand itemstack currently in hand
     */
    public static int calculateCraftingMultiplierUntilMaxStack(ItemStack result, ItemStack inHand) {
        int maxTimes = (int) ((double) result.getMaxStackSize() / (double) result.stackSize);
        if (inHand != null) {
            int diff = result.getMaxStackSize() - maxTimes * result.stackSize;
            if (inHand.stackSize > diff) {
                maxTimes -= (int) ((double) (inHand.stackSize - diff) / (double) result.stackSize + 1);
            }
        }
        return maxTimes;
    }

    /**
     * Resolves the provided string to a list of itemstacks by querying the ore dictionary. Also handles liquid containers from IC2 recipes.
     * 
     * @param string - name of the ore dictionary entry to resolve
     * @return a list of itemstacks registered under the provided name
     */
    public static ArrayList<ItemStack> resolveOreAndLiquidDictionaries(String string) {
        if (string.startsWith("liquid$")) {
            ArrayList<ItemStack> result = new ArrayList<ItemStack>();

            int separator = string.indexOf(':');
            int id;
            int meta = -1;

            try {
                if (separator > -1) {
                    id = Integer.parseInt(string.substring(7, separator - 1));
                    meta = Integer.parseInt(string.substring(separator + 1));
                } else {
                    id = Integer.parseInt(string.substring(7));
                }
            } catch (NumberFormatException e) {
                EasyLog.warning("Execption while resolving liquid dictionary!", e);
                return result;
            }

            for (LiquidContainerData data : LiquidContainerRegistry.getRegisteredLiquidContainerData()) {
                if (data.stillLiquid.itemID == id && (meta == -1 || data.stillLiquid.itemMeta == meta)) {
                    result.add(data.filled);
                }
            }

            return result;
        }
        return OreDictionary.getOres(string);
    }

    /**
     * Compare recipes by their results (id, damage, stack size).
     */
    public static class RecipeComparator implements Comparator<EasyRecipe> {

        @Override
        public int compare(EasyRecipe o1, EasyRecipe o2) {
            if (o1.getResult().getID() > o2.getResult().getID()) {
                return 1;
            } else if (o1.getResult().getID() < o2.getResult().getID()) {
                return -1;
            }

            if (o1.getResult().getDamage() > o2.getResult().getDamage()) {
                return 1;
            } else if (o1.getResult().getDamage() < o2.getResult().getDamage()) {
                return -1;
            }

            if (o1.getResult().getSize() > o2.getResult().getSize()) {
                return 1;
            } else if (o1.getResult().getSize() < o2.getResult().getSize()) {
                return -1;
            }
            return 0;
        }
    }

    /**
     * Get list of ingredients from the provided recipe.
     * 
     * @param recipe - object instance implementing IRecipe interface
     * @return ArrayList of ingredients or null if not a valid recipe
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ArrayList<Object> getIngredientList(IRecipe recipe) {
        ArrayList ingredients = null;
        // vanilla recipe classes
        if (recipe instanceof ShapedRecipes) {
            ingredients = new ArrayList(Arrays.asList(((ShapedRecipes) recipe).recipeItems));
        } else if (recipe instanceof ShapelessRecipes) {
            ingredients = new ArrayList(((ShapelessRecipes) recipe).recipeItems);
        }
        // ore dictionary classes
        else if (recipe instanceof ShapedOreRecipe) {
            Object[] input = ReflectionHelper.<Object[], ShapedOreRecipe> getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe) recipe, 3);
            ingredients = new ArrayList(Arrays.asList(input));
        } else if (recipe instanceof ShapelessOreRecipe) {
            List input = ReflectionHelper.<List, ShapelessOreRecipe> getPrivateValue(ShapelessOreRecipe.class, (ShapelessOreRecipe) recipe, 1);
            ingredients = new ArrayList(input);
        }
        // remove all null elements
        if (ingredients != null) {
            ingredients.removeAll(Collections.singleton(null));
        }
        return ingredients;
    }
}
