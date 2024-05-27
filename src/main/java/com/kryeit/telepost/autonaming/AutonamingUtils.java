package com.kryeit.telepost.autonaming;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.kryeit.telepost.utils.Utils;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.kryeit.telepost.Telepost.LOGGER;
import static com.kryeit.telepost.config.ConfigReader.POST_NAMES;

public class AutonamingUtils {

    public static String getRandom() {
        List<String> availableNames = new ArrayList<>();

        for (String name : POST_NAMES) {
            if (Telepost.getDB().getNamedPost(Utils.nameToId(name)).isPresent())
                continue;

            availableNames.add(name);
        }

        if (availableNames.isEmpty()) {
            LOGGER.warn("Config file for Telepost doesn't have enough names for autonaming.");
            return null;
        }

        return availableNames.get((int) (Math.random() * availableNames.size()));
    }

    public static void autonamePost() {
        SpiralIterator iterator = new SpiralIterator();
        while (iterator.hasNext()) {
            Post post = iterator.next();
            if (post.isNamed()) {
                continue;
            }

            String name = getRandom();

            if (name == null) {
                return;
            }

            Telepost.getDB().addNamedPost(new NamedPost(Utils.nameToId(name), name, post.getPos()));
            Utils.executeCommandAsServer("/setworldspawn " + post.getX() + " " + (post.getY() + 1) + " " + post.getZ());

            // Broadcast to all players
            MinecraftServerSupplier.getServer().getPlayerManager().broadcast(
                    TelepostMessages.getMessage(null, "telepost.autonamed", Formatting.GREEN, name, post.getStringCoords()),
                    false
            );
            LOGGER.info("[Monthly Autonaming] Named post " + name + " at " + post.getStringCoords());
            return;
        }
        LOGGER.warn("Config file for Telepost doesn't have enough names for autonaming.");
    }
}
