package net.lepko.minecraft.easycrafting;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketSender {

	public static void sendEasyCraftingUpdateOutputToClient(Player player) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try {
			data.writeInt(1);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "EasyCrafting";
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;

		PacketDispatcher.sendPacketToPlayer(packet, player);
	}

	@SideOnly(Side.CLIENT)
	public static void sendEasyCraftingPacketToServer(ItemStack is, int slot_index, InventoryPlayer player_inventory, ItemStack inHand) {
		if (is == null) {
			return;
		}

		ArrayList<EasyRecipe> rl = Recipes.getCraftableItems(player_inventory);
		EasyRecipe r = null;
		if (slot_index < rl.size() && rl.get(slot_index) != null) {
			r = rl.get(slot_index);
			if (r.result.itemID == is.itemID && r.result.getItemDamage() == is.getItemDamage()) {
				if (inHand == null && r.result.stackSize == is.stackSize) {
					// System.out.println("----------- RECIPE " + slot_index + " matches!");
				} else if (inHand != null && (inHand.stackSize + r.result.stackSize) == is.stackSize) {
					// System.out.println("----------- RECIPE " + slot_index + " matches! (inHand)");
				} else {
					r = null;
				}
			} else {
				r = null;
			}
		}

		if (r != null) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			try {
				data.writeInt(1); // ID for craft once
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
}
