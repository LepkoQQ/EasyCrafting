package net.lepko.minecraft.easycrafting;

import net.minecraftforge.client.MinecraftForgeClient;

public class ProxyClient extends ProxyCommon {

	@Override
	public void registerTextures() {
		MinecraftForgeClient.preloadTexture(blocksTextureFile);
	}
}
