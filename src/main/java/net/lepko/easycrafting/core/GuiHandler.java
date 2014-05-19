package net.lepko.easycrafting.core;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.lepko.easycrafting.EasyCrafting;
import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.core.block.TileEntityEasyCrafting;
import net.lepko.easycrafting.core.inventory.ContainerAutoCrafting;
import net.lepko.easycrafting.core.inventory.ContainerEasyCrafting;
import net.lepko.easycrafting.core.inventory.gui.GuiAutoCrafting;
import net.lepko.easycrafting.core.inventory.gui.GuiEasyCrafting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public enum GuiHandler implements IGuiHandler {
    INSTANCE;

    public static enum GuiType {
        EASYCRAFTING(0, TileEntityEasyCrafting.class, ContainerEasyCrafting.class, GuiEasyCrafting.class),
        AUTOCRAFTING(1, TileEntityAutoCrafting.class, ContainerAutoCrafting.class, GuiAutoCrafting.class);

        public final int id;
        public final Class<? extends TileEntity> teClass;
        public final Class<? extends Container> containerClass;
        public final Class<? extends GuiContainer> guiClass;

        GuiType(int id, Class<? extends TileEntity> teClass, Class<? extends Container> containerClass, Class<? extends GuiContainer> guiClass) {
            this.id = id;
            this.teClass = teClass;
            this.containerClass = containerClass;
            this.guiClass = guiClass;
        }
    }

    public static void openGui(GuiType gui, EntityPlayer player, World world, int x, int y, int z) {
        FMLNetworkHandler.openGui(player, EasyCrafting.instance, gui.id, world, x, y, z);
    }

    @Override
    public Object getServerGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        try {
            for (GuiType gui : GuiType.values()) {
                if (gui.id == guiID && gui.teClass.isInstance(te)) {
                    return gui.containerClass.getConstructor(InventoryPlayer.class, gui.teClass).newInstance(player.inventory, te);
                }
            }
        } catch (Throwable t) {
            Ref.LOGGER.warn("Bad gui container! id:" + guiID + " teClass:" + (te == null ? "null" : te.getClass().getName()), t);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        try {
            for (GuiType gui : GuiType.values()) {
                if (gui.id == guiID && gui.teClass.isInstance(te)) {
                    return gui.guiClass.getConstructor(InventoryPlayer.class, gui.teClass).newInstance(player.inventory, te);
                }
            }
        } catch (Throwable t) {
            Ref.LOGGER.warn("Bad gui! id:" + guiID + " teClass:" + (te == null ? "null" : te.getClass().getName()), t);
        }
        return null;
    }
}