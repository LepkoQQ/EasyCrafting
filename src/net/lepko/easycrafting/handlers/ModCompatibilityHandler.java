package net.lepko.easycrafting.handlers;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.modcompat.ModCompatIC2;
import net.minecraft.item.crafting.IRecipe;

public class ModCompatibilityHandler {

    public static void load() {
        EasyLog.log("[ModCompat] Loading mod compatibility modules.");
        ModCompatIC2.load();

        EasyLog.log("[ModCompat] Finished.");

        for (IRecipe r : RecipeHelper.unknownRecipes) {
            EasyLog.log("Skipped unknown recipe: " + r.getClass().getName());
        }
    }

}
