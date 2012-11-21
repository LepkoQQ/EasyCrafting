//package net.lepko.minecraft.easycrafting;
//
//import java.util.EnumSet;
//
//import net.lepko.minecraft.easycrafting.helpers.KeyHelper;
//import net.lepko.minecraft.easycrafting.helpers.VersionHelper;
//import net.minecraft.src.KeyBinding;
//import cpw.mods.fml.client.registry.KeyBindingRegistry;
//import cpw.mods.fml.common.TickType;
//
//public class KeyPressHandler extends KeyBindingRegistry.KeyHandler {
//
//	public KeyPressHandler() {
//		super(KeyHelper.getKeyBindingsArray(), KeyHelper.getKeyRepeatingsArray());
//	}
//
//	@Override
//	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
//		if (tickEnd) {
//			KeyHelper.onKeyDown(kb);
//		}
//	}
//
//	@Override
//	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
//	}
//
//	@Override
//	public EnumSet<TickType> ticks() {
//		return EnumSet.of(TickType.CLIENT);
//	}
//
//	@Override
//	public String getLabel() {
//		return VersionHelper.MOD_ID + "-" + this.getClass().getSimpleName();
//	}
// }
