package net.lepko.easycrafting.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.relauncher.Side;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.ConnectionHandler;

public class Proxy {

    @SidedProxy(clientSide = "net.lepko.easycrafting.proxy.ProxyClient", serverSide = "net.lepko.easycrafting.proxy.Proxy")
    public static Proxy proxy;

    public void init() {
        FMLCommonHandler.instance().bus().register(ConnectionHandler.INSTANCE);
    }

    public void printMessageToChat(String msg) {
        // Client only; print to console here
        if (msg != null) {
            Ref.LOGGER.info("[CHAT] " + msg);
        }
    }

    public boolean isClient() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return true;
        }
        return false;
    }

    public void replaceRecipe(String itemIDs) {
    }
}
