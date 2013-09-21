package net.lepko.easycrafting.block;

import net.lepko.easycrafting.config.ConfigHandler;
import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;

public class Blocks {

    public static Block table;

    public static void setupBlocks() {
        table = new BlockTable(ConfigHandler.EASYCRAFTINGTABLE_ID);
        GameRegistry.registerBlock(table, ItemBlockTable.class, table.getUnlocalizedName());

        GameRegistry.registerTileEntityWithAlternatives(TileEntityEasyCrafting.class, "EasyCraftingTableTE", "tileEntityEasyCrafting");
        GameRegistry.registerTileEntity(TileEntityAutoCrafting.class, "AutoCraftingTableTE");
    }
}
