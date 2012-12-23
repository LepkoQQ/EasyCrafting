package net.lepko.easycrafting.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.helpers.RecipeHelper.RecipeComparator;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraft.entity.player.InventoryPlayer;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.FMLClientHandler;

public class RecipeWorker implements Runnable {

    private ArrayList<EasyRecipe> craftableRecipes = new ArrayList<EasyRecipe>();
    private boolean displayed = true;
    private boolean requested = false;

    private void setCraftableRecipes() {
        long beforeTime = System.nanoTime();

        InventoryPlayer player_inventory = FMLClientHandler.instance().getClient().thePlayer.inventory;

        int maxRecursion = EasyConfig.instance().recipeRecursion.getInt(5);
        ArrayList<EasyRecipe> tmp = RecipeHelper.getCraftableRecipes(player_inventory, maxRecursion, RecipeHelper.getAllRecipes());
        Collections.sort(tmp, new RecipeComparator());

        craftableRecipes = tmp;
        EasyLog.log(String.format("Returning %d craftable out of %d available recipes! ---- Total time: %.8f", craftableRecipes.size(), RecipeHelper.getAllRecipes().size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
    }

    @Override
    public void run() {
        while (true) {
            if (requested) {
                lock.lock();
                try {
                    setCraftableRecipes();
                    requested = false;
                    displayed = false;
                } finally {
                    lock.unlock();
                }
            }

            try {
                Thread.sleep(75L);
            } catch (InterruptedException e) {
            }
        }
    }

    public void requestNewRecipeList() {
        this.requested = true;
    }

    public void setDisplayed() {
        this.displayed = true;
    }

    public ImmutableList<EasyRecipe> getCraftableRecipes() {
        return ImmutableList.copyOf(craftableRecipes);
    }

    public boolean refreshDisplay() {
        return !displayed && !requested;
    }

    // static
    private static RecipeWorker instance;
    private static Thread workerThread;
    public static ReentrantLock lock = new ReentrantLock();

    public static RecipeWorker instance() {
        if (!Proxy.proxy.isClient()) {
            throw new RuntimeException("Worker thread should only be run on client!");
        }
        if (instance == null) {
            instance = new RecipeWorker();
        }
        if (workerThread == null || !workerThread.isAlive()) {
            workerThread = new Thread(instance, "EasyCrafting-WorkerThread");
            workerThread.setDaemon(true);
            workerThread.start();
            EasyLog.log("Started Worker Thread");
        }
        if (!lock.isHeldByCurrentThread()) {
            EasyLog.warning("Trying to access RecipeWorker instance without acquiring a thread lock!");
        }
        if (lock.getHoldCount() > 1) {
            EasyLog.warning("Current thread holds more than one lock!");
        }
        return instance;
    }
}
