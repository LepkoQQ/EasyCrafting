package net.lepko.easycrafting.core.recipe;

import com.google.common.collect.ImmutableList;

import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.recipe.handler.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;









import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RecipeManager {

	public static final List<IRecipeHandler> HANDLERS = new LinkedList<IRecipeHandler>();

	static {
		// Mod recipe classes could extend vanilla classes so scan them first
		HANDLERS.add(new IC2RecipeHandler());
		HANDLERS.add(new ForestryRecipeHandler());
		HANDLERS.add(new MekanismRecipeHandler());

		// At the end scan vanilla and forge
		HANDLERS.add(new VanillaRecipeHandler());
		HANDLERS.add(new ForgeRecipeHandler());
	}
	private static final List<WrappedRecipe> allRecipes = new ArrayList<WrappedRecipe>();
	private static HashMap<Item, List<WrappedRecipe>> producers=new HashMap(), consumers=new HashMap();
	private static int prevListSize = 0;
	private static IRecipe prevLastElement = null;

	private static boolean shouldScan(List<IRecipe> recipes) {
		if (allRecipes.isEmpty()) {
			return true;
		}
		// XXX: Investigate issues with mods adding recipes too late at some
		// point in the future.
		if (prevListSize != recipes.size()
				|| (!recipes.isEmpty() && prevLastElement != recipes
				.get(recipes.size() - 1))) {
			Ref.LOGGER.warn("|~| A MOD IS ADDING RECIPES TOO LATE |~| Class="
					+ recipes.get(recipes.size() - 1).getClass()
					.getCanonicalName());
			return true;
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	public static void scanRecipes() {
		@SuppressWarnings("unchecked")
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		if (!shouldScan(recipes)) {
			return;
		}
		prevListSize = recipes.size();
		prevLastElement = recipes.get(recipes.size() - 1);

		allRecipes.clear();
		producers.clear();
		consumers.clear();

		long startTime = System.nanoTime();
		int fails = 0;
		HashMap<String,String> recipeExists = new HashMap<String,String>();
		for (IRecipe r : recipes) {
			WrappedRecipe wr = WrappedRecipe.of(r);
			if (wr != null && !CheckIfRecipeAlreadyExists(allRecipes, wr,recipeExists)){
				addItem(wr, wr.getOutput().getItem(), producers);
				for(Object o:wr.inputs){
					if(o instanceof ItemStack){
						addItem(wr, ((ItemStack) o).getItem(), consumers);
					}
					else if(o instanceof List){
						ArrayList<ItemStack> ar = (ArrayList<ItemStack>)o;
						for (ItemStack is : ar)
						{
							addItem(wr, is.getItem(), consumers);
						}
					}
				}
				allRecipes.add(wr);
			} else {
				fails++;
			}
		}

		Collections.sort(allRecipes, WrappedRecipe.Sorter.INSTANCE);

		Ref.LOGGER.info(String.format(
				"Scanned %d recipes (%d failed) in %.8f seconds",
				recipes.size(), fails,
				(System.nanoTime() - startTime) / 1000000000.0D));
	}

	public static List<WrappedRecipe> getAllRecipes() {
		return ImmutableList.copyOf(allRecipes);
	}

	/**
	 * Only use the inventory that is returned from this method if
	 * IRecipe.getCraftingResult() does not check for validity of pattern in
	 * crafting inventory slots.
	 */
	public static InventoryCrafting getCraftingInventory(
			List<ItemStack> usedIngredients) {
		InventoryCrafting ic = new InventoryCrafting(new Container() {
			@Override
			public boolean canInteractWith(EntityPlayer p_75145_1_) {
				return false;
			}

			@Override
			public void onCraftMatrixChanged(IInventory p_75130_1_) {
				// NO-OP
			}
		}, 3, 3);
		for (int i = 0; i < 9; i++) {
			if (i < usedIngredients.size()) {
				ic.setInventorySlotContents(i, usedIngredients.get(i));
			} else {
				ic.setInventorySlotContents(i, null);
			}
		}
		return ic;
	}

	public static boolean CheckIfRecipeAlreadyExists(List<WrappedRecipe> wr, WrappedRecipe newrecipe,HashMap<String,String> recipeExists)
	{
		try {
			if (wr.size() > 0)
			{
				String i = newrecipe.getOutput().toString();
				if (recipeExists.get(i) != null)
				{
					return true;
				}
				recipeExists.put(i, "");
			}
			return false;
		}
		catch (NullPointerException ex)
		{
			return true;
		}
	}
	
	private static void addItem(WrappedRecipe sr, Item out, HashMap<Item, List<WrappedRecipe>> r){
		if(r.containsKey(out)){
			r.get(out).add(sr);
		}else{
			ArrayList<WrappedRecipe> ls=new ArrayList<WrappedRecipe>();
			ls.add(sr);
			r.put(out, ls);
		}
	}
	
	public static List<WrappedRecipe> getProducers(Item i){
		return producers.get(i);
	}
	
	public static List<WrappedRecipe> getConsumers(Item i){
		return consumers.get(i);
	}
	
}
