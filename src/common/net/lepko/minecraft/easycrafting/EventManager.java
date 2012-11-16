package net.lepko.minecraft.easycrafting;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

/**
 * @author      Lepko <http://lepko.net>
 * 
 * This manages the events, such as forge events.
 */
public class EventManager {

	/**
	 * On loading the world informs the user if an updated version of the mod is available.
	 *
	 * @param  event	The world load event.
	 * @return N/A
	 */
	@ForgeSubscribe
	public void worldLoad(WorldEvent.Load event) {
		if (ProxyCommon.proxy.isClient()) {
			Version.updatePrint();
		}
	}
}
