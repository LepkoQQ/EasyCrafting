package net.lepko.easycrafting.modcompat;

import ic2.api.item.ElectricItem;
import ic2.api.item.ICustomElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

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

    private static IElectricItemManager getManager(ItemStack is) {
        IElectricItemManager manager = null;
        if (is.getItem() instanceof ISpecialElectricItem) {
            manager = ((ISpecialElectricItem) is.getItem()).getManager(is);
        } else {
            manager = ElectricItem.manager;
        }
        return manager;
    }

    public static int charge(ItemStack is, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!isElectric(is)) {
            return 0;
        }

        // TODO: remove when not supported anymore
        if (is.getItem() instanceof ICustomElectricItem) {
            return ((ICustomElectricItem) is.getItem()).charge(is, amount, tier, ignoreTransferLimit, simulate);
        } else if (!(is.getItem() instanceof ISpecialElectricItem)) {
            ElectricItem.charge(is, amount, tier, ignoreTransferLimit, simulate);
        }

        IElectricItemManager manager = getManager(is);
        if (manager == null) {
            return 0;
        }
        return manager.charge(is, amount, tier, ignoreTransferLimit, simulate);
    }

    public static int discharge(ItemStack is, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!isElectric(is)) {
            return 0;
        }

        // TODO: remove when not supported anymore
        if (is.getItem() instanceof ICustomElectricItem) {
            return ((ICustomElectricItem) is.getItem()).discharge(is, amount, tier, ignoreTransferLimit, simulate);
        } else if (!(is.getItem() instanceof ISpecialElectricItem)) {
            ElectricItem.discharge(is, amount, tier, ignoreTransferLimit, simulate);
        }

        IElectricItemManager manager = getManager(is);
        if (manager == null) {
            return 0;
        }
        return manager.discharge(is, amount, tier, ignoreTransferLimit, simulate);
    }
}
