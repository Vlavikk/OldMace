package org.vlavik.oldmace;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.vlavik.oldmace.Commands.MaceCommand;
import org.vlavik.oldmace.Config.Config;
import org.vlavik.oldmace.Listeners.PlayerJoinListener;
import org.vlavik.oldmace.Mace.MaceExecutor;
import org.vlavik.oldmace.Mace.MaceFixer;
import org.vlavik.oldmace.Managers.MaceManager;
import org.vlavik.oldmace.Managers.ResourcePackManager;

public final class OldMace extends JavaPlugin {

    private static OldMace main;
    private static MaceManager MACE_MANAGER;
    private static ResourcePackManager RESOURCE_PACK_MANAGER;

    @Override
    public void onEnable() {
        main = this;
        new Config(this);

        MACE_MANAGER = new MaceManager();

        try {
            RESOURCE_PACK_MANAGER = new ResourcePackManager(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getCommand("oldmace").setExecutor(new MaceCommand());
        getCommand("oldmace").setTabCompleter(new MaceCommand());

        Bukkit.getPluginManager().registerEvents(new MaceExecutor(),this);
        Bukkit.getPluginManager().registerEvents(new MaceFixer(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this);
        Bukkit.getPluginManager().registerEvents(RESOURCE_PACK_MANAGER,this);
    }

    @Override
    public void onDisable() {
        ResourcePackManager.ResourcePackServer packServer = RESOURCE_PACK_MANAGER.getPackServer();
        if (packServer != null) packServer.stop();
    }

    public static MaceManager getMaceManager() {
        return MACE_MANAGER;
    }

    public static ResourcePackManager getResourcePackManager() {
        return RESOURCE_PACK_MANAGER;
    }

    public static OldMace getInstance() {
        return main;
    }
}
