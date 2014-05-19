package net.lepko.easycrafting.proxy;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.CommandEasyCrafting;
import net.lepko.easycrafting.core.TickHandlerClient;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;

public class ProxyClient extends Proxy {

    @Override
    public void init() {
        // Register Client Tick Handler
        FMLCommonHandler.instance().bus().register(TickHandlerClient.INSTANCE);

        // Register Client Commands
        ClientCommandHandler.instance.registerCommand(new CommandEasyCrafting());
    }

    @Override
    public void printMessageToChat(String msg) {
        if (msg != null) {
            if (FMLClientHandler.instance().getClient().ingameGUI != null) {
                FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(msg));
            } else {
                Ref.LOGGER.info("[CHAT] " + msg);
            }
        }
    }
}
