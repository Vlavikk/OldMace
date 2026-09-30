package org.vlavik.oldmace.Config;

import org.bukkit.configuration.file.YamlConfiguration;

public class ResourcePackConfig {
    private final boolean enable;
    private final boolean force;
    private final String type;
    private final int localPort;
    private final String localIP;


    public ResourcePackConfig(YamlConfiguration yaml){
        String path = "resource-pack.";
        enable = yaml.getBoolean(path+"enable");
        force = yaml.getBoolean(path+"force");
        type = yaml.getString(path+"type");
        localPort = yaml.getInt(path+"local-server-port");
        localIP = yaml.getString(path+"local-server-ip");
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isForce() {
        return force;
    }

    public String getType() {
        return type;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getLocalIP() {
        return localIP;
    }
}
