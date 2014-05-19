package net.lepko.easycrafting.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.lepko.easycrafting.recipe.RecipeManager;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandlerEC {

    private boolean isFirstWorldLoad = true;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (isFirstWorldLoad) {
            isFirstWorldLoad = false;
            RecipeManager.scanRecipes();
        }
    }
}
