package net.lepko.easycrafting.core.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.network.packet.EasyPacket;
import net.lepko.easycrafting.core.network.packet.PacketEasyCrafting;
import net.lepko.easycrafting.core.network.packet.PacketInterfaceChange;
import net.minecraft.network.NetHandlerPlayServer;

public class PacketHandler {

    public static PacketHandler INSTANCE = new PacketHandler();
    public static FMLEventChannel CHANNEL = NetworkRegistry.INSTANCE.newEventDrivenChannel(Ref.CHANNEL);

    public static void init() {
        CHANNEL.register(INSTANCE);
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        ByteBuf buf = event.packet.payload();
        int id = buf.readByte();

        EasyPacket packet = getPacketType(id);
        if (packet != null) {
            packet.read(buf);
            packet.run(((NetHandlerPlayServer) event.handler).playerEntity);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        ByteBuf buf = event.packet.payload();
        int id = buf.readByte();

        EasyPacket packet = getPacketType(id);
        if (packet != null) {
            packet.read(buf);
            packet.run(FMLClientHandler.instance().getClient().thePlayer);
        }
    }

    public enum PacketTypes {
        PACKETID_EASYCRAFTING(PacketEasyCrafting.class),
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

    private EasyPacket getPacketType(int id) {
        try {
            return PacketTypes.values()[id].clazz.newInstance();
        } catch (Exception e) {
            Ref.LOGGER.warn("Bad packet ID: " + id, e);
            return null;
        }
    }

    public static void sendPacket(EasyPacket packet) {
        CHANNEL.sendToServer(new FMLProxyPacket(packet.getBytes(), Ref.CHANNEL));
    }
}
