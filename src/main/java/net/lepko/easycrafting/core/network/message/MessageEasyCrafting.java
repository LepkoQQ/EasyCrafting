package net.lepko.easycrafting.core.network.message;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.lepko.easycrafting.core.recipe.RecipeHelper;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.lepko.easycrafting.core.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MessageEasyCrafting extends AbstractMessage {

    private ItemStack result;
    private ItemStack[] ingredients;
    private boolean isRightClick = false;

    public MessageEasyCrafting() {}

    public MessageEasyCrafting(WrappedRecipe recipe, boolean isRightClick) {
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
    public void write(ByteBuf target) {
        target.writeBoolean(isRightClick);

        target.writeShort(Item.getIdFromItem(result.getItem()));
        target.writeInt(result.getItemDamage());
        target.writeByte(result.stackSize);

        target.writeByte(ingredients.length);

        for (ItemStack is : ingredients) {
            target.writeShort(Item.getIdFromItem(is.getItem()));
            target.writeInt(is.getItemDamage());
            target.writeByte(is.stackSize);
        }
    }

    @Override
    public void read(ByteBuf source) {
        isRightClick = source.readBoolean();

        int id = source.readShort();
        int damage = source.readInt();
        int size = source.readByte();

        result = new ItemStack(Item.getItemById(id), size, damage);

        int length = source.readByte();

        ingredients = new ItemStack[length];

        for (int i = 0; i < length; i++) {
            int _id = source.readShort();
            int _damage = source.readInt();
            int _size = source.readByte();

            ingredients[i] = new ItemStack(Item.getItemById(_id), _size, _damage);
        }
    }

    @Override
    public void run(EntityPlayer player, Side side) {
        Ref.LOGGER.info("Message: " + this.getClass().getName() + " Side: " + side);

        WrappedRecipe recipe = RecipeHelper.getValidRecipe(result, ingredients);
        if (recipe == null) {
            return;
        }

        ItemStack stack_in_hand = player.inventory.getItemStack();

        // We need this call to canCraft() to populate the output in getCraftingResult() with NBT
        if (RecipeHelper.canCraft(recipe, player.inventory, RecipeManager.getAllRecipes(), false, 1, ConfigHandler.MAX_RECURSION) == 0) {
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
                if (RecipeHelper.canCraft(recipe, player.inventory, RecipeManager.getAllRecipes(), true, 1, ConfigHandler.MAX_RECURSION) > 0) {
                    return_stack.stackSize = return_size;
                    player.inventory.setItemStack(return_stack);
                }
            } else {
                int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(return_stack, stack_in_hand);
                int timesCrafted = RecipeHelper.canCraft(recipe, player.inventory, RecipeManager.getAllRecipes(), true, maxTimes, ConfigHandler.MAX_RECURSION);
                if (timesCrafted > 0) {
                    return_stack.stackSize = return_size + (timesCrafted - 1) * return_stack.stackSize;
                    player.inventory.setItemStack(return_stack);
                }
            }
        }
    }
}
