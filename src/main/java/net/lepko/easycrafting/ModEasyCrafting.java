package net.lepko.easycrafting;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.lepko.easycrafting.block.ModBlocks;
import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.core.EasyLog;
import net.lepko.easycrafting.core.EventHandlerEC;
import net.lepko.easycrafting.core.GuiHandler;
import net.lepko.easycrafting.core.VersionHelper;
import net.lepko.easycrafting.network.PacketHandler;
import net.lepko.easycrafting.proxy.Proxy;
import net.lepko.easycrafting.recipe.RecipeManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = VersionHelper.MOD_ID, name = VersionHelper.MOD_NAME, version = VersionHelper.VERSION)
public class ModEasyCrafting {

    @Instance(VersionHelper.MOD_ID)
    public static ModEasyCrafting instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        EasyLog.log("Loading " + VersionHelper.MOD_NAME + " version " + VersionHelper.VERSION + ".");
        ConfigHandler.initialize(event.getSuggestedConfigurationFile());
        VersionHelper.performCheck();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ModBlocks.setupBlocks();
        Proxy.proxy.onLoad();

        PacketHandler.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(new EventHandlerEC());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        RecipeManager.scanRecipes();
    }
}
