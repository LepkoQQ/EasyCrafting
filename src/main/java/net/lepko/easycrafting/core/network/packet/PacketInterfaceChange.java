package net.lepko.easycrafting.core.network.packet;

import io.netty.buffer.ByteBuf;
import net.lepko.easycrafting.core.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.core.inventory.ContainerAutoCrafting;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public class PacketInterfaceChange extends EasyPacket {

    private int id, value;

    public PacketInterfaceChange() {
    }

    public PacketInterfaceChange(int id, int value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public void run(EntityPlayer sender) {
        if (id == 0) {
            if (sender.openContainer instanceof ContainerAutoCrafting) {
                TileEntityAutoCrafting te = ((ContainerAutoCrafting) sender.openContainer).tileEntity;
                te.setMode(value);
            }
        }
    }

    @Override
    protected void readData(ByteBuf buf) throws IOException {
        id = buf.readByte();
        value = buf.readByte();
    }

    @Override
    protected void writeData(ByteBuf buf) throws IOException {
        buf.writeByte(id);
        buf.writeByte(value);
    }
}
