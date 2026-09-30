package org.vlavik.oldmace.Config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.vlavik.oldmace.OldMace;

import java.io.File;

public class Config {

    private final Plugin plugin;
    private static YamlConfiguration config;
    public Config(Plugin plugin){
        this.plugin = plugin;
        load();
    }
    public void load() {
        File file = new File(OldMace.getInstance().getDataFolder(), "config.yml");

        if(!file.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = new YamlConfiguration();

        try {
            config.load(file);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static YamlConfiguration getYaml() {
        return config;
    }
}
