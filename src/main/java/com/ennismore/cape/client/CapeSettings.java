
package com.ennismore.cape.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Properties;

public final class CapeSettings {

    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir()
                    .resolve("cape-settings.properties");

    public enum View {
        GRID,
        LIST
    }

    public enum Filter {
        ALL,
        UNOBTAINABLE,
        LIMITED_TIME,
        CUSTOM
    }

    private static View view = View.GRID;
    private static Filter filter = Filter.ALL;
    private static String defaultCape = "default";

    private CapeSettings() {
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(FILE)) {
            properties.load(input);

            view = parseEnum(
                    View.class,
                    properties.getProperty("view"),
                    View.GRID
            );

            filter = parseEnum(
                    Filter.class,
                    properties.getProperty("filter"),
                    Filter.ALL
            );

            defaultCape = properties.getProperty("defaultCape", "default");

            if (defaultCape == null || defaultCape.isBlank()) {
                defaultCape = "default";
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void save() {
        Properties properties = new Properties();

        properties.setProperty("view", view.name());
        properties.setProperty("filter", filter.name());
        properties.setProperty("defaultCape", defaultCape);

        try {
            Files.createDirectories(FILE.getParent());

            try (OutputStream output = Files.newOutputStream(FILE)) {
                properties.store(output, "Cape Settings");
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static <T extends Enum<T>> T parseEnum(
            Class<T> type,
            String value,
            T fallback
    ) {
        if (value == null) {
            return fallback;
        }

        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    public static View getView() {
        return view;
    }

    public static void setView(View newView) {
        view = newView;
        save();
    }

    public static Filter getFilter() {
        return filter;
    }

    public static void setFilter(Filter newFilter) {
        filter = newFilter;
        save();
    }

    public static String getDefaultCape() {
        return defaultCape;
    }

    public static void setDefaultCape(String capeId) {
        if (capeId == null || capeId.isBlank()) {
            return;
        }

        defaultCape = capeId;
        save();
    }
}
