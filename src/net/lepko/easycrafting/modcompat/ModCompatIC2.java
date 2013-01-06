package net.lepko.easycrafting.modcompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.RecipeManager;
import net.minecraft.item.crafting.IRecipe;

public class ModCompatIC2 extends ModCompat {

    public ModCompatIC2() {
        super("IC2");
    }

    @Override
    public void scanRecipes(List<IRecipe> recipes) {
        try {
            //
            Iterator<IRecipe> iterator = recipes.iterator();
            while (iterator.hasNext()) {
                IRecipe r = iterator.next();
                String className = r.getClass().getName();
                if (className.equals("ic2.core.AdvRecipe") || className.equals("ic2.core.AdvShapelessRecipe")) {
                    Object[] input = (Object[]) Class.forName(className).getField("input").get(r);
                    ArrayList ingredients = new ArrayList(Arrays.asList(input));
                    RecipeManager.scannedRecipes.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
                    iterator.remove();
                }
            }
            //
        } catch (Exception e) {
            EasyLog.warning("[ModCompat] [" + modID + "] Exception while scanning recipes.", e);
            return;
        }
    }
}
