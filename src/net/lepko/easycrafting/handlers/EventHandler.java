package net.lepko.easycrafting.handlers;

import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandler {

    private boolean isFirstWorldLoad = true;

    @ForgeSubscribe
    public void onWorldLoad(WorldEvent.Load event) {
        if (isFirstWorldLoad) {
            isFirstWorldLoad = false;
            EasyLog.log("First World Load on side: " + Proxy.proxy.isClient());
            RecipeHelper.scanRecipes();
            ModCompatibilityHandler.load();
        }
    }
}
