package net.lepko.easycrafting.core.network;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.network.message.AbstractMessage;
import net.lepko.easycrafting.core.network.message.MessageEasyCrafting;
import net.lepko.easycrafting.core.network.message.MessageInterfaceChange;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

import java.util.EnumMap;

public final class PacketHandler extends FMLIndexedMessageToMessageCodec<AbstractMessage> {
    public static final PacketHandler INSTANCE = new PacketHandler();
    private static final EnumMap<Side, FMLEmbeddedChannel> CHANNELS = NetworkRegistry.INSTANCE.newChannel(Ref.CHANNEL, INSTANCE, PacketExecutor.INSTANCE);

    private PacketHandler() {}

    public static void init() {
        INSTANCE.addDiscriminator(0, MessageEasyCrafting.class);
        INSTANCE.addDiscriminator(1, MessageInterfaceChange.class);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, AbstractMessage msg, ByteBuf target) throws Exception {
        msg.write(target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, AbstractMessage msg) {
        msg.read(source);
    }

    @Sharable
    private static class PacketExecutor extends SimpleChannelInboundHandler<AbstractMessage> {
        public static final PacketExecutor INSTANCE = new PacketExecutor();

        private PacketExecutor() {}

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
            Side side = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();
            EntityPlayer player;
            if (side.isClient()) {
                player = getClientPlayer();
            } else {
                INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
                player = ((NetHandlerPlayServer) netHandler).playerEntity;
            }
            msg.run(player, side);
        }

        @SideOnly(Side.CLIENT)
        public EntityPlayer getClientPlayer() {
            return Minecraft.getMinecraft().thePlayer;
        }
    }

    public void sendToAll(AbstractMessage message) {
        CHANNELS.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        CHANNELS.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendTo(AbstractMessage message, EntityPlayerMP player) {
        CHANNELS.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        CHANNELS.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        CHANNELS.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendToAllAround(AbstractMessage message, NetworkRegistry.TargetPoint point) {
        CHANNELS.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        CHANNELS.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        CHANNELS.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendToDimension(AbstractMessage message, int dimensionId) {
        CHANNELS.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        CHANNELS.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
        CHANNELS.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendToServer(AbstractMessage message) {
        CHANNELS.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        CHANNELS.get(Side.CLIENT).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}
