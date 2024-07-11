package com.autologin;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return parent -> {
                ConfigMenuScreen configMenuScreen = new ConfigMenuScreen();
                return configMenuScreen;
            };
    }
}
