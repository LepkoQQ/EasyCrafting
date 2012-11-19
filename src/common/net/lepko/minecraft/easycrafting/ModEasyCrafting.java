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
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * The main mod setup class.
 */
@Mod(modid = "Lepko-EasyCrafting", name = Version.MOD_NAME, version = Version.VERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = true, clientPacketHandlerSpec = @SidedPacketHandler(channels = { "EasyCrafting" }, packetHandler = PacketHandlerClient.class), serverPacketHandlerSpec = @SidedPacketHandler(channels = { "EasyCrafting" }, packetHandler = PacketHandlerServer.class))
public class ModEasyCrafting {

	/** The instance of this mod */
	@Instance("Lepko-EasyCrafting")
	public static ModEasyCrafting instance = new ModEasyCrafting();

	// Blocks
	/** The easycraft table block */
	public static Block blockEasyCraftingTable;

	// Gui Handler
	/** The easycraft gui handler */
	private GuiHandler guiHandler = new GuiHandler();

	// Config values
	/** The id for the easycraft table block */
	public int blockEasyCraftingTableID;
	/** Should the recipe for making the easycraft table use redstone? */
	public boolean useRedstoneRecipe;
	/** Should this mod check for updates when loading the world? */
	public boolean checkForUpdates;
	/** How many multistep (intermediate) recipe levels are allowed. */
	public int allowMultiStepRecipes;

	/**
	 * Grabs all the configuration for the mod out of the config file.
	 *
	 * @param  event	The pre-init event. Used to get the config file.
	 * @return N/A
	 */
	@PreInit
	public void preload(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		blockEasyCraftingTableID = config.getBlock("EasyCraftingTable", 404).getInt();
		useRedstoneRecipe = config.get(Configuration.CATEGORY_GENERAL, "useRedstoneRecipe", true).getBoolean(true);
		checkForUpdates = config.get(Configuration.CATEGORY_GENERAL, "checkForUpdates", true).getBoolean(true);
		allowMultiStepRecipes = config.get(Configuration.CATEGORY_GENERAL, "allowMultiStepRecipes", 3).getInt(3);

		config.save();
	}

	/**
	 * Creates all the required instances and does the mod setup.
	 *
	 * @param  event	Unused; Parent requirement.
	 * @return N/A
	 */
	@Init
	public void load(FMLInitializationEvent event) {
		// Update check
		Version.updateCheck();

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
		ProxyCommon.proxy.registerClientSideSpecific();

		// Gui
		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);

		// Register events
		MinecraftForge.EVENT_BUS.register(new EventManager());
	}
}
