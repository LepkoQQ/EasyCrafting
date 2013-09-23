package net.lepko.easycrafting.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.lepko.easycrafting.core.EasyLog;
import net.lepko.easycrafting.core.VersionHelper;
import net.lepko.easycrafting.network.packet.EasyPacket;
import net.lepko.easycrafting.network.packet.PacketEasyCrafting;
import net.lepko.easycrafting.network.packet.PacketInterfaceChange;
import net.lepko.easycrafting.network.packet.PacketServerConfig;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

    public enum PacketTypes {
        PACKETID_EASYCRAFTING(PacketEasyCrafting.class),
        PACKETID_SERVERCONFIG(PacketServerConfig.class),
        PACKETID_INTERFACECHANGE(PacketInterfaceChange.class);

        public final Class<? extends EasyPacket> clazz;

        PacketTypes(Class<? extends EasyPacket> clazz) {
            this.clazz = clazz;
        }

        public static int indexOf(Class<? extends EasyPacket> clazz) {
            for (PacketTypes typ : PacketTypes.values()) {
                if (typ.clazz == clazz) {
                    return typ.ordinal();
                }
            }
            return -1;
        }
    }

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet250, Player player) {
        ByteArrayInputStream bis = new ByteArrayInputStream(packet250.data);
        int id = bis.read();
        DataInputStream dis = new DataInputStream(bis);

        EasyPacket packet = getPacketType(id);
        if (packet != null) {
            packet.read(dis);
            packet.run(player);
        }
    }

    private EasyPacket getPacketType(int id) {
        try {
            return PacketTypes.values()[id].clazz.newInstance();
        } catch (Exception e) {
            EasyLog.warning("Bad packet ID: " + id, e);
            return null;
        }
    }

    public static void sendPacket(EasyPacket packet) {
        Packet250CustomPayload p250 = PacketDispatcher.getPacket(VersionHelper.MOD_ID, packet.getBytes());
        PacketDispatcher.sendPacketToServer(p250);
    }
}
