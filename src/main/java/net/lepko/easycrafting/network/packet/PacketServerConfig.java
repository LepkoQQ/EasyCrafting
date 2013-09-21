package net.lepko.easycrafting.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.core.EasyLog;
import net.lepko.easycrafting.core.VersionHelper;
import net.lepko.easycrafting.network.PacketHandler;
import net.lepko.easycrafting.proxy.Proxy;
import cpw.mods.fml.common.network.Player;

public class PacketServerConfig extends EasyPacket {

    private String modVersionServer;
    private int easyCraftingTableID;
    private String customRecipeString;

    public PacketServerConfig() {
        super(PacketHandler.PACKETID_SERVERCONFIG);
    }

    @Override
    public void run(Player player) {
        if (!modVersionServer.equalsIgnoreCase(VersionHelper.VERSION)) {
            EasyLog.warning("Server is running a different version of the mod than you. Things may break!");
            EasyLog.warning("Server: " + modVersionServer + ", Client: " + VersionHelper.VERSION);
        }
        if (easyCraftingTableID != ConfigHandler.EASYCRAFTINGTABLE_ID) {
            EasyLog.warning("The Block ID for the EasyCraftingTable is different from the server. Change the config to the same value!");
            EasyLog.warning("Server: " + easyCraftingTableID + ", Client: " + ConfigHandler.EASYCRAFTINGTABLE_ID);
        }
        // Recipe from config
        ConfigHandler.CUSTOM_RECIPE_INGREDIENTS = customRecipeString;
        Proxy.proxy.replaceRecipe(customRecipeString);
    }

    @Override
    protected void readData(DataInputStream data) throws IOException {
        EasyLog.log("readData packet PacketServerConfig");

        // Mod version
        modVersionServer = data.readUTF();
        // Easy Crafting Table ID
        easyCraftingTableID = data.readShort();
        // Custom recipe string
        customRecipeString = data.readUTF();
    }

    @Override
    protected void writeData(DataOutputStream data) throws IOException {
        EasyLog.log("writeData packet PacketServerConfig");
        // only send settings that need to be in sync

        // Mod version
        data.writeUTF(VersionHelper.VERSION);
        // Easy Crafting Table ID
        data.writeShort(ConfigHandler.EASYCRAFTINGTABLE_ID);
        // Custom recipe string
        data.writeUTF(ConfigHandler.CUSTOM_RECIPE_INGREDIENTS);
    }
}
