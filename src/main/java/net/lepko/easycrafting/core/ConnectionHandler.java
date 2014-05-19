package net.lepko.easycrafting.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class ConnectionHandler {

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EasyLog.warning("PlayerLoggedInEvent RECEIVED");
        // on server
        //TODO: packets
        //        PacketServerConfig packet = new PacketServerConfig();
        //        Packet250CustomPayload p250 = PacketDispatcher.getPacket(VersionHelper.MOD_ID, packet.getBytes());
        //        manager.addToSendQueue(p250);
    }

    @SubscribeEvent
    public void connectionClosed(PlayerEvent.PlayerLoggedOutEvent event) {
        EasyLog.warning("PlayerLoggedOutEvent RECEIVED");

        // on client
        //TODO: packets
        //        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
        //            ConfigHandler.initialize(null);
        //            Proxy.proxy.replaceRecipe(ConfigHandler.CUSTOM_RECIPE_INGREDIENTS);
        //        }
    }
}
