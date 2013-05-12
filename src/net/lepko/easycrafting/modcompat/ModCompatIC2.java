package net.lepko.easycrafting.modcompat;

import ic2.api.item.ElectricItem;
import ic2.api.item.ICustomElectricItem;
import ic2.api.item.IElectricItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.helpers.EasyLog;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class ModCompatIC2 extends ModCompat {

    public ModCompatIC2() {
        super("IC2");
    }

    @Override
    public void scanRecipes(List<IRecipe> recipes) {
        try {
            //
            Iterator<IRecipe> iterator = recipes.iterator();
            while (iterator.hasNext()) {
                IRecipe r = iterator.next();
                String className = r.getClass().getName();
                if (className.equals("ic2.core.AdvRecipe") || className.equals("ic2.core.AdvShapelessRecipe")) {
                    Object[] input = (Object[]) Class.forName(className).getField("input").get(r);
                    ArrayList<Object> ingredients = new ArrayList<Object>(Arrays.asList(input));
                    RecipeHelper.scannedRecipes.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
                    iterator.remove();
                }
            }
            //
        } catch (Exception e) {
            EasyLog.warning("[ModCompat] [" + modID + "] Exception while scanning recipes.", e);
            return;
        }
    }

    public static boolean isElectric(ItemStack is) {
        if (ModCompat.isLoaded("IC2") && is.getItem() instanceof IElectricItem) {
            return true;
        }
        return false;
    }

    public static int charge(ItemStack is, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!isElectric(is)) {
            return 0;
        }
        if (is.getItem() instanceof ICustomElectricItem) {
            return ((ICustomElectricItem) is.getItem()).charge(is, amount, tier, ignoreTransferLimit, simulate);
        } else {
            return ElectricItem.charge(is, amount, tier, ignoreTransferLimit, simulate);
        }
    }

    public static int discharge(ItemStack is, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!isElectric(is)) {
            return 0;
        }
        if (is.getItem() instanceof ICustomElectricItem) {
            return ((ICustomElectricItem) is.getItem()).discharge(is, amount, tier, ignoreTransferLimit, simulate);
        } else {
            return ElectricItem.discharge(is, amount, tier, ignoreTransferLimit, simulate);
        }
    }
}
