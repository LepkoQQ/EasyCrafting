package net.lepko.easycrafting.core.recipe;

import com.google.common.primitives.Ints;

import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.recipe.handler.IRecipeHandler;
import net.lepko.easycrafting.core.util.StackUtils;
import net.lepko.easycrafting.core.util.WrappedStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WrappedRecipe {
	
	public final IRecipe recipe;
	public final List<Object> inputs;
	public final List<WrappedStack> collatedInputs;
	public final WrappedStack output;
	public final IRecipeHandler handler;
	public final List<ItemStack> usedIngredients;

	private WrappedRecipe(IRecipe recipe, List<Object> inputs, WrappedStack output, IRecipeHandler handler) {
		this.recipe = recipe;
		this.inputs = inputs;
		this.collatedInputs = StackUtils.collateStacks(inputs);
		this.output = output;
		this.handler = handler;
		this.usedIngredients = new ArrayList<ItemStack>(9);
	}
	
	public ItemStack getOutput() {
		return StackUtils.copyStack(output.stacks.get(0), output.size);
	}

	/**
	 * If recipe is valid, adds the recipe to the list and returns the WrappedRecipe instance.
	 */
	public static WrappedRecipe of(IRecipe recipe) {
		if (recipe == null) {
			warn("recipe is null");
			return null;
		}
		if (recipe.getRecipeOutput() == null) {
			warn("recipe output is null", recipe);
			return null;
		}
		if (recipe.getRecipeOutput().getItem() == null) {
			warn("recipe output item is null [id=" + recipe.getRecipeOutput().getItem() + "]", recipe);
			return null;
		}
		List<Object> inputs = null;
		IRecipeHandler handler = null;
		for (IRecipeHandler h : RecipeManager.HANDLERS) {
			inputs = h.getInputs(recipe);
			if (inputs != null) {
				handler = h;
				break;
			}
		}

		if (inputs == null) {
			warn("failed to get recipe input list", recipe);
			return null;
		}
		inputs.removeAll(Collections.singleton(null));
		if (inputs.isEmpty()) {
			warn("recipe input list is empty", recipe);
			return null;
		}
		for (Object o : inputs) {
			if (o instanceof List) {
				List<?> list = (List<?>) o;
				if (list.isEmpty()) {
					warn("one of recipe inputs is an empty list", recipe);
					return null;
				} else {
					for (Object p : list) {
						if (p instanceof ItemStack) {
							ItemStack stack = (ItemStack) p;
							if (stack.getItem() == null) {
								warn("one of recipe input items is null [id=" + stack.getItem() + "]", recipe);
								return null;
							}
							stack.stackSize = 1;
						} else {
							warn("one of recipe inputs is unknown", o, recipe);
							return null;
						}
					}
				}
			} else {
				if (o instanceof ItemStack) {
					ItemStack stack = (ItemStack) o;
					if (stack.getItem() == null) {
						warn("one of recipe input items is null [id=" + stack.getItem() + "]", recipe);
						return null;
					}
					stack.stackSize = 1;
				} else {
					warn("one of recipe inputs is unknown", o, recipe);
					return null;
				}
			}
		}
		return new WrappedRecipe(recipe, inputs, new WrappedStack(recipe.getRecipeOutput()), handler);
	}

	private static void warn(String msg, Object... objs) {
		String s = "[WrappedRecipe] " + msg;
		for (Object o : objs) {
			s += " (" + o.getClass().getCanonicalName() + ")";
		}
		Ref.LOGGER.warn(s);
	}

	public static enum Sorter implements Comparator<WrappedRecipe> {
		INSTANCE;

		@Override
		public int compare(WrappedRecipe first, WrappedRecipe second) {
			ItemStack is1 = first.getOutput();
			ItemStack is2 = second.getOutput();
			int compareName = is1.getItem().getUnlocalizedName(is1).compareTo(is2.getItem().getUnlocalizedName(is2));
			if (compareName != 0) {
				return compareName;
			} else {
				int compareDamage = Ints.compare(is1.getItemDamage(), is2.getItemDamage());
				if (compareDamage != 0) {
					return compareDamage;
				} else {
					return Ints.compare(is1.stackSize, is2.stackSize) * -1;
				}
			}
		}
	}
	
	public String toString(){
		return collatedInputs + "->" + output; 
	}
	
}
