package net.lepko.easycrafting.core.recipe;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.lepko.easycrafting.core.inventory.gui.GuiEasyCrafting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SideOnly(Side.CLIENT)
public enum RecipeChecker {
    INSTANCE;

    private Minecraft mc = FMLClientHandler.instance().getClient();

    public volatile boolean requested = false;
    public volatile boolean displayed = true;
    public volatile boolean suspend = false;
    public volatile boolean done = false;

    public volatile List<WrappedRecipe> recipes = ImmutableList.of();

    private Thread worker = null;

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && mc.theWorld != null) {
            if (worker == null || !worker.isAlive()) {
                worker = new Thread(new CraftabilityChecker(), Ref.MOD_ID + "-" + CraftabilityChecker.class.getSimpleName());
                worker.setDaemon(true);
                worker.start();
                Ref.LOGGER.info("Worker thread spawned.");
            }
            if (!displayed && !requested && done) {
                if (mc.currentScreen instanceof GuiEasyCrafting) {
                    GuiEasyCrafting gec = (GuiEasyCrafting) mc.currentScreen;
                    gec.refreshCraftingOutput();
                    displayed = true;
                }
            }
        }
    }

    private class CraftabilityChecker implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (requested) {
                    requested = false;
                    displayed = false;
                    done = false;

                    setCraftableRecipes();
                }

                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                }
            }
        }

        private void setCraftableRecipes() {
            InventoryPlayer inventory = FMLClientHandler.instance().getClient().thePlayer.inventory;
            recipes = getCraftableRecipes(inventory, ConfigHandler.MAX_RECURSION, ConfigHandler.MAX_TIME, RecipeManager.getAllRecipes());
            done = true;
        }

        private List<WrappedRecipe> getCraftableRecipes(IInventory inventory, int maxRecursion, long maxTime, List<WrappedRecipe> recipesToCheck) {
            long startTime = System.currentTimeMillis();

            List<WrappedRecipe> craftable = new LinkedList<WrappedRecipe>();
            List<WrappedRecipe> tmpAll = new LinkedList<WrappedRecipe>(recipesToCheck);

            // TODO: if a new request comes before we finish this one, abort and start again
            // TODO: timeout
            // TODO: on gui when you press shift calc all the base ingredients from the inventory you need for all crafting
            // steps not just the last recipe (also color overlay the slots you take from)

            for (WrappedRecipe wr : tmpAll) {
                if (RecipeHelper.canCraft(wr, inventory)) {
                    craftable.add(wr);
                }
            }
            tmpAll.removeAll(craftable);

            if (!craftable.isEmpty()) {
                for (int recursion = 0; recursion < maxRecursion; recursion++) {
                    if (tmpAll.isEmpty()) {
                        break;
                    }

                    List<WrappedRecipe> tmpCraftable = new LinkedList<WrappedRecipe>(craftable);
                    for (WrappedRecipe wr : tmpAll) {
                        if (RecipeHelper.canCraft(wr, inventory, tmpCraftable, maxRecursion)) {
                            craftable.add(wr);
                        }
                    }
                    tmpAll.removeAll(craftable);

                    if (tmpCraftable.size() == craftable.size()) {
                        break;
                    }
                }
            }

            Collections.sort(craftable, WrappedRecipe.Sorter.INSTANCE);

            Ref.LOGGER.info(String.format("%d/%d craftable | %.4f seconds", craftable.size(), recipesToCheck.size(), (System.currentTimeMillis() - startTime) / 1000.0D));

            return craftable;
        }
    }
}
