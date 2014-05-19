package net.lepko.easycrafting.core.network.packet;

import io.netty.buffer.ByteBuf;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.lepko.easycrafting.core.recipe.RecipeHelper;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.lepko.easycrafting.core.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.List;

public class PacketEasyCrafting extends EasyPacket {

    private ItemStack result;
    private ItemStack[] ingredients;
    private boolean isRightClick = false;

    public PacketEasyCrafting() {
    }

    public PacketEasyCrafting(WrappedRecipe recipe, boolean isRightClick) {
        setRecipe(recipe);
        this.isRightClick = isRightClick;
    }

    private void setRecipe(WrappedRecipe recipe) {
        result = recipe.output.stack;
        ingredients = new ItemStack[recipe.inputs.size()];

        for (int i = 0; i < recipe.inputs.size(); i++) {
            if (recipe.inputs.get(i) instanceof ItemStack) {
                ingredients[i] = ((ItemStack) recipe.inputs.get(i)).copy();
            } else if (recipe.inputs.get(i) instanceof List) {
                @SuppressWarnings("unchecked")
                List<ItemStack> ingList = (List<ItemStack>) recipe.inputs.get(i);
                ingredients[i] = ((ItemStack) ingList.get(0)).copy();
            }
        }
    }

    @Override
    public void run(EntityPlayer sender) {

        WrappedRecipe recipe = RecipeHelper.getValidRecipe(result, ingredients);
        if (recipe == null) {
            return;
        }

        ItemStack stack_in_hand = sender.inventory.getItemStack();

        // We need this call to canCraft() to populate the output in getCraftingResult() with NBT
        if (RecipeHelper.canCraft(recipe, sender.inventory, RecipeManager.getAllRecipes(), false, 1, ConfigHandler.MAX_RECURSION) == 0) {
            return;
        }

        ItemStack return_stack = recipe.handler.getCraftingResult(recipe, recipe.usedIngredients);
        int return_size = 0;

        if (stack_in_hand == null) {
            return_size = return_stack.stackSize;
        } else if (StackUtils.canStack(stack_in_hand, return_stack) == 0) {
            return_size = return_stack.stackSize + stack_in_hand.stackSize;
        }

        if (return_size > 0) {
            if (!isRightClick) {
                if (RecipeHelper.canCraft(recipe, sender.inventory, RecipeManager.getAllRecipes(), true, 1, ConfigHandler.MAX_RECURSION) > 0) {
                    return_stack.stackSize = return_size;
                    sender.inventory.setItemStack(return_stack);
                }
            } else {
                int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(return_stack, stack_in_hand);
                int timesCrafted = RecipeHelper.canCraft(recipe, sender.inventory, RecipeManager.getAllRecipes(), true, maxTimes, ConfigHandler.MAX_RECURSION);
                if (timesCrafted > 0) {
                    return_stack.stackSize = return_size + (timesCrafted - 1) * return_stack.stackSize;
                    sender.inventory.setItemStack(return_stack);
                }
            }
        }
    }

    @Override
    protected void readData(ByteBuf buf) throws IOException {

        isRightClick = buf.readBoolean();

        int id = buf.readShort();
        int damage = buf.readInt();
        int size = buf.readByte();

        result = new ItemStack(Item.getItemById(id), size, damage);

        int length = buf.readByte();

        ingredients = new ItemStack[length];

        for (int i = 0; i < length; i++) {
            int _id = buf.readShort();
            int _damage = buf.readInt();
            int _size = buf.readByte();

            ingredients[i] = new ItemStack(Item.getItemById(_id), _size, _damage);
        }
    }

    @Override
    protected void writeData(ByteBuf buf) throws IOException {

        buf.writeBoolean(isRightClick);

        buf.writeShort(Item.getIdFromItem(result.getItem()));
        buf.writeInt(result.getItemDamage());
        buf.writeByte(result.stackSize);

        buf.writeByte(ingredients.length);

        for (ItemStack is : ingredients) {
            buf.writeShort(Item.getIdFromItem(is.getItem()));
            buf.writeInt(is.getItemDamage());
            buf.writeByte(is.stackSize);
        }
    }
}
