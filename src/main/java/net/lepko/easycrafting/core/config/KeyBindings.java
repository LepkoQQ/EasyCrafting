package net.lepko.easycrafting.core.config;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyBindings {

	public static KeyBinding focusSearch;

	public static void init() {
		// Define the "focus" binding, with (unlocalized) name "key.focus" and
		// the category with (unlocalized) name "key.categories.EasyCrafting"
		// and
		// key code 33 ("F", LWJGL constant: Keyboard.KEY_F)
		focusSearch = new KeyBinding("key.focus", Keyboard.KEY_F,
				"key.categories.easycrafting");

		// Register KeyBinding to the ClientRegistry
		ClientRegistry.registerKeyBinding(focusSearch);
	}

}