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
			data.writeInt(identifier);
			data.writeInt(is.itemID);
			data.writeInt(is.getItemDamage());
			data.writeInt(is.stackSize);

			data.writeInt(r.getIngredientsSize());

			for (int i = 0; i < r.getIngredientsSize(); i++) {
				if (r.getIngredient(i) instanceof EasyItemStack) {
					EasyItemStack eis = (EasyItemStack) r.getIngredient(i);
					data.writeInt(eis.getID());
					data.writeInt(eis.getDamage());
					data.writeInt(eis.getSize());
				} else if (r.getIngredient(i) instanceof List) {
					data.writeInt(-1);
					data.writeInt(-1);
					data.writeInt(-1);
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
