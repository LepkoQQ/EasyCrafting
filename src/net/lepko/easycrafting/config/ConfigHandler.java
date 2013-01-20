package net.lepko.easycrafting.config;

import java.io.File;

import net.minecraftforge.common.Configuration;

public class ConfigHandler {

    /**
     * The {@link Configuration} instance.
     */
    private static Configuration config;

    // Properties
    public static int EASYCRAFTINGTABLE_ID;
    public static int EASYCRAFTINGTABLE_ID_DEFAULT = 2500;

    public static boolean UPDATE_CHECK_ENABLED;
    public static boolean UPDATE_CHECK_ENABLED_DEFAULT = true;
    public static String UPDATE_CHECK_ENABLED_COMMENT = "Whether or not to check and display when a new version of this mod is available.";

    public static int MAX_RECURSION;
    public static int MAX_RECURSION_DEFAULT = 10;
    public static String MAX_RECURSION_COMMENT = "How deep to check for ingredients in multi level crafting, higher values can cause lag; default: 10";

    public static String CUSTOM_RECIPE_INGREDIENTS;
    public static String CUSTOM_RECIPE_INGREDIENTS_DEFAULT = "58,331,340";
    public static String CUSTOM_RECIPE_INGREDIENTS_COMMENT = "Block and item IDs to use in crafting recipe; default: 58,331,340";

    public static void initialize(File file) {
        config = new Configuration(file);

        config.load();

        EASYCRAFTINGTABLE_ID = config.getBlock("EasyCraftingTable", EASYCRAFTINGTABLE_ID_DEFAULT).getInt(EASYCRAFTINGTABLE_ID_DEFAULT);

        UPDATE_CHECK_ENABLED = config.get(Configuration.CATEGORY_GENERAL, "checkForUpdates", UPDATE_CHECK_ENABLED_DEFAULT, UPDATE_CHECK_ENABLED_COMMENT).getBoolean(UPDATE_CHECK_ENABLED_DEFAULT);
        MAX_RECURSION = config.get(Configuration.CATEGORY_GENERAL, "recipeRecursion", MAX_RECURSION_DEFAULT, MAX_RECURSION_COMMENT).getInt(MAX_RECURSION_DEFAULT);

        CUSTOM_RECIPE_INGREDIENTS = config.get(Configuration.CATEGORY_GENERAL, "customRecipeItems", CUSTOM_RECIPE_INGREDIENTS_DEFAULT, CUSTOM_RECIPE_INGREDIENTS_COMMENT).value;

        config.save();
    }

    public static void set(String category, String property, String value) {

        if (config.categories.containsKey(category)) {
            if (config.categories.get(category).containsKey(property)) {
                config.categories.get(category).get(property).value = value;
            }
        }
        config.save();
    }
}
