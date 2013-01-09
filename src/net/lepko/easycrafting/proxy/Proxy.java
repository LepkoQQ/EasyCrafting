package net.lepko.easycrafting.proxy;

import net.lepko.easycrafting.helpers.EasyLog;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.relauncher.Side;

public class Proxy {

    @SidedProxy(clientSide = "net.lepko.easycrafting.proxy.ProxyClient", serverSide = "net.lepko.easycrafting.proxy.Proxy")
    public static Proxy proxy;

    public static String blocksTextureFile = "/net/lepko/easycrafting/textures/blocks.png";

    public void onLoad() {
    }

    public void printMessageToChat(String msg) {
        // Client only; print to console here
        if (msg != null) {
            EasyLog.log("[CHAT] " + msg);
        }
    }

    public boolean isClient() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return true;
        }
        return false;
    }
}
