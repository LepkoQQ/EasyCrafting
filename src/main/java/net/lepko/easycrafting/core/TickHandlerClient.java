package net.lepko.easycrafting.core;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.lepko.easycrafting.inventory.gui.GuiEasyCrafting;
import net.lepko.easycrafting.recipe.RecipeWorker;
import net.minecraft.client.Minecraft;

public class TickHandlerClient {

    private Minecraft mc = FMLClientHandler.instance().getClient();
    private static boolean updateEasyCraftingOutput = false;
    private static boolean showUpdateInChat = true;
    private static int count = 5;

    public static void scheduleRecipeUpdate() {
        scheduleRecipeUpdate(5);
    }

    public static void scheduleRecipeUpdate(int c) {
        updateEasyCraftingOutput = true;
        count = c;
    }

    @SubscribeEvent
    public void tickEnd(TickEvent event) {
        if (updateEasyCraftingOutput && count <= 0 && event.type == TickEvent.Type.CLIENT && event.phase == TickEvent.Phase.END) {
            if (RecipeWorker.lock.tryLock()) {
                try {
                    RecipeWorker.instance().requestNewRecipeList();
                    updateEasyCraftingOutput = false;
                } finally {
                    RecipeWorker.lock.unlock();
                }
            }
            if (showUpdateInChat) {
                VersionHelper.printToChat();
                showUpdateInChat = false;
            }
        } else if (count > 0) {
            count--;
        }

        if (RecipeWorker.lock.tryLock()) {
            try {
                if (mc.theWorld != null && RecipeWorker.instance().refreshDisplay()) {
                    if (mc.currentScreen != null && mc.currentScreen instanceof GuiEasyCrafting) {
                        GuiEasyCrafting gec = (GuiEasyCrafting) mc.currentScreen;
                        gec.refreshCraftingOutput();
                        RecipeWorker.instance().setDisplayed();
                    }
                }
            } finally {
                RecipeWorker.lock.unlock();
            }
        }
    }
}
