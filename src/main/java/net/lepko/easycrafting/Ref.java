package net.lepko.easycrafting;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.SidedProxy;
import net.lepko.easycrafting.core.proxy.Proxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.UUID;

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

    @SidedProxy(clientSide = "net.lepko.easycrafting.core.proxy.ProxyClient", serverSide = "net.lepko.easycrafting.core.proxy.Proxy")
    public static Proxy PROXY;

    public static GameProfile GAME_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("easycrafting.fake.player.profile".getBytes()), "[" + Ref.MOD_ID + "]");

    public static void init() {
        MOD_NAME = Loader.instance().activeModContainer().getName();
        VERSION = Loader.instance().activeModContainer().getVersion();
        URL = Loader.instance().activeModContainer().getMetadata().url;

        LOGGER.info(" > Loading {} | Version {}", MOD_NAME, VERSION);
        LOGGER.info(" > {}", URL);
    }

    public static String addDomain(String string) {
        return String.format(Locale.ENGLISH, "%s%s", RES_PREFIX, string);
    }
}
