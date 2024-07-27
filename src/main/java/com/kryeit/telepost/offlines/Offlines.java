package com.kryeit.telepost.offlines;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kryeit.telepost.MinecraftServerSupplier;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Offlines {

    public static UUID getUUIDbyName(String name) {
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);
        if (player != null) return player.getUuid();
        UserCache userCache = MinecraftServerSupplier.getServer().getUserCache();
        if (userCache == null) return null;
        Optional<GameProfile> gameProfile = userCache.findByName(name);
        return gameProfile.map(GameProfile::getId).orElse(null);
    }

    public static String getNameByUUID(UUID id) {
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(id);
        if (player != null) {
            return player.getName().getString();
        }

        String uuidString = id.toString().replace("-", ""); // Remove hyphens from UUID
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuidString;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    return jsonObject.get("name").getAsString();
                }
            } else if (responseCode == 204) {
                return "";
            } else {
                throw new RuntimeException("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public static List<String> getPlayerNames() {
        List<String> players = new ArrayList<>();
        File playerDataDirectory = new File("world/playerdata/");

        File[] playerDataFiles = playerDataDirectory.listFiles();

        if (playerDataFiles == null) return List.of();

        for (File playerDataFile : playerDataFiles) {
            String fileName = playerDataFile.getName();
            if (!fileName.endsWith(".dat")) continue;
            UUID id = UUID.fromString(fileName.substring(0, fileName.length() - 4));
            players.add(getNameByUUID(id));
        }
        return players;
    }
}
