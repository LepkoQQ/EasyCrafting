package net.lepko.minecraft.easycrafting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class handles the versioning of the mod.
 * It checks for newer versions of the mod, and informs the user if a newer version is available.
 */
public class Version {
	/** Name of this mod */
	public static final String MOD_NAME = "Easy Crafting";
	/** Version of the mod (this will be the user's version) */
	public static final String VERSION = "1.0.1-DEV";
	/** Where to check for new versions */
	private static final String UPDATE_URL = "http://lepko.net/external/easycrafting/update.txt";

	/** Version that is available */
	private static String newVersionString = "";
	/** Short description of available version */
	private static String updateMessageString = "";
	/** Has the update message already been shown to the user? */
	private static boolean updatePrinted = false;

	/**
	 * Checks online if a new version of the mod is available
	 *
	 * @param  N/A
	 * @return N/A
	 */
	public static void updateCheck() {
		if (ModEasyCrafting.instance.checkForUpdates) {
			try {
				URL url = new URL(UPDATE_URL);
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

				newVersionString = in.readLine();
				updateMessageString = in.readLine();

				in.close();
			} catch (Exception e) {
				System.out.println("[" + MOD_NAME + "] Checking for updates failed!");
				return;
			}

			if (!VERSION.contains("-DEV") && !newVersionString.trim().isEmpty() && !VERSION.equalsIgnoreCase(newVersionString)) {
				System.out.println("[" + MOD_NAME + "] Update available! You have: " + VERSION + " Latest: " + newVersionString);
				System.out.println("[" + MOD_NAME + "] " + updateMessageString);
			}
		}
	}

	/**
	 * Notify user if there is a new version available
	 *
	 * @param  N/A
	 * @return N/A
	 */
	public static void updatePrint() {
		if (!VERSION.contains("-DEV") && !updatePrinted && !newVersionString.trim().isEmpty() && !VERSION.equalsIgnoreCase(newVersionString)) {
			ProxyCommon.proxy.printMessageToChat("\u00A7" + "6[" + MOD_NAME + "] " + "\u00A7" + "aUpdate available! " + "\u00A7" + "7You have: " + "\u00A7" + "c" + VERSION + " " + "\u00A7" + "7Latest: " + "\u00A7" + "2" + newVersionString);
			ProxyCommon.proxy.printMessageToChat("\u00A7" + "6[" + MOD_NAME + "] " + "\u00A7" + "f" + updateMessageString);
		}
		updatePrinted = true;
	}
}
