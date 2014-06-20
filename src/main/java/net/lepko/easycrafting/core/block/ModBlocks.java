package net.lepko.easycrafting.core.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.lepko.easycrafting.Ref;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

@GameRegistry.ObjectHolder(Ref.MOD_ID)
public class ModBlocks {

    public static final Block table = new BlockTable();

    public static void setupBlocks() {
        GameRegistry.registerBlock(table, ItemBlockTable.class, "table");
        GameRegistry.registerCustomItemStack("easyCraftingTable", new ItemStack(table, 1, 0));
        GameRegistry.registerCustomItemStack("autoCraftingTable", new ItemStack(table, 1, 1));

        GameRegistry.registerTileEntity(TileEntityEasyCrafting.class, "EasyCraftingTableTE");
        GameRegistry.registerTileEntity(TileEntityAutoCrafting.class, "AutoCraftingTableTE");
    }

    public static void setupRecipes() {
        GameRegistry.addShapelessRecipe(get("easyCraftingTable"), Blocks.crafting_table, Items.redstone, Items.book);
        GameRegistry.addShapedRecipe(get("autoCraftingTable"), "rsr", "scs", "rsr", 'r', Items.redstone, 's', Blocks.stone, 'c', Blocks.crafting_table);

        //TODO: remove after done with testing
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.blaze_powder, 32, 0), "x", "y", 'x', "record", 'y', "blockGlass"));
    }

    private static ItemStack get(String name) {
        return GameRegistry.findItemStack(Ref.MOD_ID, name, 1);
    }
}
