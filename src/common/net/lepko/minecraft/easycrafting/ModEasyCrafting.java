package net.lepko.minecraft.easycrafting;

import net.lepko.minecraft.easycrafting.block.BlockEasyCraftingTable;
import net.lepko.minecraft.easycrafting.block.TileEntityEasyCrafting;
import net.lepko.minecraft.easycrafting.helpers.EasyConfig;
import net.lepko.minecraft.easycrafting.helpers.VersionHelper;
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
		// Load config values from file
		EasyConfig.loadConfig(event.getSuggestedConfigurationFile());
		// Check for updates
		VersionHelper.performCheck();
	}

	@Init
	public void load(FMLInitializationEvent event) {
		// Add Blocks
		blockEasyCraftingTable = new BlockEasyCraftingTable(EasyConfig.BLOCK_EASY_CRAFTING_ID);
		GameRegistry.registerBlock(blockEasyCraftingTable);
		LanguageRegistry.addName(blockEasyCraftingTable, "Easy Crafting Table");

		// TileEntities
		GameRegistry.registerTileEntity(TileEntityEasyCrafting.class, "tileEntityEasyCrafting");

		// Add recipes
		if (EasyConfig.RECIPE_ITEMS) {
			GameRegistry.addShapelessRecipe(new ItemStack(blockEasyCraftingTable, 1), new Object[] { Block.workbench, Item.book, Item.redstone });
		} else {
			GameRegistry.addShapelessRecipe(new ItemStack(blockEasyCraftingTable, 1), new Object[] { Block.workbench, Item.book });
		}

		// Textures
		Proxy.proxy.registerClientSideSpecific();

		// Gui
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
	}
}
