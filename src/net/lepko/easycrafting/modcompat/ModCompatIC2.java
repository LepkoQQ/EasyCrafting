package net.lepko.easycrafting.modcompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.helpers.RecipeHelper.RecipeComparator;
import net.minecraft.item.crafting.IRecipe;
import cpw.mods.fml.common.Loader;

public class ModCompatIC2 {

    public static boolean isModLoaded = false;

    public static void load() {
        EasyLog.log("[ModCompat] [IC2] Checking for mod...");
        if (!Loader.isModLoaded("IC2")) {
            EasyLog.log("[ModCompat] [IC2] Mod not found.");
            return;
        }
        EasyLog.log("[ModCompat] [IC2] Mod found. Loading...");
        isModLoaded = true;

        try {
            //
            ArrayList<EasyRecipe> tmp = new ArrayList<EasyRecipe>();

            Iterator<IRecipe> iterator = RecipeHelper.unknownRecipes.iterator();
            while (iterator.hasNext()) {
                IRecipe r = iterator.next();
                String className = r.getClass().getName();
                if (className.equals("ic2.core.AdvRecipe") || className.equals("ic2.core.AdvShapelessRecipe")) {
                    Object[] input = (Object[]) Class.forName(className).getField("input").get(r);
                    ArrayList ingredients = new ArrayList(Arrays.asList(input));
                    tmp.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
                    iterator.remove();
                }
            }

            RecipeHelper.allRecipes.addAll(tmp);
            Collections.sort(RecipeHelper.allRecipes, new RecipeComparator());
            //
        } catch (Exception e) {
            EasyLog.warning("[ModCompat] [IC2] Exception.", e);
            return;
        }

        EasyLog.log("[ModCompat] [IC2] Loaded.");
    }
}
