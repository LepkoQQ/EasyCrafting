package net.lepko.easycrafting.handlers;

import net.lepko.easycrafting.helpers.RecipeHelper;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandler {

    private boolean isFirstWorldLoad = true;

    @ForgeSubscribe
    public void onWorldLoad(WorldEvent.Load event) {
        if (isFirstWorldLoad) {
            isFirstWorldLoad = false;
            RecipeHelper.scanRecipes();
            ModCompatibilityHandler.load();
        }
    }
}
