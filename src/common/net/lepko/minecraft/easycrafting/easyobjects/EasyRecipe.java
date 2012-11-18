package net.lepko.minecraft.easycrafting.easyobjects;

import java.util.Collections;
import java.util.List;

import net.minecraft.src.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class EasyRecipe {

	List ingredients;
	EasyItemStack result;

	public EasyRecipe(EasyItemStack result, List ingredients) {
		this.result = result;
		ingredients.removeAll(Collections.singleton(null));
		this.ingredients = ingredients;
	}

	/**
	 * Get the EasyItemStack or a List of ItemStacks for a ingredient at the specified index in the ingredient list.
	 * 
	 * @param index
	 *            Ingredient index in the recipe ingredients list
	 * @return Either an EasyItemStack or a List if valid index; null if index out of bounds or a unrecognizable type.
	 */
	public Object getIngredient(int index) {
		if (index < 0 || index > ingredients.size()) {
			return null;
		}
		Object o = ingredients.get(index);
		if (o instanceof EasyItemStack) {
			return (EasyItemStack) o;
		} else if (o instanceof List) {
			return (List) o;
		} else if (o instanceof ItemStack) {
			EasyItemStack eis = EasyItemStack.fromItemStack((ItemStack) o);
			ingredients.set(index, eis);
			return eis;
		} else if (o instanceof String) {
			List list = OreDictionary.getOres((String) o);
			ingredients.set(index, list);
			return list;
		}
		return null;
	}

	public int getIngredientsSize() {
		return ingredients.size();
	}

	public EasyItemStack getResult() {
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((ingredients == null) ? 0 : ingredients.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EasyRecipe other = (EasyRecipe) obj;
		if (!result.equals(other.result)) {
			return false;
		}
		if (!ingredients.equals(other.ingredients)) {
			return false;
		}
		return true;
	}
}
