package net.lepko.easycrafting.handlers;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerClient implements IPacketHandler {
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload payload, Player receiving_player) {
        // DataInputStream data = new DataInputStream(new ByteArrayInputStream(payload.data));
        // EntityPlayer player = (EntityPlayer) receiving_player;
    }
}