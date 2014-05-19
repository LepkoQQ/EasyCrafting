package net.lepko.easycrafting.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.minecraftforge.event.world.WorldEvent;

public enum WorldLoadHandler {
    INSTANCE;

    private boolean isFirstWorldLoad = true;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        //TODO: maybe use the connection events now?
        if (isFirstWorldLoad) {
            isFirstWorldLoad = false;
            RecipeManager.scanRecipes();
        }
    }
}
