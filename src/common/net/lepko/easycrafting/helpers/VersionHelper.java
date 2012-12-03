package net.lepko.easycrafting.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import net.lepko.easycrafting.proxy.Proxy;
import cpw.mods.fml.common.Loader;

public class VersionHelper {

	private enum UpdateStatus {
		LATEST, OUTDATED, FAILED, DISABLED
	}

	public static final String VERSION = "1.1.1";
	public static final String MOD_NAME = "Easy Crafting";
	public static final String MOD_ID = "Lepko-EasyCrafting";

	private static final String UPDATE_URL = "http://mods.lepko.net/archive/easycrafting/update.csv";

	private static String[] updateInfo;
	private static UpdateStatus updateStatus;

	public static void performCheck() {
		if (updateStatus == null) {
			updateStatus = updateCheck();
		}
		EasyLog.log("Update check -" + updateStatus + "- Using version " + VERSION + " for " + Loader.instance().getMCVersionString());
		if (updateStatus.equals(UpdateStatus.OUTDATED)) {
			EasyLog.log("Available version " + updateInfo[1].trim() + ". Consider updating!");
		}
		if (updateInfo != null && updateInfo.length >= 3 && !updateInfo[2].trim().equalsIgnoreCase("null")) {
			EasyLog.log(updateInfo[2].trim());
		}
	}

	public static void printToChat() {
		if (updateStatus == null) {
			updateStatus = updateCheck();
		}
		if (updateStatus.equals(UpdateStatus.OUTDATED)) {
			Proxy.proxy.printMessageToChat(ChatFormat.YELLOW + "[" + VersionHelper.MOD_NAME + "] " + ChatFormat.RESET + "Using version " + VERSION + " for " + Loader.instance().getMCVersionString());
			Proxy.proxy.printMessageToChat(ChatFormat.YELLOW + "[" + VersionHelper.MOD_NAME + "] " + ChatFormat.RESET + "Available version " + updateInfo[1].trim() + ". " + ChatFormat.AQUA + "Consider updating!");
		}
		if (updateInfo != null && updateInfo.length >= 3 && !updateInfo[2].trim().equalsIgnoreCase("null")) {
			Proxy.proxy.printMessageToChat(ChatFormat.YELLOW + "[" + VersionHelper.MOD_NAME + "] " + ChatFormat.RESET + updateInfo[2].trim());
		}
	}

	private static UpdateStatus checkVersion(String newVersion) {
		String[] current = VERSION.trim().split("\\.");
		String[] latest = newVersion.trim().split("\\.");

		if (latest.length < 3 || current.length < 3) {
			return UpdateStatus.FAILED;
		}

		int currentMajor = 0;
		int currentMinor = 0;
		int currentRevision = 0;

		int latestMajor = 0;
		int latestMinor = 0;
		int latestRevision = 0;

		try {
			currentMajor = Integer.parseInt(current[0]);
			currentMinor = Integer.parseInt(current[1]);
			currentRevision = Integer.parseInt(current[2]);

			latestMajor = Integer.parseInt(latest[0]);
			latestMinor = Integer.parseInt(latest[1]);
			latestRevision = Integer.parseInt(latest[2]);
		} catch (NumberFormatException nfe) {
			return UpdateStatus.FAILED;
		}

		if (latestMajor > currentMajor) {
			return UpdateStatus.OUTDATED;
		} else if (latestMinor > currentMinor) {
			return UpdateStatus.OUTDATED;
		} else if (latestRevision > currentRevision) {
			return UpdateStatus.OUTDATED;
		}

		return UpdateStatus.LATEST;
	}

	private static UpdateStatus updateCheck() {
		if (EasyConfig.instance().checkForUpdates.getBoolean(true)) {
			String mcversion = Loader.instance().getMCVersionString().split(" ")[1];
			String newVersionString = "";

			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(new URL(UPDATE_URL).openStream()));

				while ((newVersionString = in.readLine()) != null) {
					if (newVersionString.split(",")[0].trim().equals(mcversion)) {
						break;
					}
					newVersionString = "";
				}
			} catch (Exception e) {
				return UpdateStatus.FAILED;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}

			if (newVersionString.trim().isEmpty()) {
				return UpdateStatus.FAILED;
			}

			updateInfo = newVersionString.split(",");

			if (updateInfo.length < 3) {
				return UpdateStatus.FAILED;
			}

			UpdateStatus us = checkVersion(updateInfo[1]);
			return us;
		}
		return UpdateStatus.DISABLED;
	}
}
