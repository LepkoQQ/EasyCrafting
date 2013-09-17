package net.lepko.easycrafting;

import net.lepko.easycrafting.block.BlockEasyCraftingTable;
import net.lepko.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.core.CommandEasyCrafting;
import net.lepko.easycrafting.handlers.ConnectionHandler;
import net.lepko.easycrafting.handlers.GuiHandler;
import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.lepko.easycrafting.network.PacketHandler;
import net.lepko.easycrafting.proxy.Proxy;
import net.lepko.easycrafting.recipe.RecipeManager;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = VersionHelper.MOD_ID, name = VersionHelper.MOD_NAME, version = VersionHelper.VERSION,
        dependencies = "after:Forestry;after:LogisticsPipes|Main")
@NetworkMod(clientSideRequired = true, serverSideRequired = true,
        channels = { VersionHelper.MOD_ID }, packetHandler = PacketHandler.class, connectionHandler = ConnectionHandler.class)
public class ModEasyCrafting {

    @Instance(VersionHelper.MOD_ID)
    public static ModEasyCrafting instance;

    // Blocks
    public static Block blockEasyCraftingTable;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        EasyLog.log("Loading " + VersionHelper.MOD_NAME + " version " + VersionHelper.VERSION + ".");
        ConfigHandler.initialize(event.getSuggestedConfigurationFile());
        VersionHelper.performCheck();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        blockEasyCraftingTable = new BlockEasyCraftingTable(ConfigHandler.EASYCRAFTINGTABLE_ID);
        LanguageRegistry.addName(blockEasyCraftingTable, "Easy Crafting Table");

        GameRegistry.registerBlock(blockEasyCraftingTable, "blockEasyCraftingTable");
        GameRegistry.registerTileEntity(TileEntityEasyCrafting.class, "tileEntityEasyCrafting");

        Proxy.proxy.onLoad();

        NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(new net.lepko.easycrafting.handlers.EventHandler());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // TODO: execute appropriate commands on client
        event.registerServerCommand(new CommandEasyCrafting());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        RecipeManager.scanRecipes();
    }
}
