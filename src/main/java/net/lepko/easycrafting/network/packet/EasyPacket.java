package net.lepko.easycrafting.network.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.lepko.easycrafting.core.EasyLog;
import cpw.mods.fml.common.network.Player;

public abstract class EasyPacket {

    public int packetID;

    public EasyPacket(int packetID) {
        this.packetID = packetID;
    }

    public void read(DataInputStream data) {
        try {
            readData(data);
        } catch (IOException e) {
            EasyLog.warning("Exception while reading packet: " + packetID + "!", e);
        }
    }

    public byte[] getBytes() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            dos.writeByte(packetID);
            writeData(dos);
        } catch (IOException e) {
            EasyLog.warning("Exception while writing to packet: " + packetID + "!", e);
        }

        return bos.toByteArray();
    }

    public abstract void run(Player player);

    protected abstract void readData(DataInputStream data) throws IOException;

    protected abstract void writeData(DataOutputStream data) throws IOException;
}
