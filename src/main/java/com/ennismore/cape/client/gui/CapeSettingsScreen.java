package com.ennismore.cape.client.gui;

import com.ennismore.cape.client.CapeClient;
import com.ennismore.cape.client.CapeSettings;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.network.chat.Component;

import net.minecraft.resources.Identifier;

public class CapeSettingsScreen extends Screen {

    private static final int CAPE_BLUE = 0xFF4B8DFE;
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 24;

    private final Screen parent;
    private boolean awaitingKeybind;

    public CapeSettingsScreen(Screen parent) {
        super(Component.literal("Cape Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
    }

    private int buttonLeft() {
        return this.width / 2 - BUTTON_WIDTH / 2;
    }

    private int buttonY(int index) {
        return 58 + index * 30;
    }

    private boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    private void drawButton(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            int x,
            int y,
            String label,
            boolean enabled
    ) {
        boolean hovered = enabled && isInside(
                mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT
        );

        Identifier texture = Identifier.fromNamespaceAndPath(
                "cape",
                "textures/gui/cape_button_"
                        + (hovered ? "highlighted" : "unhighlighted")
                        + ".png"
        );

        if (this.minecraft.getResourceManager().getResource(texture).isPresent()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    x, y,
                    0.0f, 0.0f,
                    BUTTON_WIDTH, BUTTON_HEIGHT,
                    64, 64
            );
        }

        graphics.text(
                this.font,
                label,
                this.width / 2 - this.font.width(label) / 2,
                y + (BUTTON_HEIGHT - this.font.lineHeight) / 2,
                enabled ? 0xFFFFFFFF : 0xFFAAAAAA,
                true
        );
    }

    private String getViewLabel() {
        return "View: " + (
                CapeSettings.getView() == CapeSettings.View.GRID
                        ? "Grid"
                        : "List"
        );
    }

    private String getFilterLabel() {
        String filter = switch (CapeSettings.getFilter()) {
            case ALL -> "All";
            case UNOBTAINABLE -> "Unobtainable";
            case LIMITED_TIME -> "Limited Time Only";
            case CUSTOM -> "Custom Capes";
        };

        return "Filter: " + filter;
    }

    private String getDefaultCapeLabel() {
        return "Default Cape: " + CapeSettings.getDefaultCape();
    }


    private String getKeybindLabel() {
        if (awaitingKeybind) {
            return "Press a key... (Esc to cancel)";
        }

        return "Edit Keybind: "
                + CapeClient.OPEN_CAPE_MENU.saveString();
}

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        String title = "Cape Settings";

        graphics.text(
                this.font,
                title,
                this.width / 2 - this.font.width(title) / 2,
                25,
                CAPE_BLUE,
                true
        );

        int left = buttonLeft();

        drawButton(graphics, mouseX, mouseY, left, buttonY(0),
                getViewLabel(), true);
        drawButton(graphics, mouseX, mouseY, left, buttonY(1),
                getFilterLabel(), true);
        drawButton(graphics, mouseX, mouseY, left, buttonY(2),
                getDefaultCapeLabel(), true);
        drawButton(graphics, mouseX, mouseY, left, buttonY(3),
                getKeybindLabel(), true);
        drawButton(graphics, mouseX, mouseY, left, this.height - 40,
                "Back", true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        double mouseX = event.x();
        double mouseY = event.y();

        int left = buttonLeft();

        if (isInside(mouseX, mouseY, left, buttonY(0),
                BUTTON_WIDTH, BUTTON_HEIGHT)) {
            CapeSettings.setView(
                    CapeSettings.getView() == CapeSettings.View.GRID
                            ? CapeSettings.View.LIST
                            : CapeSettings.View.GRID
            );
            return true;
        }

        if (isInside(mouseX, mouseY, left, buttonY(1),
                BUTTON_WIDTH, BUTTON_HEIGHT)) {
            CapeSettings.Filter[] filters = CapeSettings.Filter.values();
            int next = (CapeSettings.getFilter().ordinal() + 1)
                    % filters.length;
            CapeSettings.setFilter(filters[next]);
            return true;
        }

        if (isInside(mouseX, mouseY, left, buttonY(2),
                BUTTON_WIDTH, BUTTON_HEIGHT)) {
            this.minecraft.setScreen(new CapeScreen(this, true));
            return true;
        }

        if (isInside(mouseX, mouseY, left, buttonY(3),
                BUTTON_WIDTH, BUTTON_HEIGHT)) {
            awaitingKeybind = true;
            return true;
        }

        if (isInside(mouseX, mouseY, left, this.height - 40,
                BUTTON_WIDTH, BUTTON_HEIGHT)) {
            onClose();
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (awaitingKeybind) {
            if (event.key() == InputConstants.KEY_ESCAPE) {
                awaitingKeybind = false;
                return true;
            }

            CapeClient.OPEN_CAPE_MENU.setKey(
                    InputConstants.getKey(event)
            );

            net.minecraft.client.KeyMapping.resetMapping();
            this.minecraft.options.save();

            awaitingKeybind = false;
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
