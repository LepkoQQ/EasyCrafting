package net.lepko.easycrafting.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public enum ConnectionHandler {
    INSTANCE;

    @SubscribeEvent
    public void serverPlayerJoined(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
    }

    @SubscribeEvent
    public void clientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    }
}
