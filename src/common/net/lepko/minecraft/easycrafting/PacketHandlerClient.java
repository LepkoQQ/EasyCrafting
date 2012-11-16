package net.lepko.minecraft.easycrafting;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This handles any easycraft packets received by the client from the server.
 */
public class PacketHandlerClient implements IPacketHandler {

	/**
	 * Currently does nothing.
	 *
	 * @param  manager				Unused; Parent requirement.
	 * @param  payload				Unused; Parent requirement.
	 * @param  receiving_player		Unused; Parent requirement.
	 * @return N/A
	 */
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload payload, Player receiving_player) {
		// DataInputStream data = new DataInputStream(new ByteArrayInputStream(payload.data));
		// EntityPlayer player = (EntityPlayer) receiving_player;
	}
}