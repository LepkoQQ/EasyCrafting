package net.lepko.easycrafting.helpers;

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
        logger.log(Level.INFO, msg + " == T:" + Thread.currentThread().getName());
    }

    public static void warning(String msg) {
        logger.log(Level.WARNING, msg + " == T:" + Thread.currentThread().getName());
    }

    public static void severe(String msg) {
        logger.log(Level.SEVERE, msg + " == T:" + Thread.currentThread().getName());
    }
}
