package net.lepko.easycrafting.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.lepko.easycrafting.network.packet.EasyPacket;
import net.lepko.easycrafting.network.packet.PacketEasyCrafting;
import net.lepko.easycrafting.network.packet.PacketServerConfig;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

    public static final int PACKETID_EASYCRAFTING = 1;
    public static final int PACKETID_SERVERCONFIG = 2;

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet250, Player player) {
        ByteArrayInputStream bis = new ByteArrayInputStream(packet250.data);
        int id = bis.read();
        DataInputStream dis = new DataInputStream(bis);

        EasyPacket packet = getPacketType(id);

        packet.read(dis);
        packet.run(player);
    }

    private EasyPacket getPacketType(int id) {
        switch (id) {
            case PACKETID_EASYCRAFTING:
                return new PacketEasyCrafting();
            case PACKETID_SERVERCONFIG:
                return new PacketServerConfig();
            default:
                EasyLog.warning("Bad packet ID: " + id);
                return null;
        }
    }

    public static void sendPacket(EasyPacket packet) {
        Packet250CustomPayload p250 = PacketDispatcher.getPacket(VersionHelper.MOD_ID, packet.getBytes());
        PacketDispatcher.sendPacketToServer(p250);
    }
}
