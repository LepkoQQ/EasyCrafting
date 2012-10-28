package net.lepko.minecraft.easycrafting;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;

public class ProxyClient extends ProxyCommon {

	@Override
	public void registerClientSideSpecific() {
		MinecraftForgeClient.preloadTexture(blocksTextureFile);

		// Register Client Tick Handler
		TickRegistry.registerTickHandler(new TickHandlerClient(), Side.CLIENT);
	}

	@Override
	public void printMessageToChat(String msg) {
		if (msg != null) {
			Minecraft mc = FMLClientHandler.instance().getClient();
			if (mc.ingameGUI != null) {
				mc.ingameGUI.getChatGUI().printChatMessage(msg);
			}
		}
	}
}
