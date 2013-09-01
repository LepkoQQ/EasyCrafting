package net.lepko.easycrafting.recipe;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraft.entity.player.InventoryPlayer;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.FMLClientHandler;

public class RecipeWorker implements Runnable {

    private List<WrappedRecipe> craftableRecipes = new LinkedList<WrappedRecipe>();
    private boolean displayed = true;
    private boolean requested = false;

    // TODO: if a new request comes before we finish this one, abort and start again
    private void setCraftableRecipes() {
        long beforeTime = System.nanoTime();

        InventoryPlayer player_inventory = FMLClientHandler.instance().getClient().thePlayer.inventory;

        int maxRecursion = ConfigHandler.MAX_RECURSION;
        List<WrappedRecipe> tmp = RecipeHelper.getCraftableRecipes(player_inventory, maxRecursion, RecipeManager.getAllRecipes());

        // TODO: sort the list
        // Collections.sort(tmp, new RecipeComparator());

        craftableRecipes = tmp;
        EasyLog.log(String.format("%d/%d craftable | %.8f seconds", craftableRecipes.size(), RecipeManager.getAllRecipes().size(), (System.nanoTime() - beforeTime) / 1000000000.0D));
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
        requested = true;
    }

    public void setDisplayed() {
        displayed = true;
    }

    public List<WrappedRecipe> getCraftableRecipes() {
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
