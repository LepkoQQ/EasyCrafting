package net.lepko.easycrafting.core.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import net.lepko.easycrafting.core.ConnectionHandler;
import net.lepko.easycrafting.core.WorldLoadHandler;
import net.minecraftforge.common.MinecraftForge;

public class Proxy {

    public void registerEventHandlers() {
        FMLCommonHandler.instance().bus().register(ConnectionHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(WorldLoadHandler.INSTANCE);
    }

    public void registerCommands() {
    }
}
