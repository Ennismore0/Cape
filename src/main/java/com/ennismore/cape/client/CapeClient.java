
package com.ennismore.cape.client;

import com.ennismore.cape.client.gui.CapeScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class CapeClient implements ClientModInitializer {

    private static final KeyMapping.Category CAPE_CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath("cape", "main")
            );

    private static final KeyMapping OPEN_CAPE_MENU =
            KeyMappingHelper.registerKeyMapping(
                    new KeyMapping(
                            "key.cape.open_menu",
                            InputConstants.Type.KEYSYM,
                            InputConstants.KEY_Y,
                            CAPE_CATEGORY
                    )
            );

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CAPE_MENU.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(new CapeScreen());
                }
            }
        });
    }
}
