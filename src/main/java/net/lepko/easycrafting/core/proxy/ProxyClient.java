package net.lepko.easycrafting.core.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import net.lepko.easycrafting.core.CommandEasyCrafting;
import net.lepko.easycrafting.core.recipe.RecipeChecker;
import net.minecraftforge.client.ClientCommandHandler;

public class ProxyClient extends Proxy {

    @Override
    public void registerHandlers() {
        super.registerHandlers();

        FMLCommonHandler.instance().bus().register(RecipeChecker.INSTANCE);
    }

    @Override
    public void registerCommands() {
        super.registerCommands();

        ClientCommandHandler.instance.registerCommand(new CommandEasyCrafting());
    }
}
