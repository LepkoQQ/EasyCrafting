package net.lepko.minecraft.easycrafting;

import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This class defines a single EasyRecipe.
 */
public class ProxyCommon {

	/** An instance of this class */
	@SidedProxy(clientSide = "net.lepko.minecraft.easycrafting.ProxyClient", serverSide = "net.lepko.minecraft.easycrafting.ProxyCommon")
	public static ProxyCommon proxy;

	/** The texture filepath to fetch the textures from */
	public static String blocksTextureFile = "/net/lepko/minecraft/easycrafting/textures/blocks.png";

	public void registerClientSideSpecific() {
		// Client only
	}

	public void printMessageToChat(String msg) {
		// Client only
	}

	public void sendEasyCraftingPacketToServer(ItemStack is, int slot_index, InventoryPlayer player_inventory, ItemStack inHand, int identifier, EasyRecipe r) {
		// Client only
	}

	/**
	 * Checks if this instance is running clientside.
	 *
	 * @param  N/A
	 * @return 		True if this instance is running clientside, false if not.
	 */
	public boolean isClient() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			return true;
		}
		return false;
	}
}
