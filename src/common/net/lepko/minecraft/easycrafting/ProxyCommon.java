package net.lepko.minecraft.easycrafting;

import net.lepko.minecraft.easycrafting.easyobjects.EasyRecipe;
import net.minecraft.src.ItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;

public class ProxyCommon {

	@SidedProxy(clientSide = "net.lepko.minecraft.easycrafting.ProxyClient", serverSide = "net.lepko.minecraft.easycrafting.ProxyCommon")
	public static ProxyCommon proxy;

	public static String blocksTextureFile = "/net/lepko/minecraft/easycrafting/textures/blocks.png";

	public void registerClientSideSpecific() {
		// Client only
	}

	public void printMessageToChat(String msg) {
		// Client only
	}

	public void sendEasyCraftingPacketToServer(ItemStack is, int identifier, EasyRecipe r) {
		// Client only
	}

	public boolean isClient() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			return true;
		}
		return false;
	}
}
