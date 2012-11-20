package net.lepko.minecraft.easycrafting.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.Configuration.UnicodeInputStreamReader;

public class EasyConfig {

	// Configuration file
	private static File configFile;

	// TODO: Sort by key alphabetically when saving
	private static Map<String, Option> optionMap = new HashMap<String, EasyConfig.Option>();

	// Mod options
	public static Option UPDATE_CHECK = new Option("Option.checkForUpdates", true, "Check for updates", "Whether or not to check and display when a new version of this mod is available.");
	public static Option RECIPE_CUSTOM_ITEMS = new Option("Option.customRecipeItems", false, "Custom recipe items", "If set to true, specify the item IDs in recipeItemIDs.");
	public static Option RECIPE_CUSTOM_IDS = new Option("Option.customRecipeItemIDs", "58,331,340", "Custom recipe ID list", "Block and item IDs to use if customRecipeItems recipe is enabled. Example: 58,331,340");
	public static Option RECIPE_RECURSION = new Option("Option.craftingRecursion", 5, 0, 10, "Crafting recursion", "How deep to check for ingredients in multi level crafting, higher values can cause lag; 0 disable; 10 max");

	// Block IDs
	public static Option BLOCK_ID_EASY_CRAFTING_TABLE = new Option("BlockID.EasyCraftingTable", 404, 0, 4096, "Block ID for EasyCrafting Table");

	public static void loadConfig(File configDir) {
		if (configFile == null) {
			configFile = new File(configDir, VersionHelper.MOD_ID + "_options.cfg");
		}

		BufferedReader buffer = null;
		try {
			if (!configFile.exists() && !configFile.createNewFile()) {
				return;
			}
			if (configFile.canRead()) {
				buffer = new BufferedReader(new UnicodeInputStreamReader(new FileInputStream(configFile), "UTF-8"));

				String line;

				while (true) {
					line = buffer.readLine();

					if (line == null || line.isEmpty()) {
						break;
					}

					if (line.charAt(0) == '#') {
						continue;
					}

					String[] split = line.split("=");

					if (split.length != 2 || !optionMap.containsKey(split[0].trim())) {
						continue;
					}

					Option o = optionMap.get(split[0].trim());
					o.setValue(split[1].trim());
				}
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
				}
			}
		}

		saveConfig();

		// TODO: remove
		KeyHelper.addKeyBind("key.easyoptions", "KEY_DECIMAL");
	}

	public static void saveConfig() {
		if (configFile == null) {
			return;
		}

		BufferedWriter buffer = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd at HH:mm:ss Z");
		try {
			if (!configFile.exists() && !configFile.createNewFile()) {
				return;
			}
			if (configFile.canWrite()) {
				buffer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8"));

				buffer.write("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #\r\n");
				buffer.write("# " + VersionHelper.MOD_NAME + " - Mod Configuration File\r\n");
				buffer.write("# Saved " + sdf.format(new Date()) + " by mod version " + VersionHelper.VERSION + "\r\n");
				buffer.write("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #\r\n");
				buffer.write("\r\n");

				Option[] array = optionMap.values().toArray(new Option[optionMap.size()]);
				Arrays.sort(array, new Comparator<Option>() {
					@Override
					public int compare(Option o1, Option o2) {
						return o1.getKey().compareTo(o2.getKey());
					}
				});
				for (int i = 0; i < array.length; i++) {
					buffer.write("# " + array[i].getDescription() + "\r\n");
					buffer.write(array[i].getKey() + "=" + array[i].getValue() + "\r\n");
					buffer.write("\r\n");
				}
			}
		} catch (UnsupportedEncodingException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static class Option {

		private String key;
		private String displayName;
		private String description;
		private String value;
		private int min = 0;
		private int max = 0;

		private Option(String key, int defaultValue, int minValue, int maxValue, String description) {
			this(key, defaultValue, minValue, maxValue, "", description);
		}

		private Option(String key, int defaultValue, int minValue, int maxValue, String displayName, String description) {
			this(key, Integer.toString(defaultValue), displayName, description);
			this.min = minValue;
			this.max = maxValue;
		}

		private Option(String key, boolean defaultValue, String displayName, String description) {
			this(key, Boolean.toString(defaultValue), displayName, description);
		}

		private Option(String key, String defaultValue, String displayName, String description) {
			this.key = key;
			this.displayName = displayName;
			this.value = defaultValue;
			this.description = description;
			optionMap.put(key, this);
		}

		public void setValue(boolean newValue) {
			if (isBoolean()) {
				setValue(Boolean.toString(newValue));
			}
		}

		public void setValue(int newValue) {
			if (isInteger()) {
				if (newValue > max) {
					newValue = max;
				} else if (newValue < min) {
					newValue = min;
				}
				setValue(Integer.toString(newValue));
			}
		}

		private void setValue(String newValue) {
			if (isBoolean() && ("true".equals(newValue.toLowerCase()) || "false".equals(newValue.toLowerCase()))) {
				this.value = newValue;
			} else if (isInteger()) {
				try {
					Integer.parseInt(newValue);
				} catch (NumberFormatException nfe) {
					return;
				}
				this.value = newValue;
			} else {
				this.value = newValue;
			}
		}

		public String getKey() {
			return key;
		}

		public String getDescription() {
			return description;
		}

		public String getValue() {
			return value;
		}

		public boolean getBooleanValue() {
			return Boolean.parseBoolean(value);
		}

		public int getIntegerValue() {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException nfe) {
				return -1;
			}
		}

		private boolean isBoolean() {
			return ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase()));
		}

		private boolean isInteger() {
			try {
				Integer.parseInt(value);
				return true;
			} catch (NumberFormatException nfe) {
				return false;
			}
		}

		public int getMax() {
			return max;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
}
