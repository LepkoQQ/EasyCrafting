package net.lepko.easycrafting.core;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.lepko.easycrafting.core.inventory.gui.GuiEasyCrafting;
import net.lepko.easycrafting.core.recipe.RecipeWorker;
import net.minecraft.client.Minecraft;

@SideOnly(Side.CLIENT)
public enum TickHandlerClient {
    INSTANCE;

    private Minecraft mc = FMLClientHandler.instance().getClient();
    private boolean updateEasyCraftingOutput = false;
    private int count = 5;

    public void scheduleRecipeUpdate() {
        scheduleRecipeUpdate(5);
    }

    public void scheduleRecipeUpdate(int c) {
        updateEasyCraftingOutput = true;
        count = c;
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (updateEasyCraftingOutput && count <= 0) {
                if (RecipeWorker.lock.tryLock()) {
                    try {
                        RecipeWorker.instance().requestNewRecipeList();
                        updateEasyCraftingOutput = false;
                    } finally {
                        RecipeWorker.lock.unlock();
                    }
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
}
