package net.lepko.minecraft.easycrafting.helpers;

import java.io.File;

import net.minecraftforge.common.Configuration;

public class EasyConfig {

	// Configuration variables
	public static boolean UPDATE_CHECK = false;
	public static boolean RECIPE_ITEMS = true;
	public static int BLOCK_EASY_CRAFTING_ID = 404;
	public static int RECIPE_RECURSION = 5;

	public static void loadConfig(File configFile) {
		Configuration config = new Configuration(configFile);
		config.load();

		BLOCK_EASY_CRAFTING_ID = config.getBlock("EasyCraftingTable", 404).getInt(404);
		RECIPE_ITEMS = config.get(Configuration.CATEGORY_GENERAL, "useRedstoneRecipe", true).getBoolean(true);
		UPDATE_CHECK = config.get(Configuration.CATEGORY_GENERAL, "checkForUpdates", true).getBoolean(true);
		RECIPE_RECURSION = config.get(Configuration.CATEGORY_GENERAL, "allowMultiStepRecipes", 3).getInt(3);

		config.save();
	}
}
