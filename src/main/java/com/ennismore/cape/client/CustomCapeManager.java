package com.ennismore.cape.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mojang.blaze3d.platform.NativeImage;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;

import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CustomCapeManager {

    public static final int MAX_CAPES = 15;

    private static final Path DIRECTORY =
            FabricLoader.getInstance().getConfigDir().resolve("cape/custom");

    private static final Path METADATA =
            FabricLoader.getInstance().getConfigDir().resolve("cape/custom_capes.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<CustomCape> CAPES = new ArrayList<>();
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();

    private static boolean loaded;

    private CustomCapeManager() {
    }

    public static void load() {
        if (loaded) {
            return;
        }

        loaded = true;
        CAPES.clear();
        TEXTURES.clear();

        try {
            Files.createDirectories(DIRECTORY);

            if (!Files.exists(METADATA)) {
                return;
            }

            JsonArray array = JsonParser.parseString(
                    Files.readString(METADATA)
            ).getAsJsonArray();

            for (int i = 0; i < array.size() && CAPES.size() < MAX_CAPES; i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                String id = object.get("id").getAsString();
                String name = object.get("name").getAsString();
                Path file = DIRECTORY.resolve(id + ".png").normalize();

                if (!file.getParent().equals(DIRECTORY.normalize())
                        || !id.matches("custom_[1-9][0-9]*")
                        || !Files.isRegularFile(file)) {
                    continue;
                }

                try {
                    registerTexture(id, file);
                    CAPES.add(new CustomCape(id, name));
                } catch (IOException | RuntimeException ignored) {
                }
            }
        } catch (IOException | RuntimeException ignored) {
        }
    }

    public static List<CustomCape> getCapes() {
        load();
        return List.copyOf(CAPES);
    }

    public static int getCapeCount() {
        load();
        return CAPES.size();
    }

    public static boolean isCustomCape(String id) {
        load();
        return CAPES.stream().anyMatch(cape -> cape.id().equals(id));
    }

    public static Identifier getTexture(String id) {
        load();
        return TEXTURES.get(id);
    }

    public static Optional<CustomCape> addCape(Path source, String requestedName)
            throws IOException {
        load();

        if (CAPES.size() >= MAX_CAPES) {
            throw new IllegalStateException("You have reached the 15 custom cape limit.");
        }

        if (!Files.isRegularFile(source)
                || !source.getFileName().toString().toLowerCase().endsWith(".png")) {
            throw new IllegalArgumentException("Please select a PNG image.");
        }

        try (InputStream input = Files.newInputStream(source);
             NativeImage image = NativeImage.read(input)) {
            if (image.getWidth() != 64 || image.getHeight() != 32) {
                throw new IllegalArgumentException(
                        "Cape images must be exactly 64×32 pixels."
                );
            }
        }

        int number = 1;
        boolean exists;

        do {
            exists = false;

            for (CustomCape cape : CAPES) {
                if (cape.id().equals("custom_" + number)) {
                    exists = true;
                    number++;
                    break;
                }
            }
        } while (exists);

        String id = "custom_" + number;
        String name = requestedName == null ? "" : requestedName.trim();

        if (name.isEmpty()) {
            name = "Custom #" + number;
        }

        if (name.length() > 15) {
            throw new IllegalArgumentException("Cape names cannot exceed 15 characters.");
        }

        Path destination = DIRECTORY.resolve(id + ".png");
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

        try {
            registerTexture(id, destination);
        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(destination);
            throw exception;
        }

        CustomCape cape = new CustomCape(id, name);
        CAPES.add(cape);
        saveMetadata();

        return Optional.of(cape);
    }

    public static boolean deleteCape(String id) throws IOException {
        load();

        if (id == null || !id.matches("custom_[1-9][0-9]*")) {
            return false;
        }

        CustomCape cape = CAPES.stream()
                .filter(entry -> entry.id().equals(id))
                .findFirst()
                .orElse(null);

        if (cape == null) {
            return false;
        }

        Path file = DIRECTORY.resolve(id + ".png").normalize();

        if (!file.getParent().equals(DIRECTORY.normalize())) {
            return false;
        }

        Files.deleteIfExists(file);

        CAPES.remove(cape);

        Identifier texture = TEXTURES.remove(id);

        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(texture);
        }

        try {
            saveMetadata();
        } catch (IOException ignored) {
        }

        return true;
    }

    private static void registerTexture(String id, Path file) throws IOException {
        NativeImage image;

        try (InputStream input = Files.newInputStream(file)) {
            image = NativeImage.read(input);
        }

        if (image.getWidth() != 64 || image.getHeight() != 32) {
            image.close();
            throw new IOException("Invalid cape dimensions. Must be 64×32.");
        }

        Identifier texture = Identifier.fromNamespaceAndPath(
                "cape",
                "custom/" + id
        );

        DynamicTexture dynamicTexture =
                new DynamicTexture(() -> "Cape " + id, image);

        Minecraft.getInstance().getTextureManager().register(
                texture,
                dynamicTexture
        );

        TEXTURES.put(id, texture);
    }

    private static void saveMetadata() throws IOException {
        JsonArray array = new JsonArray();

        for (CustomCape cape : CAPES) {
            JsonObject object = new JsonObject();
            object.addProperty("id", cape.id());
            object.addProperty("name", cape.name());
            array.add(object);
        }

        Files.createDirectories(METADATA.getParent());
        Files.writeString(METADATA, GSON.toJson(array));
    }

    public record CustomCape(String id, String name) {
    }
}
