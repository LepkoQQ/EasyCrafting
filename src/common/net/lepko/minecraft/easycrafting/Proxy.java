package net.lepko.minecraft.easycrafting;

import net.lepko.minecraft.easycrafting.easyobjects.EasyRecipe;
import net.lepko.minecraft.easycrafting.helpers.EasyLog;
import net.minecraft.src.ItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;

public class Proxy {

	@SidedProxy(clientSide = "net.lepko.minecraft.easycrafting.ProxyClient", serverSide = "net.lepko.minecraft.easycrafting.ProxyCommon")
	public static Proxy proxy;

	public static String blocksTextureFile = "/net/lepko/minecraft/easycrafting/textures/blocks.png";

	public void onLoad() {
		// Client only
	}

	public void printMessageToChat(String msg) {
		// Client only; print to console here
		if (msg != null) {
			EasyLog.log("[CHAT] " + msg);
		}
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
