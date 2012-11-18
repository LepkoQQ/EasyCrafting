package net.lepko.minecraft.easycrafting.easyobjects;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;

import java.util.List;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class EasyItemStack {

	private int id;
	private int damage;
	private int size;
	private int charge;

	public EasyItemStack(int id, int damage, int size, int charge) {
		this.id = id;
		this.damage = damage;
		this.size = size;
		this.charge = charge;
	}

	public EasyItemStack(int id, int damage, int size) {
		this(id, damage, size, 0);
	}

	public EasyItemStack(int id, int damage) {
		this(id, damage, 1, 0);
	}

	public EasyItemStack(int id) {
		this(id, 0, 1, 0);
	}

	public int getID() {
		return id;
	}

	public int getDamage() {
		return damage;
	}

	public int getSize() {
		return size;
	}

	public int getCharge() {
		return charge;
	}

	public ItemStack toItemStack() {
		ItemStack is = new ItemStack(id, size, damage);
		if (is.getItem() instanceof IElectricItem && charge > 0) {
			ElectricItem.charge(is, charge, 0x7fffffff, true, false);
		}
		return is;
	}

	public static EasyItemStack fromItemStack(ItemStack is) {
		int charge = 0;
		if (is.getItem() instanceof IElectricItem) {
			charge = ElectricItem.discharge(is, 0x7fffffff, 0x7fffffff, true, true);
		}
		return new EasyItemStack(is.itemID, is.getItemDamage(), is.stackSize, charge);
	}

	public boolean canTakeFrom(ItemStack is) {
		if (is == null) {
			return false;
		}
		if (id != is.itemID) {
			return false;
		}
		if (damage != -1 && damage != is.getItemDamage() && !(Item.itemsList[id] instanceof IElectricItem)) {
			return false;
		}
		if (size > is.stackSize) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + damage;
		result = prime * result + size;
		return result;
	}

	@Override
	public String toString() {
		return "EasyItemStack [id=" + id + ", damage=" + damage + ", size=" + size + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, false);
	}

	public boolean equals(Object obj, boolean ignoreSize) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EasyItemStack other = (EasyItemStack) obj;
		if (id != other.id) {
			return false;
		}
		if (damage != other.damage && damage != -1 && other.damage != -1 && !(Item.itemsList[id] instanceof IElectricItem)) {
			return false;
		}
		if (!ignoreSize && size != other.size) {
			return false;
		}
		return true;
	}

	public boolean equalsItemStack(ItemStack is) {
		return equalsItemStack(is, false);
	}

	public boolean equalsItemStack(ItemStack is, boolean ignoreSize) {
		if (is == null) {
			return false;
		}
		if (id != is.itemID) {
			return false;
		}
		if (damage != is.getItemDamage() && damage != -1 && is.getItemDamage() != -1 && !(Item.itemsList[id] instanceof IElectricItem)) {
			return false;
		}
		if (!ignoreSize && size != is.stackSize) {
			return false;
		}
		return true;
	}

	public void setCharge(List<ItemStack> usedIngredients) {
		int outputCharge = 0;

		if (usedIngredients != null) {
			for (int i = 0; i < usedIngredients.size(); i++) {
				ItemStack ingredient = usedIngredients.get(i);
				if (ingredient.getItem() instanceof IElectricItem) {
					outputCharge += ElectricItem.discharge(ingredient, 0x7fffffff, 0x7fffffff, true, true);
				}
			}
		}

		this.charge = outputCharge;
	}
}
