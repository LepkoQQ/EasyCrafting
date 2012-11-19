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

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This handles any easycraft packets received by the server from the client.
 * @see http://jd.minecraftforge.net/cpw/mods/fml/common/network/IPacketHandler.html
 */
public class PacketHandlerServer implements IPacketHandler {

	/**
	 * Informs the server of what easyrecipe the user crafted. So that the server can calculate the inventory change.
	 * @see net.lepko.minecraft.easycrafting.ContainerEasyCrafting
	 *
	 * @param  manager				The network manager this packet arrives from.
	 * @param  payload				The actual data of the packet.
	 * @param  receiving_player		Represents the player from which the player originates. Can be used to find the actual player entity.
	 * @return N/A
	 */
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		EntityPlayer sender = (EntityPlayer) player;

		try {
			//Fetch the data for the itemstack in the player's hand
			int id = data.readInt();
			int metaID = data.readInt();
			int stackSize = data.readInt();
			
			ItemStack inHand = new ItemStack(id, stackSize, metaID);
			sender.inventory.setItemStack(inHand);

			//Fetch the data for each changed inventory slot
			int numOfSlotsChanged = data.readInt();
			if(Version.DEBUG) {
				System.out.println("Datapacket, slots: " + numOfSlotsChanged);
			}
			
			int[] changedSlots = new int[numOfSlotsChanged];
			ItemStack[] slotContents = new ItemStack[numOfSlotsChanged];

			for (int i = 0; i < numOfSlotsChanged; i++) {
				int _id = data.readInt();
				int _damage = data.readInt();
				int _stackSize = data.readInt();
				changedSlots[i] = data.readInt();

				//Determine if it is an empty slot or itemstack change.
				if ((_id == 0 && _damage == 0) || (_stackSize == 0)) {
					//Empty slot, make null.
					sender.inventory.setInventorySlotContents(changedSlots[i],null);
				} else {
					slotContents[i] = new ItemStack(_id, _stackSize, _damage);
					//Update the relevant slot in the inventory
					sender.inventory.setInventorySlotContents(changedSlots[i],slotContents[i]);
				}
				if(Version.DEBUG) {
					System.out.println("Datapacket, InvChange: " + changedSlots[i] + " to " + slotContents[i]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}