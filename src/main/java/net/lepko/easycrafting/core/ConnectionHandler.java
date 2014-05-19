package net.lepko.easycrafting.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.network.packet.PacketServerConfig;
import net.lepko.easycrafting.proxy.Proxy;

public class ConnectionHandler {

    @SubscribeEvent
    public void playerLoggedIn(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        //TODO: packets
        PacketServerConfig packet = new PacketServerConfig();
        FMLProxyPacket pkt = new FMLProxyPacket(packet.getBytes(), VersionHelper.MOD_ID);
        event.manager.scheduleOutboundPacket(pkt.toS3FPacket());
    }

    @SubscribeEvent
    public void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        //TODO: this is called in "Netty Client IO #0" thread; issue?
        ConfigHandler.initialize(null);
        Proxy.proxy.replaceRecipe(ConfigHandler.CUSTOM_RECIPE_INGREDIENTS);
    }
}
