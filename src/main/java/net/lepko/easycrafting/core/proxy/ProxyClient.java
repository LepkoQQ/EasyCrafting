package net.lepko.easycrafting.core.proxy;

import net.lepko.easycrafting.core.CommandEasyCrafting;
import net.lepko.easycrafting.core.block.ModBlocks;
import net.lepko.easycrafting.core.recipe.RecipeChecker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.ClientCommandHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProxyClient extends Proxy {

	@Override
	public void registerHandlers() {
		super.registerHandlers();

		FMLCommonHandler.instance().bus().register(RecipeChecker.INSTANCE);
	}

	@Override
	public void registerCommands() {
		super.registerCommands();

		ClientCommandHandler.instance
				.registerCommand(new CommandEasyCrafting());
	}
}
