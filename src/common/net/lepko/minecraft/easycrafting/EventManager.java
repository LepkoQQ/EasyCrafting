package net.lepko.minecraft.easycrafting;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

public class EventManager {

	@ForgeSubscribe
	public void worldLoad(WorldEvent.Load event) {
		if (ProxyCommon.proxy.isClient()) {
			Version.updatePrint();
		}
	}
}
