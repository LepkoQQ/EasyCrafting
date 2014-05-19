package net.lepko.easycrafting.core.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import net.lepko.easycrafting.core.CommandEasyCrafting;
import net.lepko.easycrafting.core.TickHandlerClient;
import net.minecraftforge.client.ClientCommandHandler;

public class ProxyClient extends Proxy {

    @Override
    public void registerEventHandlers() {
        super.registerEventHandlers();

        FMLCommonHandler.instance().bus().register(TickHandlerClient.INSTANCE);
    }

    @Override
    public void registerCommands() {
        super.registerCommands();

        ClientCommandHandler.instance.registerCommand(new CommandEasyCrafting());
    }
}
