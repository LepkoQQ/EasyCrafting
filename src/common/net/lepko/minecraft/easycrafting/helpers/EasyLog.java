package net.lepko.minecraft.easycrafting.helpers;

import java.util.logging.Level;
import java.util.logging.Logger;

import cpw.mods.fml.common.FMLLog;

public class EasyLog {

	private static final Logger logger;
	static {
		logger = Logger.getLogger(VersionHelper.MOD_ID);
		logger.setParent(FMLLog.getLogger());
	}

	public static void log(String msg) {
		logger.log(Level.INFO, msg);
	}

	public static void warning(String msg) {
		logger.log(Level.WARNING, msg);
	}

	public static void severe(String msg) {
		logger.log(Level.SEVERE, msg);
	}
}
