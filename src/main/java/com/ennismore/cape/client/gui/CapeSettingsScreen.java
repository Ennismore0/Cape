
package com.ennismore.cape.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CapeSettingsScreen extends Screen {

    private final Screen parent;

    public CapeSettingsScreen(Screen parent) {
        super(Component.literal("Cape Settings"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(
                this.font,
                "Cape Settings",
                this.width / 2,
                30,
                0xFF4B8DFE,
                true
        );

        graphics.text(
                this.font,
                "Settings are coming soon!",
                this.width / 2,
                this.height / 2,
                0xFFFFFFFF,
                true
        );

        graphics.text(
                this.font,
                "Press ESC to return",
                this.width / 2,
                this.height / 2 + 20,
                0xFFAAAAAA,
                true
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
