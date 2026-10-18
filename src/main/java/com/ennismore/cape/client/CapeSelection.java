package com.ennismore.cape.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CapeSelection {

    private static final Logger LOGGER = LoggerFactory.getLogger("Cape");

    private static final Path CONFIG_FILE =
            FabricLoader.getInstance().getConfigDir().resolve("cape-cape-selection.json");

    private static String selectedCape = load();

    static {
        logSelectedCape();
    }

    private CapeSelection() {
    }

    public static String getSelectedCape() {
        return selectedCape;
    }

    public static void setSelectedCape(String cape) {
        if (cape == null || cape.equals(selectedCape)) {
            return;
        }

        selectedCape = cape;
        save();
        logSelectedCape();
    }

    private static void logSelectedCape() {
        String name = switch (selectedCape) {
            case "default" -> "Default";
            case "none" -> "No";
            case "cape" -> "Cape";
            case "migrator" -> "Migrator";
            case "cherry_blossom" -> "Cherry Blossom";
            case "mojang_studios" -> "Mojang Studios";
            case "purple_heart" -> "Purple Heart";
            case "15th_anniversary" -> "15th Anniversary";
            default -> selectedCape;
        };

        LOGGER.info("{} cape selected!", name);
    }

    private static String load() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                String value = Files.readString(CONFIG_FILE).trim();

                if (value.matches("default|none|cape|migrator|cherry_blossom|mojang_studios|purple_heart|15th_anniversary")) {
                    return value;
                }
            }
        } catch (IOException ignored) {
        }

        return "default";
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.writeString(CONFIG_FILE, selectedCape);
        } catch (IOException ignored) {
        }
    }
}
