package com.kryeit.telepost.offlines;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;

import java.io.File;
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
        UserCache userCache = MinecraftServerSupplier.getServer().getUserCache();
        if (userCache == null) return "Unknown";

        Optional<GameProfile> gameProfile = userCache.getByUuid(id);
        if (gameProfile.isEmpty()) return "Unknown";
        return gameProfile.get().getName();
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
