package net.lepko.easycrafting.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.helpers.RecipeHelper;
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

		EasyItemStack result;
		ItemStack[] ingredients;

		try {
			identifier = data.readByte();

			if (identifier == 1 || identifier == 2) {

				int id = data.readShort();
				int damage = data.readInt();
				int stackSize = data.readByte();

				ItemStack inHand = sender.inventory.getItemStack();
				if (inHand != null && inHand.itemID == id && inHand.getItemDamage() == damage && inHand.stackSize < stackSize) {
					stackSize -= inHand.stackSize;
				}

				result = new EasyItemStack(id, damage, stackSize);

				int ingSize = data.readByte();

				ingredients = new ItemStack[ingSize];
				for (int i = 0; i < ingSize; i++) {
					int _id = data.readShort();
					int _damage = data.readInt();
					int _stackSize = data.readByte();

					ingredients[i] = new ItemStack(_id, _stackSize, _damage);
				}

				EasyRecipe recipe = RecipeHelper.getValidRecipe(result, ingredients);

				//TODO: fix inhand detect
				if (recipe != null) {
					if ((inHand == null && result.getSize() == recipe.getResult().getSize()) || (inHand != null && (inHand.stackSize + recipe.getResult().getSize()) == result.getSize())) {
						if (identifier == 1) {
							if (RecipeHelper.takeIngredients(recipe, sender.inventory, 0)) {
								ItemStack is = recipe.getResult().toItemStack();
								if (inHand != null) {
									is.stackSize += inHand.stackSize;
								}
								sender.inventory.setItemStack(is);
							}
						} else if (identifier == 2) {
							int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(recipe.getResult().toItemStack(), inHand);
							int timesCrafted = RecipeHelper.takeIngredientsMaxStack(recipe, sender.inventory, maxTimes, 0);
							if (timesCrafted > 0) {
								int size = result.getSize() + (timesCrafted - 1) * recipe.getResult().getSize();
								ItemStack is = recipe.getResult().toItemStack();
								is.stackSize = size;
								sender.inventory.setItemStack(is);
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