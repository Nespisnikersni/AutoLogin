package com.autologin;


import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;


public class AutoLoginClient implements ClientModInitializer {
	public static final String MOD_ID = "autologin";
	public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("AutoLogin");
	private ConfigA config=new ConfigA();
	private static KeyBinding keyBinding;

	@Override
	public void onInitializeClient() {
		config.createConfig("Data");
		config.createConfig("AutoLogin");
		if(config.isEmpty("AutoLogin")) {
			config.addString("AutoLogin", "autoLogin", "true");
			config.addString("AutoLogin", "autoRegister", "true");
			config.addString("AutoLogin", "autoRegisterPassword", ConfigA.encryptString("12345678"));
		}
		registerModMenuIntegration();
		ClientReceiveMessageEvents.GAME.register(new Event());
		ClientSendMessageEvents.COMMAND.register(new Event());
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"login settings",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_U,
				"AutoLogin"
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client!=null) {
                while (keyBinding.wasPressed()) {
                    ConfigMenuScreen configMenuScreen = new ConfigMenuScreen();
                    client.setScreen(configMenuScreen);
                }
            }
		});
	}
	public static void registerModMenuIntegration() {
		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			ModMenuApi modMenuApi = new ModMenuIntegration();
		}
	}
}




