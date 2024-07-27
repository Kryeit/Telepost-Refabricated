package com.kryeit.telepost.offlines;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;

import javax.json.Json;
import javax.json.JsonObject;
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
        if (player != null) return player.getName().getString();

        // Fetch mojang session server and get the player name
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + id.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            JsonReader reader = Json.createReader(br);
            JsonObject jsonObject = reader.readObject();
            connection.disconnect();
            return jsonObject.getString("name");
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
