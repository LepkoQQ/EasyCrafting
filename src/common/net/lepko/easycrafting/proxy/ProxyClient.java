package net.lepko.easycrafting.proxy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.handlers.TickHandlerClient;
import net.lepko.easycrafting.helpers.EasyLog;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;

public class ProxyClient extends Proxy {

	@Override
	public void onLoad() {
		MinecraftForgeClient.preloadTexture(blocksTextureFile);

		// Register Client Tick Handler
		TickRegistry.registerTickHandler(new TickHandlerClient(), Side.CLIENT);
	}

	@Override
	public void printMessageToChat(String msg) {
		if (msg != null) {
			if (FMLClientHandler.instance().getClient().ingameGUI != null) {
				FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(msg);
			} else {
				EasyLog.log("[CHAT] " + msg);
			}
		}
	}

	@Override
	public void sendEasyCraftingPacketToServer(ItemStack is, int identifier, EasyRecipe r) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try {
			data.writeByte(identifier);
			data.writeShort(is.itemID);
			data.writeInt(is.getItemDamage());
			data.writeByte(is.stackSize);

			data.writeByte(r.getIngredientsSize());

			for (int i = 0; i < r.getIngredientsSize(); i++) {
				if (r.getIngredient(i) instanceof EasyItemStack) {
					EasyItemStack eis = (EasyItemStack) r.getIngredient(i);
					data.writeShort(eis.getID());
					data.writeInt(eis.getDamage());
					data.writeByte(eis.getSize());
				} else if (r.getIngredient(i) instanceof List) {
					// TODO: when updating forge use the new oredict method of obtaining oreID
					data.writeShort(-1);
					data.writeInt(-1);
					data.writeByte(-1);
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
