package net.lepko.minecraft.easycrafting;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;

public class ProxyClient extends ProxyCommon {

	@Override
	public void registerClientSideSpecific() {
		MinecraftForgeClient.preloadTexture(blocksTextureFile);

		// Register Client Tick Handler
		TickRegistry.registerTickHandler(new TickHandlerClient(), Side.CLIENT);
	}

	@Override
	public void printMessageToChat(String msg) {
		if (msg != null) {
			Minecraft mc = FMLClientHandler.instance().getClient();
			if (mc.ingameGUI != null) {
				mc.ingameGUI.getChatGUI().printChatMessage(msg);
			}
		}
	}

	@Override
	public void sendEasyCraftingPacketToServer(ItemStack is, int slot_index, InventoryPlayer player_inventory, ItemStack inHand, int identifier, EasyRecipe r) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try {
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

			data.writeInt(count);

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
