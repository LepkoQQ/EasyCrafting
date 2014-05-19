package net.lepko.easycrafting.core.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.lepko.easycrafting.core.ConnectionHandler;
import net.lepko.easycrafting.core.GuiHandler;
import net.lepko.easycrafting.core.WorldLoadHandler;
import net.minecraftforge.common.MinecraftForge;

public class Proxy {

    public void registerHandlers() {
        // Event Handlers
        FMLCommonHandler.instance().bus().register(ConnectionHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(WorldLoadHandler.INSTANCE);

        // Gui Handlers
        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler.INSTANCE);
    }

    public void registerCommands() {
    }
}
