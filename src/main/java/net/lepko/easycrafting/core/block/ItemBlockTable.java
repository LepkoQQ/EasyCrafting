package net.lepko.easycrafting.core.block;

import net.lepko.easycrafting.Ref;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockTable extends ItemBlock {

    public ItemBlockTable(Block block) {
        super(block);
        setUnlocalizedName(Ref.addDomain("table"));
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        if (meta >= 0 && meta < BlockTable.names.length) {
            return this.getUnlocalizedName() + "." + BlockTable.names[meta];
        }
        return "missingno";
    }
}
