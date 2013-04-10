package net.lepko.easycrafting.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.lepko.easycrafting.config.ConfigHandler;
import net.lepko.easycrafting.easyobjects.EasyItemStack;
import net.lepko.easycrafting.easyobjects.EasyRecipe;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.Player;

public class PacketEasyCrafting extends EasyPacket {

    private EasyItemStack result;
    ItemStack[] ingredients;
    private boolean isRightClick = false;

    public PacketEasyCrafting() {
        super(PacketHandler.PACKETID_EASYCRAFTING);
    }

    public PacketEasyCrafting(EasyRecipe recipe, boolean isRightClick) {
        this();
        setRecipe(recipe);
        this.isRightClick = isRightClick;
    }

    public void setRecipe(EasyRecipe recipe) {

        result = recipe.getResult();
        ingredients = new ItemStack[recipe.getIngredientsSize()];

        for (int i = 0; i < recipe.getIngredientsSize(); i++) {
            if (recipe.getIngredient(i) instanceof EasyItemStack) {
                EasyItemStack eis = (EasyItemStack) recipe.getIngredient(i);
                ingredients[i] = new ItemStack(eis.getID(), eis.getSize(), eis.getDamage());
            } else if (recipe.getIngredient(i) instanceof List) {
                // TODO: when updating forge use the new oredict method of obtaining oreID
                // TODO: change to custom oreDict EIS because if you getItem(-1) it will throw ArrayOutOfBoundException
                ingredients[i] = new ItemStack(-1, -1, -1);
            }
        }
    }

    @Override
    public void run(Player player) {

        EasyRecipe recipe = RecipeHelper.getValidRecipe(result, ingredients);
        if (recipe == null) {
            return;
        }

        EntityPlayer sender = (EntityPlayer) player;
        ItemStack stack_in_hand = sender.inventory.getItemStack();
        ItemStack return_stack = null;
        int return_size = 0;

        if (stack_in_hand == null) {
            return_stack = recipe.getResult().toItemStack();
            return_size = recipe.getResult().getSize();
        } else if (recipe.getResult().equalsItemStack(stack_in_hand, true) && stack_in_hand.getMaxStackSize() >= recipe.getResult().getSize() + stack_in_hand.stackSize && EasyItemStack.areStackTagsEqual(recipe.getResult(), stack_in_hand)) {
            return_stack = recipe.getResult().toItemStack();
            return_size = recipe.getResult().getSize() + stack_in_hand.stackSize;
        }

        if (return_stack != null) {
            if (!isRightClick) {
                if (RecipeHelper.canCraft(recipe, sender.inventory, RecipeHelper.getAllRecipes(), true, 1, ConfigHandler.MAX_RECURSION) > 0) {
                    return_stack.stackSize = return_size;
                    sender.inventory.setItemStack(return_stack);
                }
            } else {
                int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(return_stack, stack_in_hand);
                int timesCrafted = RecipeHelper.canCraft(recipe, sender.inventory, RecipeHelper.getAllRecipes(), true, maxTimes, ConfigHandler.MAX_RECURSION);
                if (timesCrafted > 0) {
                    return_stack.stackSize = return_size + (timesCrafted - 1) * recipe.getResult().getSize();
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

        result = new EasyItemStack(id, damage, size);

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

        data.writeShort(result.getID());
        data.writeInt(result.getDamage());
        data.writeByte(result.getSize());

        data.writeByte(ingredients.length);

        for (ItemStack is : ingredients) {
            data.writeShort(is.itemID);
            data.writeInt(is.getItemDamage());
            data.writeByte(is.stackSize);
        }
    }
}
