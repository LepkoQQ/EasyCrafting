package net.lepko.easycrafting.block;

import net.lepko.easycrafting.core.VersionHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockTable extends ItemBlock {

    public ItemBlockTable(int id) {
        super(id);
        setUnlocalizedName(VersionHelper.MOD_ID + ":table");
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        if (meta >= 0 && meta < BlockEasyCraftingTable.names.length) {
            return this.getUnlocalizedName() + "." + BlockEasyCraftingTable.names[meta];
        }
        return "missingno";
    }
}
