package net.lepko.easycrafting.core.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.lepko.easycrafting.EasyCrafting;
import net.lepko.easycrafting.core.ConnectionHandler;
import net.lepko.easycrafting.core.GuiHandler;

public class Proxy {

    public void registerHandlers() {
        // Event Handlers
        FMLCommonHandler.instance().bus().register(ConnectionHandler.INSTANCE);

        // Gui Handlers
        NetworkRegistry.INSTANCE.registerGuiHandler(EasyCrafting.INSTANCE, GuiHandler.INSTANCE);
    }

    public void registerCommands() {
    }
}
