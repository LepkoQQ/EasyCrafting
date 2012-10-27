package net.lepko.minecraft.easycrafting;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerClient implements IPacketHandler {
	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload payload, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(payload.data));

		try {
			int i1 = data.readInt();
			if (i1 == 1) {
				EntityPlayer player1 = (EntityPlayer) player;
				if (player1.craftingInventory instanceof ContainerEasyCrafting) {
					ContainerEasyCrafting c = (ContainerEasyCrafting) player1.craftingInventory;
					c.refreshCraftingOutput(player1);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}