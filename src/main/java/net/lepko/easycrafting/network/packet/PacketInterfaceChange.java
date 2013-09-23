package net.lepko.easycrafting.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.lepko.easycrafting.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.inventory.ContainerAutoCrafting;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

public class PacketInterfaceChange extends EasyPacket {

    private int id, value;

    public PacketInterfaceChange() {
    }

    public PacketInterfaceChange(int id, int value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public void run(Player player) {
        EntityPlayer sender = (EntityPlayer) player;
        if (id == 0) {
            if (sender.openContainer instanceof ContainerAutoCrafting) {
                TileEntityAutoCrafting te = ((ContainerAutoCrafting) sender.openContainer).tileEntity;
                te.setMode(value);
            }
        }
    }

    @Override
    protected void readData(DataInputStream data) throws IOException {
        id = data.readByte();
        value = data.readByte();
    }

    @Override
    protected void writeData(DataOutputStream data) throws IOException {
        data.writeByte(id);
        data.writeByte(value);
    }
}
