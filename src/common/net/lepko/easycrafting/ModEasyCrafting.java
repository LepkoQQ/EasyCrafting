package net.lepko.easycrafting;

import net.lepko.easycrafting.block.BlockEasyCraftingTable;
import net.lepko.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.handlers.GuiHandler;
import net.lepko.easycrafting.handlers.PacketHandlerClient;
import net.lepko.easycrafting.handlers.PacketHandlerServer;
import net.lepko.easycrafting.helpers.EasyConfig;
import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
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
		GameRegistry.addShapelessRecipe(new ItemStack(blockEasyCraftingTable, 1), new Object[] { Block.workbench, Item.book, Item.redstone });
		// TODO: implement custom recipe items

		Proxy.proxy.onLoad();

		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
	}
}
