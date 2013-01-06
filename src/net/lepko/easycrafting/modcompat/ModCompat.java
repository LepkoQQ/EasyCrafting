package net.lepko.easycrafting.modcompat;

import java.util.List;

import net.lepko.easycrafting.handlers.ModCompatibilityHandler;
import net.lepko.easycrafting.helpers.EasyLog;
import net.minecraft.item.crafting.IRecipe;
import cpw.mods.fml.common.Loader;

public abstract class ModCompat {

    public boolean isModLoaded = false;
    public String modID;

    public ModCompat(String modID) {
        this.modID = modID;
        ModCompatibilityHandler.mods.put(modID, this);
    }

    public void load() {
        log("Checking for mod...");
        if (!Loader.isModLoaded(modID)) {
            log("Mod not found.");
            return;
        }
        EasyLog.log("Mod found.");
        isModLoaded = true;
    }

    public abstract void scanRecipes(List<IRecipe> recipes);

    protected void log(String msg) {
        EasyLog.log("[ModCompat] [" + modID + "] " + msg);
    }

    public static final boolean isLoaded(String modID) {
        if (ModCompatibilityHandler.mods.get(modID) != null) {
            return ModCompatibilityHandler.mods.get(modID).isModLoaded;
        }
        return false;
    }
}
