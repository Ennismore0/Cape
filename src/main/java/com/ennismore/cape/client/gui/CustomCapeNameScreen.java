package com.ennismore.cape.client.gui;

import com.ennismore.cape.client.CapeSelection;
import com.ennismore.cape.client.CustomCapeManager;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

import net.minecraft.network.chat.Component;

import java.io.IOException;

import java.nio.file.Path;

public class CustomCapeNameScreen extends Screen {

    private final Screen parent;
    private final Path image;
    private EditBox nameBox;
    private String error = "";

    public CustomCapeNameScreen(Screen parent, Path image) {
        super(Component.literal("Name Custom Cape"));
        this.parent = parent;
        this.image = image;
    }

    @Override
    protected void init() {
        int center = this.width / 2;

        nameBox = new EditBox(
                this.font,
                center - 110,
                this.height / 2 - 12,
                220,
                20,
                Component.literal("Cape Name")
        );

        nameBox.setHint(Component.literal("Leave blank for an automatic name"));
        nameBox.setMaxLength(40);
        addRenderableWidget(nameBox);

        addRenderableWidget(Button.builder(
                Component.literal("Upload"),
                button -> upload()
        ).bounds(center - 105, this.height / 2 + 20, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Cancel"),
                button -> onClose()
        ).bounds(center + 5, this.height / 2 + 20, 100, 20).build());

        setInitialFocus(nameBox);
    }

    private void upload() {
        try {
            var cape = CustomCapeManager.addCape(image, nameBox.getValue());

            if (cape.isPresent()) {
                CapeSelection.setSelectedCape(cape.get().id());
                minecraft.setScreen(parent);
            }
        } catch (IOException | RuntimeException exception) {
            error = exception.getMessage() == null
                    ? "Could not upload this cape."
                    : exception.getMessage();
        }
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        String title = "Name Your Custom Cape";

        graphics.text(
                font,
                title,
                width / 2 - font.width(title) / 2,
                height / 2 - 48,
                0xFFFFFFFF,
                true
        );

        if (!error.isEmpty()) {
            graphics.text(
                    font,
                    error,
                    width / 2 - font.width(error) / 2,
                    height / 2 + 48,
                    0xFFFF5555,
                    true
            );
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
