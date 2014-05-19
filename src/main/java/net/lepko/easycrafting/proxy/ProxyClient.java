package net.lepko.easycrafting.proxy;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.lepko.easycrafting.block.ModBlocks;
import net.lepko.easycrafting.core.CommandEasyCrafting;
import net.lepko.easycrafting.core.ConnectionHandler;
import net.lepko.easycrafting.core.EasyLog;
import net.lepko.easycrafting.core.TickHandlerClient;
import net.lepko.easycrafting.recipe.RecipeManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.LinkedList;
import java.util.List;

public class ProxyClient extends Proxy {

    @Override
    public void init() {
        // Register Client Tick Handler
        FMLCommonHandler.instance().bus().register(TickHandlerClient.INSTANCE);

        // Register Client Commands
        ClientCommandHandler.instance.registerCommand(new CommandEasyCrafting());
    }

    @Override
    public void printMessageToChat(String msg) {
        if (msg != null) {
            if (FMLClientHandler.instance().getClient().ingameGUI != null) {
                FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(msg));
            } else {
                EasyLog.log("[CHAT] " + msg);
            }
        }
    }

    @Override
    public void replaceRecipe(String itemIDs) {
        ItemStack is = new ItemStack(ModBlocks.table, 1, 0);

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
                array[i] = new ItemStack(Item.getItemById(Integer.parseInt(items[i])), 1, OreDictionary.WILDCARD_VALUE);
            } catch (NumberFormatException nfe) {
                EasyLog.warning("customRecipeItems: '" + itemIDs + "' is not valid; Using default!");
                array = new Object[] { Blocks.crafting_table, Items.book, Items.redstone };
                break;
            }
        }
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.table, 1, 0), array);

        RecipeManager.scanRecipes();
    }
}
