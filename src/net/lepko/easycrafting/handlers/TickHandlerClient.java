package net.lepko.easycrafting.handlers;

import java.util.EnumSet;

import net.lepko.easycrafting.helpers.RecipeWorker;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.lepko.easycrafting.inventory.gui.GuiEasyCrafting;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient implements ITickHandler {

    private Minecraft mc = FMLClientHandler.instance().getClient();
    private static boolean updateEasyCraftingOutput = false;
    private static boolean showUpdateInChat = true;
    private static int count = 5;

    public static void updateEasyCraftingOutput() {
        updateEasyCraftingOutput(5);
    }

    public static void updateEasyCraftingOutput(int c) {
        updateEasyCraftingOutput = true;
        count = c;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        if (updateEasyCraftingOutput && count <= 0 && type.equals(EnumSet.of(TickType.CLIENT))) {
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

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel() {
        return VersionHelper.MOD_ID + "-" + this.getClass().getSimpleName();
    }
}
