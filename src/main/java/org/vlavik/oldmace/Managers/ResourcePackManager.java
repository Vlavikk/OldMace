package org.vlavik.oldmace.Managers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.vlavik.oldmace.Config.Config;
import org.vlavik.oldmace.Config.ResourcePackConfig;
import org.vlavik.oldmace.OldMace;
import org.vlavik.oldmace.Utils.HashUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ResourcePackManager implements Listener {
    private final ResourcePackConfig resourcePackConfig;

    private final String remoteURLPack = "https://www.dropbox.com/scl/fi/8nkllljsod9jhg9d8yarc/OldMacePack.zip?rlkey=zqydig3cha3b29qqcsliz5w6q&st=oq9a4fht&dl=1";

    private final String PACK_NAME = "OldMacePack.zip";
    private ResourcePackServer packServer;

    private final byte[] packHash;

    public ResourcePackManager(Plugin plugin) throws Exception {
        resourcePackConfig = new ResourcePackConfig(Config.getYaml());
        if (Type.valueOf(resourcePackConfig.getType()) == Type.LOCAL_SERVER){
            File packFile = new File(plugin.getDataFolder(), PACK_NAME);
            plugin.saveResource(PACK_NAME, true);

            packHash = HashUtils.getSha1FromFile(packFile);
            packServer = new ResourcePackServer(plugin,resourcePackConfig.getLocalPort(),PACK_NAME);
            packServer.start();
        }else packHash = HashUtils.getSha1FromUrl(remoteURLPack);

    }

    public boolean isEnableResourcePack(){
        return resourcePackConfig.isEnable();
    }

    public void sendResourcePackToPlayer(Player player){
        if (isEnableResourcePack()){
            Type type = Type.valueOf(resourcePackConfig.getType());
            if (type == Type.REMOTE_SERVER){
                player.setResourcePack(remoteURLPack);
            }else {
                String url = "http://" + resourcePackConfig.getLocalIP() + ":" + resourcePackConfig.getLocalPort() + "/" + PACK_NAME;
                OldMace.getInstance().getServer().getScheduler().runTaskLater(OldMace.getInstance(), () -> {
                    if (player.isOnline()) {
                        player.setResourcePack(url,packHash);
                    }
                }, 10L);
            }
        }
    }


    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (!resourcePackConfig.isForce()) return;
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        switch (status) {
            case DECLINED:
                event.getPlayer().kickPlayer(
                        ChatColor.RED + "Для игры на сервере обязателен ресурс-пак!\n\n" +
                                ChatColor.WHITE + "В списке серверов выберите этот сервер,\n" +
                                ChatColor.WHITE + "нажмите 'Настроить' и установите:\n" +
                                ChatColor.YELLOW + "Наборы ресурсов: Включены"
                );
                break;
            case FAILED_DOWNLOAD:
                event.getPlayer().kickPlayer(
                        ChatColor.RED + "Ошибка при загрузке ресурс-пака.\n\n" +
                                ChatColor.WHITE + "Пожалуйста, перезайдите на сервер."
                );
                break;
            case ACCEPTED:
                break;
            case SUCCESSFULLY_LOADED:
                break;
        }
    }

    public enum Type{
        LOCAL_SERVER,
        REMOTE_SERVER
    }

    public ResourcePackServer getPackServer() {
        return packServer;
    }

    public class ResourcePackServer {

        private HttpServer server;
        private final Plugin plugin;
        private final int port;
        private final String fileName;

        public ResourcePackServer(Plugin plugin, int port, String fileName) {
            this.plugin = plugin;
            this.port = port;
            this.fileName = fileName;
        }

        public void start() {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);

                server.createContext("/" + fileName, new PackHandler(plugin, fileName));

                server.setExecutor(null);
                server.start();
                plugin.getLogger().info("Pack server with port: " + port);
            } catch (IOException e) {
                plugin.getLogger().severe("The resource pack web server could not be started: " + e.getMessage());
            }
        }

        public void stop() {
            if (server != null) {
                server.stop(0);
                plugin.getLogger().info("The resource pack web server has been stopped");
            }
        }

        private class PackHandler implements HttpHandler {
            private final Plugin plugin;
            private final String fileName;

            public PackHandler(Plugin plugin, String fileName) {
                this.plugin = plugin;
                this.fileName = fileName;
            }

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                File packFile = new File(plugin.getDataFolder(), fileName);

                if (!packFile.exists()) {
                    String response = "Resource pack not found on server.";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, packFile.length());

                try (OutputStream os = exchange.getResponseBody();
                     FileInputStream fs = new FileInputStream(packFile)) {

                    final byte[] buffer = new byte[1024 * 64];
                    int count;
                    while ((count = fs.read(buffer)) >= 0) {
                        os.write(buffer, 0, count);
                    }
                }
            }
        }
    }
}
