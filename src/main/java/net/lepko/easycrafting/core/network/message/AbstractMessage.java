package net.lepko.easycrafting.core.network.message;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public abstract class AbstractMessage {
    public abstract void write(ByteBuf target);
    public abstract void read(ByteBuf source);
    public abstract void run(EntityPlayer player, Side side);
}
