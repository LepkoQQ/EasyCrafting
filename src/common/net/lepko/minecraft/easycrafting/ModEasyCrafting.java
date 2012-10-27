package net.lepko.minecraft.easycrafting;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "Lepko-EasyCrafting", name = "Easy Crafting", version = "1.0.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, clientPacketHandlerSpec = @SidedPacketHandler(channels = { "EasyCrafting" }, packetHandler = PacketHandlerClient.class), serverPacketHandlerSpec = @SidedPacketHandler(channels = { "EasyCrafting" }, packetHandler = PacketHandlerServer.class))
public class ModEasyCrafting {

	@Instance
	public static ModEasyCrafting instance = new ModEasyCrafting();

	// Blocks
	public static Block blockEasyCraftingTable;

	// Gui Handler
	private GuiHandler guiHandler = new GuiHandler();

	// Config values
	public int blockEasyCraftingTableID;
	public boolean useRedstoneRecipe;
	public boolean allowBlockVariations;
	// TODO: implement block variations (like all color wood etc.)

	@SidedProxy(clientSide = "net.lepko.minecraft.easycrafting.ProxyClient", serverSide = "net.lepko.minecraft.easycrafting.ProxyCommon")
	public static ProxyCommon proxy;

	@PreInit
	public void preload(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		blockEasyCraftingTableID = config.getBlock("EasyCraftingTable", 404).getInt();
		useRedstoneRecipe = config.get("options", "useRedstoneRecipe", true).getBoolean(true);
		allowBlockVariations = config.get("options", "allowBlockVariations", false).getBoolean(false);

		config.save();
	}

	@Init
	public void load(FMLInitializationEvent event) {

		// Add Blocks
		blockEasyCraftingTable = new BlockEasyCraftingTable(blockEasyCraftingTableID);
		GameRegistry.registerBlock(blockEasyCraftingTable);
		LanguageRegistry.addName(blockEasyCraftingTable, "Easy Crafting Table");

		// TileEntities
		GameRegistry.registerTileEntity(TileEntityEasyCrafting.class, "tileEntityEasyCrafting");

		// Add recipes
		if (useRedstoneRecipe) {
			GameRegistry.addShapelessRecipe(new ItemStack(blockEasyCraftingTable, 1), new Object[] { Block.workbench, Item.book, Item.redstone });
		} else {
			GameRegistry.addShapelessRecipe(new ItemStack(blockEasyCraftingTable, 1), new Object[] { Block.workbench, Item.book });
		}

		// Textures
		proxy.registerClientSideSpecific();

		// Gui
		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);

		// Registe events
		MinecraftForge.EVENT_BUS.register(new EventManager());
	}
}
