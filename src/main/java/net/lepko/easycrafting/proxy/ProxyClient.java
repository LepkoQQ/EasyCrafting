package net.lepko.easycrafting.proxy;

import java.util.LinkedList;
import java.util.List;

import net.lepko.easycrafting.ModEasyCrafting;
import net.lepko.easycrafting.core.EasyLog;
import net.lepko.easycrafting.core.TickHandlerClient;
import net.lepko.easycrafting.recipe.RecipeManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ProxyClient extends Proxy {

    @Override
    public void onLoad() {
        // Register Client Tick Handler
        TickRegistry.registerTickHandler(new TickHandlerClient(), Side.CLIENT);
    }

    @Override
    public void printMessageToChat(String msg) {
        if (msg != null) {
            if (FMLClientHandler.instance().getClient().ingameGUI != null) {
                FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(msg);
            } else {
                EasyLog.log("[CHAT] " + msg);
            }
        }
    }

    @Override
    public void replaceRecipe(String itemIDs) {
        ItemStack is = new ItemStack(ModEasyCrafting.blockEasyCraftingTable, 1);

        @SuppressWarnings("unchecked")
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        List<IRecipe> toRemove = new LinkedList<IRecipe>();
        for (IRecipe r : recipes) {
            if (is.equals(r.getRecipeOutput())) {
                toRemove.add(r);
            }
        }
        recipes.removeAll(toRemove);

        String[] items = itemIDs.split(",");
        Object[] array = new Object[items.length];
        for (int i = 0; i < items.length; i++) {
            try {
                array[i] = new ItemStack(Integer.parseInt(items[i]), 1, OreDictionary.WILDCARD_VALUE);
            } catch (NumberFormatException nfe) {
                EasyLog.warning("customRecipeItems: '" + itemIDs + "' is not valid; Using default!");
                array = new Object[] { Block.workbench, Item.book, Item.redstone };
                break;
            }
        }
        GameRegistry.addShapelessRecipe(new ItemStack(ModEasyCrafting.blockEasyCraftingTable, 1), array);

        RecipeManager.scanRecipes();
    }
}
