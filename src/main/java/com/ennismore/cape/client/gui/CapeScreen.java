package com.ennismore.cape.client.gui;

import com.ennismore.cape.client.CapeSelection;
import com.ennismore.cape.client.CustomCapeManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    private static final int GRID_BOTTOM_MARGIN = 48;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int UPLOAD_SIZE = 16;

    private static final List<CapeEntry> CAPES = List.of(
            new CapeEntry("Default Cape", "default", "default", true),
            new CapeEntry("No Cape", "none", "none", true),
            new CapeEntry("Cape", "cape", "cape", false),
            new CapeEntry("Migrator", "migrator", "migrator", false),
            new CapeEntry("Cherry Blossom", "cherry_blossom", "cherry_blossom", false),
            new CapeEntry("Mojang Studios", "mojang_studios", "mojang_studios", false),
            new CapeEntry("Purple Heart", "purple_heart", "purple_heart", false),
            new CapeEntry("15th Anniversary", "15th_anniversary", "15th_anniversary", false),
            new CapeEntry("Vanilla", "vanilla", "vanilla", false),
            new CapeEntry("MineCon 2012", "minecon_2012", "minecon_2012", false),
            new CapeEntry("Twisted", "twisted", "twisted", false),
            new CapeEntry("Hero", "hero", "hero", false),
            new CapeEntry("Copper", "copper", "copper", false),
            new CapeEntry("Aurora", "aurora", "aurora", false)
    );

    private String selectedCape = "default";
    private EditBox searchBox;
    private int gridLeft;
    private int gridTop;
    private int gridBottom;
    private int scrollOffset;

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
        gridBottom = Math.max(gridTop, this.height - GRID_BOTTOM_MARGIN);
        scrollOffset = clampScrollOffset(scrollOffset);
    }

    private List<CapeEntry> getVisibleCapes() {
        String query = searchBox == null
                ? ""
                : searchBox.getValue().toLowerCase(Locale.ROOT).trim();

        List<CapeEntry> entries = new ArrayList<>(CAPES);

        for (CustomCapeManager.CustomCape cape : CustomCapeManager.getCapes()) {
            entries.add(new CapeEntry(cape.name(), cape.id(), cape.id(), false));
        }

        return entries.stream()
                .filter(cape -> cape.name().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private int getContentHeight() {
        int count = getVisibleCapes().size();
        int rows = (count + TILE_COLUMNS - 1) / TILE_COLUMNS;
        return rows == 0 ? 0 : rows * TILE_ROW_HEIGHT - TILE_GAP;
    }

    private int getMaxScroll() {
        return Math.max(0, getContentHeight() - (gridBottom - gridTop));
    }

    private int clampScrollOffset(int offset) {
        return Math.max(0, Math.min(offset, getMaxScroll()));
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
        scrollOffset = clampScrollOffset(scrollOffset);

        graphics.enableScissor(
                gridLeft,
                gridTop,
                Math.min(this.width, gridLeft + TILE_COLUMNS * TILE_STEP - TILE_GAP),
                gridBottom
        );

        for (int i = 0; i < visibleCapes.size(); i++) {
            CapeEntry cape = visibleCapes.get(i);

            int column = i % TILE_COLUMNS;
            int row = i / TILE_COLUMNS;
            int x = gridLeft + column * TILE_STEP;
            int y = gridTop + row * TILE_ROW_HEIGHT - scrollOffset;

            if (y + TILE_ROW_HEIGHT <= gridTop || y >= gridBottom) {
                continue;
            }

            boolean selected = cape.id().equals(selectedCape);

            Identifier frameTexture = texture(
                    "textures/gui/cape_frame_"
                            + (selected ? "highlighted" : "unhighlighted")
                            + ".png"
            );

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    frameTexture,
                    x, y,
                    0.0f, 0.0f,
                    TILE_SIZE, TILE_SIZE,
                    FRAME_TEXTURE_SIZE, FRAME_TEXTURE_SIZE
            );

            boolean custom = CustomCapeManager.isCustomCape(cape.id());

            Identifier preview = custom
                    ? CustomCapeManager.getTexture(cape.id())
                    : texture("textures/capes/previews/" + cape.previewId() + ".png");

            int previewWidth = custom ? 64 : PREVIEW_DISPLAY_SIZE;
            int previewHeight = custom ? 32 : PREVIEW_DISPLAY_SIZE;
            int previewX = x + (TILE_SIZE - previewWidth) / 2;
            int previewY = y + (TILE_SIZE - previewHeight) / 2;

            boolean previewExists = preview != null
                    && (custom || this.minecraft.getResourceManager().getResource(preview).isPresent());

            if (previewExists) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        preview,
                        previewX, previewY,
                        0.0f, 0.0f,
                        previewWidth, previewHeight,
                        custom ? 64 : PREVIEW_TEXTURE_SIZE,
                        custom ? 32 : PREVIEW_TEXTURE_SIZE,
                        custom ? 64 : PREVIEW_TEXTURE_SIZE,
                        custom ? 32 : PREVIEW_TEXTURE_SIZE
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

        graphics.disableScissor();
        drawScrollbar(graphics, mouseX, mouseY);

        drawCustomButton(
                graphics,
                this.width - 38, 10, 28, 28,
                "textures/gui/cape_settings_button.png",
                SETTINGS_TEXTURE_SIZE
        );

        int doneWidth = 100;
        int doneHeight = 24;
        int doneX = this.width / 2 - doneWidth / 2;
        int doneY = this.height - 38;

        boolean doneHovered = isInside(mouseX, mouseY, doneX, doneY, doneWidth, doneHeight);

        drawCustomButton(
                graphics,
                doneX, doneY, doneWidth, doneHeight,
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

        int uploadX = doneX + doneWidth + 8;
        int uploadY = doneY + (doneHeight - UPLOAD_SIZE) / 2;

        boolean uploadHovered = isInside(
                mouseX, mouseY, uploadX, uploadY, UPLOAD_SIZE, UPLOAD_SIZE
        );

        drawCustomButton(
                graphics,
                uploadX, uploadY, UPLOAD_SIZE, UPLOAD_SIZE,
                "textures/gui/cape_upload_button_"
                        + (uploadHovered ? "highlighted" : "unhighlighted")
                        + ".png",
                UPLOAD_SIZE
        );

        if (uploadHovered) {
            graphics.text(
                    this.font,
                    "Upload Custom Cape",
                    mouseX + 8,
                    mouseY - 12,
                    0xFFFFFFFF,
                    true
            );
        }
    }

    private void drawScrollbar(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        int contentHeight = getContentHeight();
        int viewportHeight = gridBottom - gridTop;

        if (contentHeight <= viewportHeight || viewportHeight <= 0) {
            return;
        }

        int barX = Math.min(
                this.width - SCROLLBAR_WIDTH - 4,
                gridLeft + TILE_COLUMNS * TILE_STEP - TILE_GAP + 6
        );

        graphics.fill(barX, gridTop, barX + SCROLLBAR_WIDTH, gridBottom, 0x66333333);

        int thumbHeight = Math.max(18, viewportHeight * viewportHeight / contentHeight);
        int thumbTravel = viewportHeight - thumbHeight;
        int thumbY = gridTop + thumbTravel * scrollOffset / Math.max(1, getMaxScroll());

        boolean hovered = isInside(
                mouseX, mouseY, barX, thumbY, SCROLLBAR_WIDTH, thumbHeight
        );

        graphics.fill(
                barX, thumbY, barX + SCROLLBAR_WIDTH, thumbY + thumbHeight,
                hovered ? CAPE_BLUE : 0xFFCCCCCC
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

        if (this.minecraft.getResourceManager().getResource(id).isPresent()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    id,
                    x, y,
                    0.0f, 0.0f,
                    width, height,
                    textureSize, textureSize
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
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (isInside(
                mouseX, mouseY,
                gridLeft, gridTop,
                TILE_COLUMNS * TILE_STEP - TILE_GAP,
                gridBottom - gridTop
        )) {
            scrollOffset = clampScrollOffset(
                    scrollOffset - (int) Math.signum(scrollY) * TILE_ROW_HEIGHT
            );
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

            if (isInside(mouseX, mouseY, doneX, doneY, doneWidth, doneHeight)) {
                this.onClose();
                return true;
            }

            int uploadX = doneX + doneWidth + 8;
            int uploadY = doneY + (doneHeight - UPLOAD_SIZE) / 2;

            if (isInside(mouseX, mouseY, uploadX, uploadY, UPLOAD_SIZE, UPLOAD_SIZE)) {
                openUploadPicker();
                return true;
            }

            if (isInside(mouseX, mouseY, this.width - 38, 10, 28, 28)) {
                this.minecraft.setScreen(new CapeSettingsScreen(this));
                return true;
            }

            if (isInside(
                    mouseX, mouseY,
                    gridLeft, gridTop,
                    TILE_COLUMNS * TILE_STEP - TILE_GAP,
                    gridBottom - gridTop
            )) {
                List<CapeEntry> visibleCapes = getVisibleCapes();

                for (int i = 0; i < visibleCapes.size(); i++) {
                    int column = i % TILE_COLUMNS;
                    int row = i / TILE_COLUMNS;
                    int x = gridLeft + column * TILE_STEP;
                    int y = gridTop + row * TILE_ROW_HEIGHT - scrollOffset;

                    if (y + TILE_SIZE + 18 <= gridTop || y >= gridBottom) {
                        continue;
                    }

                    if (isInside(mouseX, mouseY, x, y, TILE_SIZE, TILE_SIZE + 18)) {
                        CapeEntry cape = visibleCapes.get(i);
                        selectedCape = cape.id();
                        CapeSelection.setSelectedCape(selectedCape);
                        return true;
                    }
                }

                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void openUploadPicker() {
        if (CustomCapeManager.getCapeCount() >= CustomCapeManager.MAX_CAPES) {
            if (this.minecraft.player != null) {
                this.minecraft.player.connection.sendChat(
                        "You have reached the maximum of "
                                + CustomCapeManager.MAX_CAPES
                                + " custom capes."
                );
            }
            return;
        }

        Thread pickerThread = new Thread(() -> {
            String script = "Add-Type -AssemblyName System.Windows.Forms; "
                    + "$dialog = New-Object System.Windows.Forms.OpenFileDialog; "
                    + "$dialog.Title = 'Upload Custom Cape'; "
                    + "$dialog.Filter = 'PNG Images (*.png)|*.png'; "
                    + "$dialog.Multiselect = $false; "
                    + "if ($dialog.ShowDialog() -eq 'OK') { "
                    + "[Console]::WriteLine($dialog.FileName) "
                    + "}";

            try {
                Process process = new ProcessBuilder(
                        "powershell.exe",
                        "-NoProfile",
                        "-STA",
                        "-Command",
                        script
                ).start();

                String selectedPath;

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                )) {
                    selectedPath = reader.readLine();
                }

                int exitCode = process.waitFor();

                if (exitCode != 0 || selectedPath == null || selectedPath.isBlank()) {
                    return;
                }

                File file = new File(selectedPath);

                if (!file.isFile()
                        || !file.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                    return;
                }

                this.minecraft.execute(() -> {
                    if (this.minecraft.screen == this) {
                        this.minecraft.setScreen(
                                new CustomCapeNameScreen(this, file.toPath())
                        );
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Cape-File-Picker");

        pickerThread.setDaemon(true);
        pickerThread.start();
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
