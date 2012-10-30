package net.lepko.minecraft.easycrafting;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerServer implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		EntityPlayer sender = (EntityPlayer) player;

		int i1;

		ItemStack result;
		ItemStack[] ingredients;

		try {
			i1 = data.readInt(); // identifier

			if (i1 == 1) {

				int id = data.readInt();
				int damage = data.readInt();
				int stackSize = data.readInt();

				result = new ItemStack(id, stackSize, damage);

				int ingSize = data.readInt();

				ingredients = new ItemStack[ingSize];

				for (int i = 0; i < ingSize; i++) {
					int _id = data.readInt();
					int _damage = data.readInt();
					int _stackSize = data.readInt();

					ingredients[i] = new ItemStack(_id, _stackSize, _damage);
				}

				ItemStack inHand = sender.inventory.getItemStack();
				EasyRecipe recipe = Recipes.getValidRecipe(result, ingredients);

				if (recipe != null) {
					if ((inHand == null && result.stackSize == recipe.result.stackSize) || (inHand != null && (inHand.stackSize + recipe.result.stackSize) == result.stackSize)) {
						if (Recipes.takeIngredients(ingredients, sender.inventory)) {
							sender.inventory.setItemStack(result);
						}
					}
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		PacketSender.sendEasyCraftingUpdateOutputToClient(player);
	}
}