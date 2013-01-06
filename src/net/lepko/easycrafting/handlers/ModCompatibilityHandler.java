package net.lepko.easycrafting.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.modcompat.ModCompat;
import net.lepko.easycrafting.modcompat.ModCompatEE3;
import net.lepko.easycrafting.modcompat.ModCompatIC2;
import net.minecraft.item.crafting.IRecipe;

public class ModCompatibilityHandler {

    public static Map<String, ModCompat> mods = new HashMap<String, ModCompat>();

    public static void load() {
        EasyLog.log("[ModCompat] Loading mod compatibility modules.");

        new ModCompatEE3();
        new ModCompatIC2();

        for (Map.Entry<String, ModCompat> entry : mods.entrySet()) {
            entry.getValue().load();
        }

        EasyLog.log("[ModCompat] Finished.");
    }

    public static void scanRecipes(List<IRecipe> recipes) {
        for (Map.Entry<String, ModCompat> entry : mods.entrySet()) {
            if (entry.getValue().isModLoaded) {
                entry.getValue().scanRecipes(recipes);
            }
        }
    }
}
