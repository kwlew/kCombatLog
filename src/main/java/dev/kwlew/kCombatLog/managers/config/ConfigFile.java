package dev.kwlew.kCombatLog.managers.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigFile {

    private final JavaPlugin plugin;
    private final String name;

    private File file;
    private FileConfiguration config;

    public ConfigFile(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), name);

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        applyDefaults();
    }

    private void applyDefaults() {
        InputStream stream = plugin.getResource(name);
        if (stream == null) return;

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            config.setDefaults(YamlConfiguration.loadConfiguration(reader));
            config.options().copyDefaults(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        save();
    }

    public FileConfiguration get() {
        return config;
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
        applyDefaults();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}