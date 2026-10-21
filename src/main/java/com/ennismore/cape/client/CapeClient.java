package com.ennismore.cape.client;

import com.ennismore.cape.client.gui.CapeScreen;

import com.mojang.blaze3d.platform.InputConstants;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import net.minecraft.client.KeyMapping;

import net.minecraft.network.chat.Component;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;

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

    private static final List<String> BUILT_IN_CAPES = List.of(
            "default",
            "none",
            "cape",
            "migrator",
            "cherry_blossom",
            "mojang_studios",
            "purple_heart",
            "15th_anniversary",
            "vanilla",
            "minecon_2012",
            "twisted",
            "hero",
            "copper",
            "aurora"
    );

    @Override
    public void onInitializeClient() {
        CustomCapeManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CAPE_MENU.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(new CapeScreen());
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        ClientCommands.literal("cape")
                                .executes(context -> {
                                    context.getSource().getClient().setScreen(
                                            new CapeScreen()
                                    );
                                    return 1;
                                })
                                .then(
                                        ClientCommands.argument(
                                                        "cape",
                                                        StringArgumentType.word()
                                                )
                                                .suggests((context, builder) -> {
                                                    for (String id : BUILT_IN_CAPES) {
                                                        builder.suggest(id);
                                                    }

                                                    for (CustomCapeManager.CustomCape cape
                                                            : CustomCapeManager.getCapes()) {
                                                        builder.suggest(cape.id());
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    String id = StringArgumentType
                                                            .getString(context, "cape")
                                                            .toLowerCase(Locale.ROOT);

                                                    if (!BUILT_IN_CAPES.contains(id)
                                                            && !CustomCapeManager.isCustomCape(id)) {
                                                        context.getSource().sendError(
                                                                Component.literal(
                                                                        "Unknown cape ID: " + id
                                                                ).withStyle(style -> style.withBold(true))
                                                        );
                                                        return 0;
                                                    }

                                                    CapeSelection.setSelectedCape(id);

                                                    context.getSource().sendFeedback(
                                                            Component.literal(
                                                                    "Cape equipped: " + id
                                                            ).withStyle(style -> style.withBold(true))
                                                    );

                                                    return 1;
                                                })
                                )
                )
        );
    }
}
