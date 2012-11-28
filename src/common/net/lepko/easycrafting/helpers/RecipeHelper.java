package net.lepko.easycrafting.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.lepko.easycrafting.block.GuiEasyCrafting;
import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.proxy.Proxy;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import net.minecraft.src.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class RecipeHelper implements Runnable {

	private static ImmutableList<EasyRecipe> allRecipes;
	private ArrayList<EasyRecipe> craftableRecipes = new ArrayList<EasyRecipe>();
	private boolean displayed = true;
	private boolean requested = false;

	private void setAllRecipes() {
		long beforeTime = System.nanoTime();

		List mcRecipes = CraftingManager.getInstance().getRecipeList();
		ArrayList<EasyRecipe> tmp = new ArrayList<EasyRecipe>();
		int skipped = 0;

		for (int i = 0; i < mcRecipes.size(); i++) {
			IRecipe r = (IRecipe) mcRecipes.get(i);
			ArrayList ingredients = null;
			// TODO: in future versions of forge you don't have to use reflections anymore, fields are exposed
			if (r instanceof ShapedRecipes) {
				ItemStack[] input = ReflectionHelper.<ItemStack[], ShapedRecipes> getPrivateValue(ShapedRecipes.class, (ShapedRecipes) r, 2);
				ingredients = new ArrayList(Arrays.asList(input));
			} else if (r instanceof ShapelessRecipes) {
				List input = ReflectionHelper.<List, ShapelessRecipes> getPrivateValue(ShapelessRecipes.class, (ShapelessRecipes) r, 1);
				ingredients = new ArrayList(input);
			} else if (r instanceof ShapedOreRecipe) {
				Object[] input = ReflectionHelper.<Object[], ShapedOreRecipe> getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe) r, 3);
				ingredients = new ArrayList(Arrays.asList(input));
			} else if (r instanceof ShapelessOreRecipe) {
				List input = ReflectionHelper.<List, ShapelessOreRecipe> getPrivateValue(ShapelessOreRecipe.class, (ShapelessOreRecipe) r, 1);
				ingredients = new ArrayList(input);
			} else {
				String className = r.getClass().getName();
				if (className.equals("ic2.common.AdvRecipe") || className.equals("ic2.common.AdvShapelessRecipe")) {
					try {
						Object[] input = (Object[]) Class.forName(className).getField("input").get(r);
						ingredients = new ArrayList(Arrays.asList(input));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// It's a special recipe (map extending, armor dyeing, ...) - ignore
					// TODO: add IC2 and any other custom recipe classes
					skipped++;
					EasyLog.log(skipped + ": Skipped recipe: " + r);
					continue;
				}
			}
			if (r.getRecipeOutput().toString().contains("item.cart.tank")) {
				skipped++;
				EasyLog.log(skipped + ": Skipped recipe with Tank Cart: " + r.getRecipeOutput());
				continue;
			}
			tmp.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
		}

		Collections.sort(tmp, new Comparator<EasyRecipe>() {
			@Override
			public int compare(EasyRecipe o1, EasyRecipe o2) {
				if (o1.getResult().getID() > o2.getResult().getID()) {
					return 1;
				} else if (o1.getResult().getID() < o2.getResult().getID()) {
					return -1;
				}

				if (o1.getResult().getDamage() > o2.getResult().getDamage()) {
					return 1;
				} else if (o1.getResult().getDamage() < o2.getResult().getDamage()) {
					return -1;
				}

				if (o1.getResult().getSize() > o2.getResult().getSize()) {
					return 1;
				} else if (o1.getResult().getSize() < o2.getResult().getSize()) {
					return -1;
				}
				return 0;
			}
		});

		allRecipes = ImmutableList.copyOf(tmp);
		EasyLog.log(String.format("Returning %d available recipes! ---- Total time: %.8f", allRecipes.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
	}

	private void setCraftableRecipes() {
		long beforeTime = System.nanoTime();

		InventoryPlayer player_inventory = FMLClientHandler.instance().getClient().thePlayer.inventory;

		ArrayList<EasyRecipe> tmp = new ArrayList<EasyRecipe>();
		for (int i = 0; i < allRecipes.size(); i++) {
			if (hasIngredients(allRecipes.get(i), player_inventory, 0)) {
				tmp.add(allRecipes.get(i));
			}
		}

		craftableRecipes = tmp;
		EasyLog.log(String.format("Returning %d craftable out of %d available recipes! ---- Total time: %.8f", craftableRecipes.size(), allRecipes.size(), ((double) (System.nanoTime() - beforeTime) / 1000000000.0D)));
	}

	@Override
	public void run() {
		lock.lock();
		try {
			setAllRecipes();
		} finally {
			lock.unlock();
		}

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

	// Static
	private static RecipeHelper instance;
	private static Thread workerThread;
	public static ReentrantLock lock = new ReentrantLock();

	public static RecipeHelper instance() {
		if (instance == null) {
			if (!Proxy.proxy.isClient()) {
				EasyLog.severe("RecipeHelper instance is only ment to run on a client!");
				throw new RuntimeException("RecipeHelper instance tried to run on a server!");
			}
			instance = new RecipeHelper();
		}
		if (workerThread == null || !workerThread.isAlive()) {
			workerThread = new Thread(instance, "EasyCrafting-WorkerThread");
			workerThread.setDaemon(true);
			workerThread.start();
			EasyLog.log("Started Worker Thread");
		}
		if (!lock.isHeldByCurrentThread()) {
			EasyLog.warning("Trying to access RecipeHelper instance without acquiring a thread lock!");
		}
		if (lock.getHoldCount() > 1) {
			EasyLog.warning("Current thread holds more than one lock!");
		}
		return instance;
	}

	public static boolean hasIngredients(EasyRecipe recipe, InventoryPlayer player_inventory, int recursionCount) {
		return InventoryHelper.checkIngredients(recipe, player_inventory, false, 1, recursionCount) == 0 ? false : true;
	}

	public static boolean takeIngredients(EasyRecipe recipe, InventoryPlayer player_inventory, int recursionCount) {
		return InventoryHelper.checkIngredients(recipe, player_inventory, true, 1, recursionCount) == 0 ? false : true;
	}

	public static int hasIngredientsMaxStack(EasyRecipe recipe, InventoryPlayer player_inventory, int maxTimes, int recursionCount) {
		return InventoryHelper.checkIngredients(recipe, player_inventory, false, maxTimes, recursionCount);
	}

	public static int takeIngredientsMaxStack(EasyRecipe recipe, InventoryPlayer player_inventory, int maxTimes, int recursionCount) {
		return InventoryHelper.checkIngredients(recipe, player_inventory, true, maxTimes, recursionCount);
	}

	public static ImmutableList<EasyRecipe> getAllRecipes() {
		if (allRecipes == null) {
			EasyLog.warning("Tried to get allRecipes before they were set; Returning empty list!");
			return ImmutableList.of();
		} else {
			return allRecipes;
		}
	}

	public static ArrayList<EasyRecipe> getValidRecipes(EasyItemStack result) {
		ArrayList<EasyRecipe> list = new ArrayList<EasyRecipe>();
		ImmutableList<EasyRecipe> all = getAllRecipes();
		for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.getResult().equals(result, true)) {
				list.add(r);
			}
		}
		return list;
	}

	public static ArrayList<EasyRecipe> getValidRecipes(ArrayList<ItemStack> possibleIngredients) {
		ArrayList<EasyRecipe> list = new ArrayList<EasyRecipe>();
		for (int i = 0; i < possibleIngredients.size(); i++) {
			list.addAll(getValidRecipes(EasyItemStack.fromItemStack(possibleIngredients.get(i))));
		}
		return list;
	}

	public static EasyRecipe getValidRecipe(EasyItemStack result, ItemStack[] ingredients) {
		ImmutableList<EasyRecipe> all = getAllRecipes();
		allLoop: for (int i = 0; i < all.size(); i++) {
			EasyRecipe r = all.get(i);
			if (r.getResult().equals(result) && r.getIngredientsSize() == ingredients.length) {
				for (int j = 0; j < r.getIngredientsSize(); j++) {
					if (r.getIngredient(j) instanceof EasyItemStack) {
						EasyItemStack eis = (EasyItemStack) r.getIngredient(j);
						if (!eis.equalsItemStack(ingredients[j])) {
							continue allLoop;
						}
					} else if (r.getIngredient(j) instanceof ArrayList) {
						if (ingredients[j].itemID != -1) {
							continue allLoop;
						}
					}
				}
				return r;
			}
		}
		return null;
	}

	public static int calculateCraftingMultiplierUntilMaxStack(ItemStack result, ItemStack inHand) {
		// TODO: there has to be a better way to calculate this
		int maxTimes = (int) ((double) result.getMaxStackSize() / (double) result.stackSize);
		if (inHand != null) {
			int diff = result.getMaxStackSize() - (maxTimes * result.stackSize);
			if (inHand.stackSize > diff) {
				maxTimes -= (int) (((double) (inHand.stackSize - diff) / (double) result.stackSize) + 1);
			}
		}
		//
		return maxTimes;
	}

	@SideOnly(Side.CLIENT)
	public static EasyRecipe getValidRecipe(GuiEasyCrafting gui, int slot_index, ItemStack result) {
		int recipe_index = slot_index + (gui.currentScroll * 8);
		if (recipe_index >= 0 && gui.renderList != null && recipe_index < gui.renderList.size()) {
			EasyRecipe r = gui.renderList.get(recipe_index);
			if (r.getResult().equalsItemStack(result) && gui.craftableList.contains(r)) {
				return r;
			}
		}
		return null;
	}
}
