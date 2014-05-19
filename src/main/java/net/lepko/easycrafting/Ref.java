package net.lepko.easycrafting;

import cpw.mods.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public final class Ref {

    private Ref() {}

    public static final String MOD_ID = "EasyCrafting";
    public static final String RES_DOMAIN = MOD_ID.toLowerCase(Locale.ENGLISH);
    public static final String RES_PREFIX = String.format(Locale.ENGLISH, "%s:", RES_DOMAIN);
    public static final String CHANNEL = RES_DOMAIN;
    public static final String CONFIG_DIR = RES_DOMAIN;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static String MOD_NAME;
    public static String VERSION;
    public static String URL;

    public static void init() {
        MOD_NAME = Loader.instance().activeModContainer().getName();
        VERSION = Loader.instance().activeModContainer().getVersion();
        URL = Loader.instance().activeModContainer().getMetadata().url;

        Ref.LOGGER.info(" > Loading {} | Version {}", MOD_NAME, VERSION);
        Ref.LOGGER.info(" > {}", URL);
    }

    public static final String addDomain(String string) {
        return String.format(Locale.ENGLISH, "%s%s", RES_PREFIX, string);
    }
}
