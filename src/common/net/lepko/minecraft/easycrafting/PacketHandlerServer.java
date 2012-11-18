package net.lepko.minecraft.easycrafting;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.lepko.minecraft.easycrafting.easyobjects.EasyItemStack;
import net.lepko.minecraft.easycrafting.easyobjects.EasyRecipe;
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

		try {
			identifier = data.readInt();

			if (identifier == 1 + 8 || identifier == 2 + 8) {
				identifier -= 8;
				// TODO: constant here and where the packet is sent

				int id = data.readInt();
				int damage = data.readInt();
				int stackSize = data.readInt();

				result = new EasyItemStack(id, damage, stackSize);

				int ingSize = data.readInt();
				int hashCode = data.readInt();

				ItemStack inHand = sender.inventory.getItemStack();
				EasyRecipe recipe = Recipes.getValidRecipe(hashCode);

				if (recipe != null) {
					if ((inHand == null && result.getSize() == recipe.getResult().getSize()) || (inHand != null && (inHand.stackSize + recipe.getResult().getSize()) == result.getSize())) {
						if (identifier == 1) {
							if (Recipes.takeIngredients(recipe, sender.inventory, 0)) {
								ItemStack is = recipe.getResult().toItemStack();
								is.stackSize = result.getSize();
								sender.inventory.setItemStack(is);
							}
						} else if (identifier == 2) {
							int maxTimes = Recipes.calculateCraftingMultiplierUntilMaxStack(recipe.getResult().toItemStack(), inHand);
							int timesCrafted = Recipes.takeIngredientsMaxStack(recipe, sender.inventory, maxTimes, 0);
							if (timesCrafted > 0) {
								int size = result.getSize() + (timesCrafted - 1) * recipe.getResult().getSize();
								ItemStack is = recipe.getResult().toItemStack();
								is.stackSize = size;
								sender.inventory.setItemStack(is);
							}
						}
					}

					System.out.println("Recieved HC: " + recipe.hashCode());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}