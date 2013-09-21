package net.lepko.easycrafting.block;

import java.util.List;

import net.lepko.easycrafting.core.GuiHandler;
import net.lepko.easycrafting.core.VersionHelper;
import net.lepko.easycrafting.recipe.RecipeManager;
import net.lepko.easycrafting.util.InventoryUtils;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTable extends BlockContainer {

    public static String[] names = { "easy_crafting", "auto_crafting" };
    private Icon[][] icons = new Icon[2][4];

    public BlockTable(int blockID) {
        super(blockID, Material.wood);
        setHardness(2.5F);
        setStepSound(soundWoodFootstep);
        setUnlocalizedName(VersionHelper.MOD_ID + ":table");
        setTextureName(VersionHelper.MOD_ID + ":table");
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister) {
        // Easy Crafting Table
        icons[0][0] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "easyCraftingTable_bottom");
        icons[0][1] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "easyCraftingTable_top");
        icons[0][2] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "easyCraftingTable_side1");
        icons[0][3] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "easyCraftingTable_side2");

        // Auto Crafting Table
        icons[1][0] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "autoCraftingTable_bottom");
        icons[1][1] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "autoCraftingTable_top");
        icons[1][2] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "autoCraftingTable_side1");
        icons[1][3] = iconRegister.registerIcon(VersionHelper.MOD_ID + ":" + "autoCraftingTable_side2");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta) {
        if (meta < 0 || meta > icons.length) {
            meta = 0;
        }
        switch (side) {
            case 0:
                return icons[meta][0];
            case 1:
                return icons[meta][1];
            case 2:
            case 3:
                return icons[meta][2];
            default:
                return icons[meta][3];
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(int id, CreativeTabs tab, List list) {
        for (int meta = 0; meta < names.length; meta++) {
            list.add(new ItemStack(id, 1, meta));
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getBlockTileEntity(x, y, z);

        if (te == null || player.isSneaking()) {
            return false;
        }

        if (te instanceof TileEntityEasyCrafting) {
            RecipeManager.scanRecipes();
            GuiHandler.openGui(GuiHandler.GUI_EASYCRAFTING, player, world, x, y, z);
            return true;
        }
        if (te instanceof TileEntityAutoCrafting) {
            GuiHandler.openGui(GuiHandler.GUI_AUTOCRAFTING, player, world, x, y, z);
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int i, int j) {
        InventoryUtils.dropItems(world.getBlockTileEntity(x, y, z));
        super.breakBlock(world, x, y, z, i, j);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        switch (metadata) {
            case 1:
                return new TileEntityAutoCrafting();
            default:
                return new TileEntityEasyCrafting();
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return createTileEntity(world, -1);
    }
}