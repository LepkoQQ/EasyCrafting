package net.lepko.easycrafting.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.core.EasyLog;
import net.lepko.easycrafting.core.VersionHelper;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public class PacketServerConfig extends EasyPacket {

    private String modVersionServer;
    private String customRecipeString;

    @Override
    public void run(EntityPlayer player) {
        if (!modVersionServer.equalsIgnoreCase(VersionHelper.VERSION)) {
            EasyLog.warning("Server is running a different version of the mod than you. Things may break!");
            EasyLog.warning("Server: " + modVersionServer + ", Client: " + VersionHelper.VERSION);
        }
        // Recipe from config
        ConfigHandler.CUSTOM_RECIPE_INGREDIENTS = customRecipeString;
        Proxy.proxy.replaceRecipe(customRecipeString);
    }

    @Override
    protected void readData(ByteBuf buf) throws IOException {
        // Mod version
        modVersionServer = ByteBufUtils.readUTF8String(buf);
        // Custom recipe string
        customRecipeString = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    protected void writeData(ByteBuf buf) throws IOException {
        // only send settings that need to be in sync

        // Mod version
        ByteBufUtils.writeUTF8String(buf, VersionHelper.VERSION);
        // Custom recipe string
        ByteBufUtils.writeUTF8String(buf, ConfigHandler.CUSTOM_RECIPE_INGREDIENTS);
    }
}
