package net.lepko.easycrafting;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.lepko.easycrafting.core.GuiHandler;
import net.lepko.easycrafting.core.block.ModBlocks;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.lepko.easycrafting.core.network.PacketHandler;
import net.lepko.easycrafting.core.recipe.RecipeManager;

@Mod(modid = Ref.MOD_ID, useMetadata = true)
public class EasyCrafting {

    @Instance(Ref.MOD_ID)
    public static EasyCrafting instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Ref.init();
        ConfigHandler.initialize(event.getSuggestedConfigurationFile());

        ModBlocks.setupBlocks();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Ref.PROXY.registerEventHandlers();
        Ref.PROXY.registerCommands();

        PacketHandler.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        //TODO: VersionHelper.performCheck();
        RecipeManager.scanRecipes();
    }
}
