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

		int identifier;

		ItemStack result;
		ItemStack[] ingredients;

		try {
			identifier = data.readInt();

			if (identifier == 1 || identifier == 2) {
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
						if (identifier == 1) {
							if (Recipes.takeIngredients(ingredients, sender.inventory, 0)) {
								sender.inventory.setItemStack(result);
							}
						} else if (identifier == 2) {
							int maxTimes = Recipes.calculateCraftingMultiplierUntilMaxStack(recipe.result, inHand);
							int timesCrafted = Recipes.takeIngredientsMaxStack(ingredients, sender.inventory, maxTimes, 0);
							if (timesCrafted > 0) {
								result.stackSize += (timesCrafted - 1) * recipe.result.stackSize;
								sender.inventory.setItemStack(result);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}