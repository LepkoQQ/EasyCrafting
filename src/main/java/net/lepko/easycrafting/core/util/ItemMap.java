package net.lepko.easycrafting.core.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemMap {
    private static ListMultimap<Item, ItemStack> subItems = ArrayListMultimap.create();

    public static void build() {
        ArrayList<ItemStack> tmp = Lists.newArrayList();

        @SuppressWarnings("unchecked")
        Iterable<Item> itemRegistry = (Iterable<Item>) GameData.getItemRegistry();
        for (Item item : itemRegistry) {
            if (item == null) {
                continue;
            }

            tmp.clear();
            try {
                item.getSubItems(item, null, tmp);
            } catch (Throwable t) {
                continue;
            }

            subItems.putAll(item, tmp);
        }
    }

    public static List<ItemStack> get(Item item) {
        return subItems.get(item);
    }
}
