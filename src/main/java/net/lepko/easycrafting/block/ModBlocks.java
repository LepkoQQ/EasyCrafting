package net.lepko.easycrafting.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class ModBlocks {

    public static Block table;

    public static void setupBlocks() {
        table = new BlockTable();
        GameRegistry.registerBlock(table, ItemBlockTable.class, table.getUnlocalizedName());

        GameRegistry.registerTileEntityWithAlternatives(TileEntityEasyCrafting.class, "EasyCraftingTableTE", "tileEntityEasyCrafting");
        GameRegistry.registerTileEntity(TileEntityAutoCrafting.class, "AutoCraftingTableTE");
    }
}
