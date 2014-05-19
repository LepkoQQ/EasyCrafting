package net.lepko.easycrafting.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EasyLog {

    private static final Logger logger;

    static {
        logger = LogManager.getLogger(VersionHelper.MOD_ID);
    }

    public static void log(String msg) {
        logger.log(Level.INFO, msg + " == T:" + Thread.currentThread().getName());
    }

    public static void warning(String msg) {
        logger.log(Level.WARN, msg + " == T:" + Thread.currentThread().getName());
    }

    public static void warning(String msg, Throwable throwable) {
        logger.log(Level.WARN, msg + " == T:" + Thread.currentThread().getName(), throwable);
    }

    public static void severe(String msg) {
        logger.log(Level.FATAL, msg + " == T:" + Thread.currentThread().getName());
    }
}
