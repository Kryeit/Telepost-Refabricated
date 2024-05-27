package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.utils.Utils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class RandomPost {

    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player)) {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Text text;

        if (!new Post(player.getPos()).isInside(player.getPos())) {
            text = TelepostMessages.getMessage(player, "telepost.standing", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        if (Telepost.randomPostCooldown.hasPlayer(player.getUuid())) {
            text = TelepostMessages.getMessage(player, "telepost.randompost.cooldown", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        // Choose a post at random from the list
        List<Post> posts = Utils.getUnnamedPosts();
        Post post = posts.get((int) (Math.random() * posts.size()));

        try {
            Telepost.randomPostCooldown.addPlayer(player.getUuid());
        } catch (IOException e) {
            e.printStackTrace();
        }

        post.teleport(player);

        return 1;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("randompost")
                .requires(source -> Permissions.check(source, "telepost.randompost", true))
                .executes(RandomPost::execute)
        );
    }
}
