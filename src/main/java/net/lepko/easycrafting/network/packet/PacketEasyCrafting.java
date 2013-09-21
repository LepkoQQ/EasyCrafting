package net.lepko.easycrafting.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.recipe.RecipeHelper;
import net.lepko.easycrafting.recipe.RecipeManager;
import net.lepko.easycrafting.recipe.WrappedRecipe;
import net.lepko.easycrafting.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.Player;

public class PacketEasyCrafting extends EasyPacket {

    private ItemStack result;
    private ItemStack[] ingredients;
    private boolean isRightClick = false;

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
    public void run(Player player) {

        WrappedRecipe recipe = RecipeHelper.getValidRecipe(result, ingredients);
        if (recipe == null) {
            return;
        }

        EntityPlayer sender = (EntityPlayer) player;
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
    protected void readData(DataInputStream data) throws IOException {

        isRightClick = data.readBoolean();

        int id = data.readShort();
        int damage = data.readInt();
        int size = data.readByte();

        result = new ItemStack(id, size, damage);

        int length = data.readByte();

        ingredients = new ItemStack[length];

        for (int i = 0; i < length; i++) {
            int _id = data.readShort();
            int _damage = data.readInt();
            int _size = data.readByte();

            ingredients[i] = new ItemStack(_id, _size, _damage);
        }
    }

    @Override
    protected void writeData(DataOutputStream data) throws IOException {

        data.writeBoolean(isRightClick);

        data.writeShort(result.itemID);
        data.writeInt(result.getItemDamage());
        data.writeByte(result.stackSize);

        data.writeByte(ingredients.length);

        for (ItemStack is : ingredients) {
            data.writeShort(is.itemID);
            data.writeInt(is.getItemDamage());
            data.writeByte(is.stackSize);
        }
    }
}
