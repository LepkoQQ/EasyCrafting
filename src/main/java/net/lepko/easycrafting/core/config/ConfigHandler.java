package net.lepko.easycrafting.core.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {

    private static Configuration config;

    public static int MAX_RECURSION;
    public static int MAX_RECURSION_DEFAULT = 10;
    public static String MAX_RECURSION_COMMENT = "How deep to check for ingredients in multi level crafting, higher values can cause lag; default: 10";

    public static void initialize(File file) {
        if (config == null) {
            config = new Configuration(file);
        }

        config.load();

        MAX_RECURSION = config.get(Configuration.CATEGORY_GENERAL, "recipeRecursion", MAX_RECURSION_DEFAULT, MAX_RECURSION_COMMENT).getInt(MAX_RECURSION_DEFAULT);

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void setRecursion(int value) {
        MAX_RECURSION = value;
        config.getCategory(Configuration.CATEGORY_GENERAL).get("recipeRecursion").set(value);
        config.save();
    }
}
