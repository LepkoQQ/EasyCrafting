package net.lepko.easycrafting.core;

import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.network.packet.PacketServerConfig;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ConnectionHandler implements IConnectionHandler {

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
        // on server
        PacketServerConfig packet = new PacketServerConfig();
        Packet250CustomPayload p250 = PacketDispatcher.getPacket("EasyCrafting", packet.getBytes());
        manager.addToSendQueue(p250);
    }

    @Override
    public void connectionClosed(INetworkManager manager) {
        // on client
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            ConfigHandler.initialize(null);
            Proxy.proxy.replaceRecipe(ConfigHandler.CUSTOM_RECIPE_INGREDIENTS);
        }
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
        return null;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
    }
}
