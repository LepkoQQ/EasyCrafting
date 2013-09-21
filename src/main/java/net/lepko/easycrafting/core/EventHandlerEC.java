package net.lepko.easycrafting.core;

import net.lepko.easycrafting.recipe.RecipeManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandlerEC {

    private boolean isFirstWorldLoad = true;

    @ForgeSubscribe
    public void onWorldLoad(WorldEvent.Load event) {
        if (isFirstWorldLoad) {
            isFirstWorldLoad = false;
            RecipeManager.scanRecipes();
        }
    }
}
