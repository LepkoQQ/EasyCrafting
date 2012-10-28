package net.lepko.minecraft.easycrafting;

import net.minecraft.src.TileEntity;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.Player;

public class EventManager {

	@ForgeSubscribe
	public void playerInteract(PlayerInteractEvent event) {
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
			TileEntity te = event.entityPlayer.worldObj.getBlockTileEntity(event.x, event.y, event.z);
			if (te != null && te instanceof TileEntityEasyCrafting) {
				PacketSender.sendEasyCraftingUpdateOutputToClient((Player) event.entityPlayer);
			}
		}
	}

	@ForgeSubscribe
	public void playerPickupItem(EntityItemPickupEvent event) {
		if (event.entityPlayer.craftingInventory instanceof ContainerEasyCrafting) {
			PacketSender.sendEasyCraftingUpdateOutputToClient((Player) event.entityPlayer);
		}
	}

	@ForgeSubscribe
	public void worldLoad(WorldEvent.Load event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			Version.updatePrint();
		}
	}
}
