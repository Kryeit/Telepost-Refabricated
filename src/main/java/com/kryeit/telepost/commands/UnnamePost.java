package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.kryeit.telepost.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class UnnamePost {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Supplier<Text> message;

        if (player == null || !Utils.isInOverworld(player)) {
            message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());
        String postName = StringArgumentType.getString(context, "name");
        String postID = Utils.nameToId(postName);

        Optional<NamedPost> namedPost = Telepost.getDB().getNamedPost(postID);
        if (namedPost.isEmpty()) {
            message = () -> Text.literal("The nearest post is not named");
            source.sendFeedback(message, false);
            return 0;
        }

        Telepost.playerNamedPosts.revokePost(postID);

        Telepost.getDB().deleteNamedPost(postID);

        message = () -> Text.literal(
                "The nearest post has been unnamed at: " + post.getStringCoords());

        source.sendFeedback(message, false);

        if (CompatAddon.BLUEMAP.isLoaded()) {
            BlueMapImpl.removeMarker(postName);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        List<String> suggestions = new ArrayList<>();

        for (NamedPost namedPost : Telepost.getDB().getNamedPosts()) {
            suggestions.add(namedPost.name());
        }

        SuggestionProvider<ServerCommandSource> suggestionProvider = (context, builder) ->
                CommandSource.suggestMatching(suggestions, builder);

        dispatcher.register(CommandManager.literal("unnamepost")
                .requires(source -> Permissions.check(source, "telepost.unnamepost", false) || source.hasPermissionLevel(4))
                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                        .suggests(suggestionProvider)
                        .executes(UnnamePost::execute)
                )
        );
    }
}
