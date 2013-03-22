package net.lepko.easycrafting.proxy;

import net.lepko.easycrafting.handlers.TickHandlerClient;
import net.lepko.easycrafting.helpers.EasyLog;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ProxyClient extends Proxy {

    @Override
    public void onLoad() {
        // Register Client Tick Handler
        TickRegistry.registerTickHandler(new TickHandlerClient(), Side.CLIENT);
    }

    @Override
    public void printMessageToChat(String msg) {
        if (msg != null) {
            if (FMLClientHandler.instance().getClient().ingameGUI != null) {
                FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(msg);
            } else {
                EasyLog.log("[CHAT] " + msg);
            }
        }
    }
}
