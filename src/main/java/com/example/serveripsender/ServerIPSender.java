package com.example.serveripsender;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;

public class ServerIPSender extends JavaPlugin {
    private final Gson gson = new Gson();
    @Override
    public void onEnable() {
        saveDefaultConfig();

        String ipPort = fetchFromPterodactyl();
        if (ipPort == null || ipPort.isBlank()) {
            Server server = getServer();
            String ip = server.getIp();
            int port = server.getPort();
            ipPort = ip + ":" + port;
        }

        saveToFile(ipPort);
        sendToVelocity(ipPort);
    }

    private String fetchFromPterodactyl() {
        String baseUrl = getConfig().getString("pterodactyl-url");
        String token = getConfig().getString("auth-token");
        String serverId = String.valueOf(getConfig().get("server-id"));

        if (baseUrl == null || baseUrl.isBlank() ||
                token == null || token.isBlank() ||
                serverId == null || serverId.isBlank()) {
            return null;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/client/servers/" + serverId + "/network/allocations"))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                getLogger().warning("Failed to fetch allocation info from Pterodactyl: HTTP " + response.statusCode());
                return null;
            }

            JsonObject obj = gson.fromJson(response.body(), JsonObject.class);
            JsonArray data = obj.getAsJsonArray("data");
            if (data == null) return null;
            for (JsonElement el : data) {
                JsonObject attr = el.getAsJsonObject().getAsJsonObject("attributes");
                if (attr != null && attr.get("is_default").getAsBoolean()) {
                    String ip = attr.has("alias") && !attr.get("alias").isJsonNull()
                            ? attr.get("alias").getAsString() : attr.get("ip").getAsString();
                    int port = attr.get("port").getAsInt();
                    return ip + ":" + port;
                }
            }
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error contacting Pterodactyl API", ex);
        }

        return null;
    }

    private void saveToFile(String ipPort) {
        File dataDir = getDataFolder();
        File file = new File(dataDir, "server-ip.txt");
        try {
            if (!dataDir.exists()) {
                if (!dataDir.mkdirs()) {
                    getLogger().warning("Could not create plugin data folder");
                    return;
                }
            }
            Files.writeString(file.toPath(), ipPort, StandardCharsets.UTF_8);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not write server IP", e);
        }
    }

    private void sendToVelocity(String ipPort) {
        Plugin velocity = getServer().getPluginManager().getPlugin("VelocityAPI");
        if (velocity == null || !velocity.isEnabled()) {
            getLogger().warning("VelocityAPI plugin not found or not enabled");
            return;
        }

        String channel = "velocity:api";
        getServer().getMessenger().registerOutgoingPluginChannel(this, channel);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ServerAddress");
        out.writeUTF(ipPort);
        getServer().sendPluginMessage(this, channel, out.toByteArray());
    }
}

