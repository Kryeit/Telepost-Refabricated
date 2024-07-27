package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.kryeit.telepost.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.Supplier;

public class PrivatePost {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player)) {
            Supplier<Text>  message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());

        Optional<NamedPost> namedPost = post.getNamedPost();
        if (namedPost.isEmpty())
            return 0;

        if (Telepost.playerNamedPosts.getPlayerForPost(namedPost.get().id()).equals(player.getUuid())) {

            boolean wasPrivate = namedPost.get().isPrivate();
            String id = namedPost.get().id();
            String name = namedPost.get().name();

            Telepost.getDB().deleteNamedPost(namedPost.get().id());
            Telepost.getDB().addNamedPost(new NamedPost(
                    id,
                    name,
                    post.getPos(),
                    !wasPrivate));

            player.sendMessage(Text.literal("The post is now " + (namedPost.get().isPrivate() ? "public" : "private")), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("privatepost")
                .requires(source -> Permissions.check(source, "telepost.privatepost", true))
                .executes(PrivatePost::execute)
        );
    }
}
