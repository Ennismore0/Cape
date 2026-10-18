
package com.ennismore.cape.client.gui;

import com.ennismore.cape.client.CapeSelection;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;

public class CapeScreen extends Screen {

    private static final int CAPE_BLUE = 0xFF4B8DFE;

    private static final int FRAME_TEXTURE_SIZE = 88;
    private static final int BUTTON_TEXTURE_SIZE = 64;
    private static final int SETTINGS_TEXTURE_SIZE = 128;
    private static final int PREVIEW_TEXTURE_SIZE = 350;

    private static final int TILE_SIZE = 88;
    private static final int TILE_GAP = 12;
    private static final int TILE_STEP = TILE_SIZE + TILE_GAP;
    private static final int TILE_COLUMNS = 4;
    private static final int PREVIEW_DISPLAY_SIZE = 48;
    private static final int TILE_ROW_HEIGHT = 112;

    private static final List<CapeEntry> CAPES = List.of(
            new CapeEntry("Default Cape", "default", "default", true),
            new CapeEntry("No Cape", "none", "none", true),
            new CapeEntry("Cape", "cape", "cape", false),
            new CapeEntry("Migrator", "migrator", "migrator", false),
            new CapeEntry("Cherry Blossom", "cherry_blossom", "cherry_blossom", false),
            new CapeEntry("Mojang Studios", "mojang_studios", "mojang_studios", false),
            new CapeEntry("Purple Heart", "purple_heart", "purple_heart", false),
            new CapeEntry("15th Anniversary", "15th_anniversary", "15th_anniversary", false)
    );

    private String selectedCape = "default";
    private EditBox searchBox;
    private int gridLeft;
    private int gridTop;

    public CapeScreen() {
        super(Component.literal("Cape"));
    }

    @Override
    protected void init() {
        selectedCape = CapeSelection.getSelectedCape();

        int searchWidth = Math.min(260, this.width - 100);

        searchBox = new EditBox(
                this.font,
                this.width / 2 - searchWidth / 2,
                43,
                searchWidth,
                20,
                Component.literal("Search Capes")
        );

        searchBox.setHint(Component.literal("Search Capes..."));
        this.addRenderableWidget(searchBox);

        gridLeft = this.width / 2
                - (TILE_COLUMNS * TILE_SIZE
                + (TILE_COLUMNS - 1) * TILE_GAP) / 2;

        gridTop = 92;
    }

    private List<CapeEntry> getVisibleCapes() {
        String query = searchBox == null
                ? ""
                : searchBox.getValue().toLowerCase(Locale.ROOT).trim();

        return CAPES.stream()
                .filter(cape ->
                        cape.name().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        String title = "Cape";

        graphics.text(
                this.font,
                title,
                this.width / 2 - this.font.width(title) / 2,
                15,
                CAPE_BLUE,
                true
        );

        List<CapeEntry> visibleCapes = getVisibleCapes();

        for (int i = 0; i < visibleCapes.size(); i++) {
            CapeEntry cape = visibleCapes.get(i);

            int column = i % TILE_COLUMNS;
            int row = i / TILE_COLUMNS;
            int x = gridLeft + column * TILE_STEP;
            int y = gridTop + row * TILE_ROW_HEIGHT;

            boolean selected = cape.id().equals(selectedCape);

            Identifier frameTexture = texture(
                    "textures/gui/cape_frame_"
                            + (selected ? "highlighted" : "unhighlighted")
                            + ".png"
            );

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    frameTexture,
                    x,
                    y,
                    0.0f,
                    0.0f,
                    TILE_SIZE,
                    TILE_SIZE,
                    FRAME_TEXTURE_SIZE,
                    FRAME_TEXTURE_SIZE
            );

            Identifier preview = texture(
                    "textures/capes/previews/" + cape.previewId() + ".png"
            );

            int previewX = x + (TILE_SIZE - PREVIEW_DISPLAY_SIZE) / 2;
            int previewY = y + (TILE_SIZE - PREVIEW_DISPLAY_SIZE) / 2;

            if (this.minecraft.getResourceManager()
                    .getResource(preview).isPresent()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        preview,
                        previewX,
                        previewY,
                        0.0f,
                        0.0f,
                        PREVIEW_DISPLAY_SIZE,
                        PREVIEW_DISPLAY_SIZE,
                        PREVIEW_TEXTURE_SIZE,
                        PREVIEW_TEXTURE_SIZE,
                        PREVIEW_TEXTURE_SIZE,
                        PREVIEW_TEXTURE_SIZE
                );
            } else {
                String missing = "?";

                graphics.text(
                        this.font,
                        missing,
                        x + TILE_SIZE / 2 - this.font.width(missing) / 2,
                        y + TILE_SIZE / 2 - this.font.lineHeight / 2,
                        0xFFFFFFFF,
                        true
                );
            }

            int textColor = selected ? CAPE_BLUE : 0xFFFFFFFF;

            graphics.text(
                    this.font,
                    cape.name(),
                    x + (TILE_SIZE - this.font.width(cape.name())) / 2,
                    y + TILE_SIZE + 2,
                    textColor,
                    true
            );
        }

        drawCustomButton(
                graphics,
                this.width - 38,
                10,
                28,
                28,
                "textures/gui/cape_settings_button.png",
                SETTINGS_TEXTURE_SIZE
        );

        int doneWidth = 100;
        int doneHeight = 24;
        int doneX = this.width / 2 - doneWidth / 2;
        int doneY = this.height - 38;

        boolean doneHovered = isInside(
                mouseX,
                mouseY,
                doneX,
                doneY,
                doneWidth,
                doneHeight
        );

        drawCustomButton(
                graphics,
                doneX,
                doneY,
                doneWidth,
                doneHeight,
                "textures/gui/cape_button_"
                        + (doneHovered ? "highlighted" : "unhighlighted")
                        + ".png",
                BUTTON_TEXTURE_SIZE
        );

        String doneText = "Done";

        graphics.text(
                this.font,
                doneText,
                this.width / 2 - this.font.width(doneText) / 2,
                doneY + (doneHeight - this.font.lineHeight) / 2,
                0xFFFFFFFF,
                true
        );
    }

    private void drawCustomButton(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            String path,
            int textureSize
    ) {
        Identifier id = texture(path);

        if (this.minecraft.getResourceManager()
                .getResource(id).isPresent()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    id,
                    x,
                    y,
                    0.0f,
                    0.0f,
                    width,
                    height,
                    textureSize,
                    textureSize
            );
        }
    }

    private static Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath("cape", path);
    }

    private static boolean isInside(
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

    @Override
    public boolean mouseClicked(
            net.minecraft.client.input.MouseButtonEvent event,
            boolean doubleClick
    ) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            int doneWidth = 100;
            int doneHeight = 24;
            int doneX = this.width / 2 - doneWidth / 2;
            int doneY = this.height - 38;

            if (isInside(
                    mouseX,
                    mouseY,
                    doneX,
                    doneY,
                    doneWidth,
                    doneHeight
            )) {
                this.onClose();
                return true;
            }

            if (isInside(
                    mouseX,
                    mouseY,
                    this.width - 38,
                    10,
                    28,
                    28
            )) {
                this.minecraft.setScreen(new CapeSettingsScreen(this));
                return true;
            }

            List<CapeEntry> visibleCapes = getVisibleCapes();

            for (int i = 0; i < visibleCapes.size(); i++) {
                int column = i % TILE_COLUMNS;
                int row = i / TILE_COLUMNS;
                int x = gridLeft + column * TILE_STEP;
                int y = gridTop + row * TILE_ROW_HEIGHT;

                if (isInside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        TILE_SIZE,
                        TILE_SIZE + 18
                )) {
                    CapeEntry cape = visibleCapes.get(i);

                    selectedCape = cape.id();
                    CapeSelection.setSelectedCape(selectedCape);

                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    private record CapeEntry(
            String name,
            String id,
            String previewId,
            boolean special
    ) {
    }
}
