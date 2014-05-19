package net.lepko.easycrafting.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.network.PacketHandler.PacketTypes;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public abstract class EasyPacket {

    public int packetID;

    public EasyPacket() {
        this.packetID = PacketTypes.indexOf(this.getClass());
    }

    public final void read(ByteBuf buf) {
        try {
            readData(buf);
        } catch (IOException e) {
            Ref.LOGGER.warn("Exception while reading packet: " + packetID + "!", e);
        }
    }

    public final ByteBuf getBytes() {
        ByteBuf buf = Unpooled.buffer();

        try {
            buf.writeByte(packetID);
            writeData(buf);
        } catch (IOException e) {
            Ref.LOGGER.warn("Exception while writing to packet: " + packetID + "!", e);
        }

        return buf;
    }

    public abstract void run(EntityPlayer player);

    protected abstract void readData(ByteBuf buf) throws IOException;

    protected abstract void writeData(ByteBuf buf) throws IOException;
}
