package net.lepko.easycrafting.easyobjects;

import java.util.ArrayList;
import java.util.Collections;

import net.lepko.easycrafting.helpers.RecipeHelper;
import net.minecraft.item.ItemStack;

public class EasyRecipe {

    private ArrayList<Object> ingredients;
    private EasyItemStack result;

    /**
     * Creates a new instance, with specified result and ingredients.
     * 
     * The ingredients ArrayList can contain an ItemStack or an EasyItemStack to define a specific ingredient. It can also contain an ArrayList
     * consisting of ItemStacks to define a list of possible ingredients. If it contains a String it will be converted using the forge ore dictionary.
     * 
     * @param result - EasyItemStack representation of the crafting result
     * @param ingredients - and ArrayList of ingredients
     */
    public EasyRecipe(EasyItemStack result, ArrayList<Object> ingredients) {
        this.result = result;
        ingredients.removeAll(Collections.singleton(null));
        this.ingredients = ingredients;
    }

    /**
     * Get the ingredient at the specified index in the recipes list of ingredients.
     * 
     * @param index - Ingredient index in the recipe ingredients list
     * @return Either - an instance of EasyItemStack or an ArrayList (containing ItemStacks); null if index out of bounds or a unrecognizable type.
     */
    public Object getIngredient(int index) {
        if (index < 0 || index > ingredients.size()) {
            return null;
        }
        Object o = ingredients.get(index);
        if (o instanceof EasyItemStack) {
            return o;
        } else if (o instanceof ArrayList) {
            return o;
        } else if (o instanceof ItemStack) {
            EasyItemStack eis = EasyItemStack.fromItemStack((ItemStack) o);
            ingredients.set(index, eis);
            return eis;
        } else if (o instanceof String) {
            ArrayList<ItemStack> list = RecipeHelper.resolveOreAndLiquidDictionaries((String) o);
            ingredients.set(index, list);
            return list;
        }
        return null;
    }

    /**
     * Get the size of the ingredient list.
     * 
     * @return size of the ingredient list
     */
    public int getIngredientsSize() {
        return ingredients.size();
    }

    /**
     * Get the result of the recipe.
     * 
     * @return EasyItemStack instance that is the result of the recipe
     */
    public EasyItemStack getResult() {
        return result;
    }

    /**
     * Get a list of ingredients with sized itemstacks instead of duplicate entries.
     * 
     * @return list of ingredients
     */
    public ArrayList<ItemStack> getCompactIngredientList() {

        ArrayList<ItemStack> list = new ArrayList<ItemStack>();

        if (result.usedIngredients == null) {
            ingredients: for (int i = 0; i < ingredients.size(); i++) {
                Object o = getIngredient(i);

                EasyItemStack eis = null;
                if (o instanceof EasyItemStack) {
                    eis = (EasyItemStack) o;
                } else if (o instanceof ArrayList) {
                    @SuppressWarnings("unchecked")
                    ArrayList<ItemStack> list2 = (ArrayList<ItemStack>) o;
                    if (list2.isEmpty()) {
                        // Missing Ingredient, can't craft
                        return null;
                    }
                    eis = EasyItemStack.fromItemStack(list2.get(0));
                } else {
                    // Invalid ingredient, can't craft
                    return null;
                }

                for (ItemStack is2 : list) {
                    if (eis.equalsItemStack(is2, true)) {
                        is2.stackSize++;
                        continue ingredients;
                    }
                }

                // Some recipes use larger stack sizes in their recipes
                ItemStack is = eis.toItemStack();
                is.stackSize = 1;
                list.add(is);
            }
        } else {
            used: for (ItemStack is : result.usedIngredients) {
                EasyItemStack eis = EasyItemStack.fromItemStack(is);

                for (ItemStack is2 : list) {
                    if (eis.equalsItemStack(is2, true)) {
                        is2.stackSize++;
                        continue used;
                    }
                }

                list.add(eis.toItemStack());
            }
        }

        return list;
    }
}
