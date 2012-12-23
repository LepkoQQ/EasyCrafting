package net.lepko.easycrafting.helpers;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class EasyConfig extends Configuration {

    private static EasyConfig instance;

    // Properties
    public Property easyCraftingTableID;
    public Property checkForUpdates;
    public Property recipeRecursion;
    public Property customRecipeItems;

    private EasyConfig(File file) {
        super(file);
    }

    public static void initialize(File file) {
        instance = new EasyConfig(file);
        instance.load();
        instance.defaults();
        instance.save();
    }

    public static EasyConfig instance() {
        return instance;
    }

    private void defaults() {
        easyCraftingTableID = getBlock("EasyCraftingTable", 404);

        checkForUpdates = get(Configuration.CATEGORY_GENERAL, "checkForUpdates", true);
        checkForUpdates.comment = "Whether or not to check and display when a new version of this mod is available.";

        recipeRecursion = get(Configuration.CATEGORY_GENERAL, "allowMultiStepRecipes", 3);
        recipeRecursion.comment = "How deep to check for ingredients in multi level crafting, higher values can cause lag; 0 disable; 10 max";

        customRecipeItems = get(Configuration.CATEGORY_GENERAL, "customRecipeItems", "58,331,340");
        customRecipeItems.comment = "Block and item IDs to use in crafting recipe; default: 58,331,340";
    }
}
