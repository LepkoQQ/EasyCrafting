package net.lepko.easycrafting;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.lepko.easycrafting.core.block.ModBlocks;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.lepko.easycrafting.core.config.KeyBindings;
import net.lepko.easycrafting.core.network.PacketHandler;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.util.ItemMap;

@Mod(modid = Ref.MOD_ID, useMetadata = true)
public class EasyCrafting {

	// TODO: block.shouldCheckWeakPower()
	// block.onNeighbourTileChange()
	// CREDIT ElementalRobot50 for Auto Crafting Table textures

	@Instance(Ref.MOD_ID)
	public static EasyCrafting INSTANCE;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (event.getSide() == Side.CLIENT)
			KeyBindings.init();
		Ref.init();
		ConfigHandler.initialize(event.getSuggestedConfigurationFile());

		ModBlocks.setupBlocks();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Ref.PROXY.registerHandlers();
		Ref.PROXY.registerCommands();

		PacketHandler.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ModBlocks.setupRecipes();
		// XXX: VersionHelper.performCheck();
	}

	@EventHandler
	public void available(FMLLoadCompleteEvent event) {
		ItemMap.build();

		// This fires after the recipes are sorted by forge; Mods should not
		// add/remove recipes after this point!!
		RecipeManager.scanRecipes();
	}
}
