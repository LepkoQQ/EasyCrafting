package net.lepko.easycrafting.block;

import java.util.Random;

import net.lepko.easycrafting.ModEasyCrafting;
import net.lepko.easycrafting.helpers.RecipeHelper;
import net.lepko.easycrafting.helpers.VersionHelper;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEasyCraftingTable extends BlockContainer {

    private Icon[] icons = new Icon[4];

    public BlockEasyCraftingTable(int blockID) {
        super(blockID, Material.wood);
        setHardness(2.5F);
        setStepSound(soundWoodFootstep);
        setUnlocalizedName("easycraftingtable");
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister) {
        icons[0] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "easyCraftingTable_top");
        icons[1] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "easyCraftingTable_bottom");
        icons[2] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "easyCraftingTable_side1");
        icons[3] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "easyCraftingTable_side2");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta) {
        switch (side) {
            case 0:
                return icons[1];
            case 1:
                return icons[0];
            case 2:
            case 3:
                return icons[2];
            default:
                return icons[3];
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float f, float g, float t) {
        TileEntity tile_entity = world.getBlockTileEntity(x, y, z);

        if (tile_entity == null || player.isSneaking()) {
            return false;
        }

        if (!(tile_entity instanceof TileEntityEasyCrafting)) {
            return false;
        }

        RecipeHelper.checkForNewRecipes();
        player.openGui(ModEasyCrafting.instance, 0, world, x, y, z);
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int i, int j) {
        dropItems(world, x, y, z);
        super.breakBlock(world, x, y, z, i, j);
    }

    private void dropItems(World world, int x, int y, int z) {
        Random rand = new Random();

        TileEntityEasyCrafting var7 = (TileEntityEasyCrafting) world.getBlockTileEntity(x, y, z);

        if (var7 != null) {
            for (int var8 = 0; var8 < var7.getSizeInventory(); ++var8) {
                ItemStack var9 = var7.getStackInSlot(var8);

                if (var9 != null) {
                    float var10 = rand.nextFloat() * 0.8F + 0.1F;
                    float var11 = rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem var14;

                    for (float var12 = rand.nextFloat() * 0.8F + 0.1F; var9.stackSize > 0; world.spawnEntityInWorld(var14)) {
                        int var13 = rand.nextInt(21) + 10;

                        if (var13 > var9.stackSize) {
                            var13 = var9.stackSize;
                        }

                        var9.stackSize -= var13;
                        var14 = new EntityItem(world, x + var10, y + var11, z + var12, new ItemStack(var9.itemID, var13, var9.getItemDamage()));
                        float var15 = 0.05F;
                        var14.motionX = (float) rand.nextGaussian() * var15;
                        var14.motionY = (float) rand.nextGaussian() * var15 + 0.2F;
                        var14.motionZ = (float) rand.nextGaussian() * var15;

                        if (var9.hasTagCompound()) {
                            var14.getEntityItem().setTagCompound((NBTTagCompound) var9.getTagCompound().copy());
                        }
                    }
                }
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return new TileEntityEasyCrafting();
    }
}