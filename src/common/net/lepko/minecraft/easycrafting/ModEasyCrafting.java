package net.lepko.minecraft.easycrafting;

import net.lepko.minecraft.easycrafting.block.BlockEasyCraftingTable;
import net.lepko.minecraft.easycrafting.block.TileEntityEasyCrafting;
import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import net.minecraftforge.oredict.ShapelessOreRecipe;
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

@Mod(modid = "Lepko-EasyCrafting", name = Version.MOD_NAME, version = Version.VERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = true, clientPacketHandlerSpec = @SidedPacketHandler(channels = { "EasyCrafting" }, packetHandler = PacketHandlerClient.class), serverPacketHandlerSpec = @SidedPacketHandler(channels = { "EasyCrafting" }, packetHandler = PacketHandlerServer.class))
public class ModEasyCrafting {

	@Instance("Lepko-EasyCrafting")
	public static ModEasyCrafting instance = new ModEasyCrafting();

	// Blocks
	public static Block blockEasyCraftingTable;

	// Gui Handler
	private GuiHandler guiHandler = new GuiHandler();

	// Config values
	public int blockEasyCraftingTableID;
	public boolean useRedstoneRecipe;
	public boolean checkForUpdates;
	public int allowMultiStepRecipes;

	//

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

		OreDictionary.registerOre("woodLog", new ItemStack(blockEasyCraftingTable));
		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(Item.bucketEmpty, new Object[] { Block.dirt, "woodLog" }));

		// Textures
		ProxyCommon.proxy.registerClientSideSpecific();

		// Gui
		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);

		// Register events
		MinecraftForge.EVENT_BUS.register(new EventManager());
	}
}
