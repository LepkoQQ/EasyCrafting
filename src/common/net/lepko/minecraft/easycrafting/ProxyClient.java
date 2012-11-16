package net.lepko.minecraft.easycrafting;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This is the proxy class for the slient side.
 */
public class ProxyClient extends ProxyCommon {

	/**
	 * Registers the texture and tickhandler for the clientside.
	 *
	 * @param  N/A
	 * @return N/A
	 */
	@Override
	public void registerClientSideSpecific() {
		MinecraftForgeClient.preloadTexture(blocksTextureFile);

		// Register Client Tick Handler
		TickRegistry.registerTickHandler(new TickHandlerClient(), Side.CLIENT);
	}

	/**
	 * Prints the specified message to the user's chat window.
	 *
	 * @param  msg	The string to print to the user's chat window.
	 * @return N/A
	 */
	@Override
	public void printMessageToChat(String msg) {
		if (msg != null) {
			FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(msg);
		}
	}

	/**
	 * Sends data to the server when an easycraft recipe is crafted.
	 *
	 * So that the server may also execute the recipe and update the inventory appropriately.
	 *
	 * @param  is					The itemstack result from the crafted recipe.
	 * @param  slot_index			Unused.
	 * @param  player_inventory		Unused.
	 * @param  inHand				Unused.
	 * @param  identifier			What mouse action (left- or right-click) was issued.
	 * @param  r					The easycraft recipe that was crafted.
	 * @return N/A
	 */
	@Override
	public void sendEasyCraftingPacketToServer(ItemStack is, int slot_index, InventoryPlayer player_inventory, ItemStack inHand, int identifier, EasyRecipe r) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try {
			//Data for the recipe that was made
			data.writeInt(identifier);
			data.writeInt(is.itemID);
			data.writeInt(is.getItemDamage());
			data.writeInt(is.stackSize);

			int count = 0;
			for (int j = 0; j < r.ingredients.length; j++) {
				if (r.ingredients[j] != null) {
					count++;
				}
			}

			//Ingredient count
			data.writeInt(count);

			//Data for the ingredients
			for (int i = 0; i < r.ingredients.length; i++) {
				if (r.ingredients[i] != null) {
					data.writeInt(r.ingredients[i].itemID);
					data.writeInt(r.ingredients[i].getItemDamage());
					data.writeInt(r.ingredients[i].stackSize);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "EasyCrafting";
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;
		FMLClientHandler.instance().sendPacket(packet);
	}
}
