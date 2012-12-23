package net.lepko.easycrafting;

import net.lepko.easycrafting.block.BlockEasyCraftingTable;
import net.lepko.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.handlers.GuiHandler;
import net.lepko.easycrafting.handlers.PacketHandlerClient;
import net.lepko.easycrafting.handlers.PacketHandlerServer;
import net.lepko.easycrafting.helpers.EasyConfig;
import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = VersionHelper.MOD_ID, name = VersionHelper.MOD_NAME, version = VersionHelper.VERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = true, clientPacketHandlerSpec = @SidedPacketHandler(channels = { "EasyCrafting" }, packetHandler = PacketHandlerClient.class), serverPacketHandlerSpec = @SidedPacketHandler(channels = { "EasyCrafting" }, packetHandler = PacketHandlerServer.class))
public class ModEasyCrafting {

    @Instance(VersionHelper.MOD_ID)
    public static ModEasyCrafting instance = new ModEasyCrafting();

    // Blocks
    public static Block blockEasyCraftingTable;

    @PreInit
    public void preload(FMLPreInitializationEvent event) {
        EasyLog.log("Loading " + VersionHelper.MOD_NAME + " version " + VersionHelper.VERSION + ".");
        EasyConfig.initialize(event.getSuggestedConfigurationFile());
        VersionHelper.performCheck();
    }

    @Init
    public void load(FMLInitializationEvent event) {
        blockEasyCraftingTable = new BlockEasyCraftingTable(EasyConfig.instance().easyCraftingTableID.getInt());
        LanguageRegistry.addName(blockEasyCraftingTable, "Easy Crafting Table");

        GameRegistry.registerBlock(blockEasyCraftingTable);
        GameRegistry.registerTileEntity(TileEntityEasyCrafting.class, "tileEntityEasyCrafting");

        // Recipe from config
        String recipeItems = EasyConfig.instance().customRecipeItems.value;
        String[] items = recipeItems.split(",");
        Object[] array = new Object[items.length];
        for (int i = 0; i < items.length; i++) {
            try {
                array[i] = new ItemStack(Integer.parseInt(items[i]), 1, -1);
            } catch (NumberFormatException nfe) {
                EasyLog.warning("customRecipeItems: '" + recipeItems + "' is not valid; Using default!");
                array = new Object[] { Block.workbench, Item.book, Item.redstone };
                break;
            }
        }
        GameRegistry.addShapelessRecipe(new ItemStack(blockEasyCraftingTable, 1), array);
        //

        Proxy.proxy.onLoad();

        NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
    }

    @PostInit
    public void postload(FMLPostInitializationEvent event) {
        RecipeHelper.scanRecipes();
    }
}
