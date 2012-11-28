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

		try {
			int identifier = data.readByte();

			if (identifier == 1 || identifier == 2) {

				int id = data.readShort();
				int damage = data.readInt();
				int stackSize = data.readByte();

				EasyItemStack result = new EasyItemStack(id, damage, stackSize);

				int ingSize = data.readByte();

				ItemStack[] ingredients = new ItemStack[ingSize];
				for (int i = 0; i < ingSize; i++) {
					int _id = data.readShort();
					int _damage = data.readInt();
					int _stackSize = data.readByte();

					ingredients[i] = new ItemStack(_id, _stackSize, _damage);
				}

				EasyRecipe recipe = RecipeHelper.getValidRecipe(result, ingredients);

				if (recipe != null) {
					ItemStack stack_in_hand = sender.inventory.getItemStack();
					ItemStack return_stack = null;
					int return_size = 0;

					if (stack_in_hand == null) {
						return_stack = recipe.getResult().toItemStack();
						return_size = recipe.getResult().getSize();
					} else if (recipe.getResult().equalsItemStack(stack_in_hand, true) && stack_in_hand.getMaxStackSize() >= (recipe.getResult().getSize() + stack_in_hand.stackSize) && EasyItemStack.areStackTagsEqual(recipe.getResult(), stack_in_hand)) {
						return_stack = recipe.getResult().toItemStack();
						return_size = recipe.getResult().getSize() + stack_in_hand.stackSize;
					}

					if (return_stack != null) {
						if (identifier == 1) {
							if (RecipeHelper.takeIngredients(recipe, sender.inventory, 0)) {
								return_stack.stackSize = return_size;
								sender.inventory.setItemStack(return_stack);
							}
						} else if (identifier == 2) {
							int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(return_stack, stack_in_hand);
							int timesCrafted = RecipeHelper.takeIngredientsMaxStack(recipe, sender.inventory, maxTimes, 0);
							if (timesCrafted > 0) {
								return_stack.stackSize = return_size + (timesCrafted - 1) * recipe.getResult().getSize();
								sender.inventory.setItemStack(return_stack);
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