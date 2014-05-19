package net.lepko.easycrafting.core.network.message;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.core.inventory.ContainerAutoCrafting;
import net.minecraft.entity.player.EntityPlayer;

public class MessageInterfaceChange extends AbstractMessage {

    private int id, value;

    public MessageInterfaceChange() {}

    public MessageInterfaceChange(int id, int value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public void write(ByteBuf target) {
        target.writeByte(id);
        target.writeByte(value);
    }

    @Override
    public void read(ByteBuf source) {
        id = source.readByte();
        value = source.readByte();
    }

    @Override
    public void run(EntityPlayer player, Side side) {
        Ref.LOGGER.info("Message: " + this.getClass().getName() + " Side: " + side);

        if (id == 0) {
            if (player.openContainer instanceof ContainerAutoCrafting) {
                TileEntityAutoCrafting te = ((ContainerAutoCrafting) player.openContainer).tileEntity;
                te.setMode(value);
            }
        }
    }
}
