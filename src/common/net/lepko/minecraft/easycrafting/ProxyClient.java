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
	public void sendEasyCraftingPacketToServer(ItemStack[] updatedStacks, int[] slotIndexes, ItemStack inHandStack) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try {
			//TODO: Allow craft table inventory to be used as well
		
			//Start off with send the data for whatever is in the player's hand
			data.writeInt(inHandStack.itemID);
			data.writeInt(inHandStack.getItemDamage());
			data.writeInt(inHandStack.stackSize);

			//We pass over a list of all the slots that were changed, and what the new content of those slots are.
			int count = 0;
			count = updatedStacks.length;

			//Ingredient count
			data.writeInt(count);

			//Data for the ingredients
			for (int i = 0; i < updatedStacks.length; i++) {
				if (updatedStacks[i] != null) {
					//Not an emppty slot, so update with itemstack data
					data.writeInt(updatedStacks[i].itemID);
					data.writeInt(updatedStacks[i].getItemDamage());
					data.writeInt(updatedStacks[i].stackSize);
					data.writeInt(slotIndexes[i]);
				} else {
					//Empty slot, so send empty stack of air
					data.writeInt(0); //ID
					data.writeInt(0); //MetaID
					data.writeInt(0); //Stacksize
					data.writeInt(slotIndexes[i]);
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
