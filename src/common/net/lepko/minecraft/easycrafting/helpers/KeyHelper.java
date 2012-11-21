//package net.lepko.minecraft.easycrafting.helpers;
//
//import java.util.ArrayList;
//
//import net.lepko.minecraft.easycrafting.ModEasyCrafting;
//import net.lepko.minecraft.easycrafting.Proxy;
//import net.minecraft.client.Minecraft;
//import net.minecraft.src.KeyBinding;
//
//import org.lwjgl.input.Keyboard;
//
//import cpw.mods.fml.client.FMLClientHandler;
//
//public class KeyHelper {
//
//	private static ArrayList<KeyBinding> keyBindingsList = new ArrayList<KeyBinding>();
//	private static ArrayList<Boolean> keyRepeatingsList = new ArrayList<Boolean>();
//
//	public static boolean[] getKeyRepeatingsArray() {
//		boolean[] array = new boolean[keyRepeatingsList.size()];
//		for (int i = 0; i < array.length; i++) {
//			array[i] = keyRepeatingsList.get(i).booleanValue();
//		}
//		return array;
//	}
//
//	public static KeyBinding[] getKeyBindingsArray() {
//		return keyBindingsList.toArray(new KeyBinding[keyBindingsList.size()]);
//	}
//
//	public static void addKeyBind(String action, int code, boolean isRepeating) {
//		keyBindingsList.add(new KeyBinding(action, code));
//		keyRepeatingsList.add(isRepeating);
//	}
//
//	/**
//	 * http://www.minecraftwiki.net/wiki/Key_Codes
//	 */
//	public static void addKeyBind(String action, String name) {
//		if (name.indexOf("KEY_") > -1) {
//			name = name.substring(4);
//		}
//		int code = Keyboard.getKeyIndex(name);
//		if (code != Keyboard.KEY_NONE) {
//			addKeyBind(action, code, false);
//		}
//	}
//
//	public static void onKeyDown(KeyBinding kb) {
//		EasyLog.log(kb.keyDescription + " pressed!");
//		if (Proxy.proxy.isClient()) {
//			Minecraft mc = FMLClientHandler.instance().getClient();
//			EasyLog.log("currentScreen: " + mc.currentScreen);
//			if (mc.currentScreen == null) {
//				mc.thePlayer.openGui(ModEasyCrafting.instance, 1, mc.thePlayer.worldObj, (int) mc.thePlayer.posX, (int) mc.thePlayer.posY, (int) mc.thePlayer.posZ);
//			}
//		}
//	}
// }
