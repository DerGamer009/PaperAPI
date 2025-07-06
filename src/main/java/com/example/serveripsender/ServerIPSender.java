package com.example.serveripsender;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;

public class ServerIPSender extends JavaPlugin {
    @Override
    public void onEnable() {
        Server server = getServer();
        String ip = server.getIp();
        int port = server.getPort();
        String ipPort = ip + ":" + port;

        saveToFile(ipPort);
        sendToVelocity(ipPort);
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

