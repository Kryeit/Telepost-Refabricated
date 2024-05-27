package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.commands.completion.SuggestionsProvider;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.kryeit.telepost.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.function.Supplier;

public class Visit {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player))  {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post closestPost = new Post(player.getPos());
        String postNameOrPlayer = StringArgumentType.getString(context, "name");
        String postID = Utils.nameToId(postNameOrPlayer);

        Text text;

        if (!closestPost.isInside(player.getPos())) {
            text = TelepostMessages.getMessage(player, "telepost.standing", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }
        
        ServerPlayerEntity visited = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(postNameOrPlayer);

        // /visit Player
        if (visited != null) {
            if (Utils.isInvited(visited, player) || Permissions.check(source, "telepost.visit.others", false)) {
                Optional<HomePost> home = Telepost.getDB().getHome(visited.getUuid());
                if (home.isEmpty()) {
                    text = TelepostMessages.getMessage(player, "telepost.no_homepost", Formatting.RED);
                    player.sendMessage(text);
                    return 0;
                }
                Post homePost = new Post(home.get());

                if (closestPost.isSame(homePost)) {
                    text = TelepostMessages.getMessage(player, "telepost.already-there", Formatting.RED);
                    player.sendMessage(text, true);
                    return 0;
                }

                text = TelepostMessages.getMessage(player, "telepost.teleport.homepost.other", Formatting.GREEN, visited.getName().getString());
                player.sendMessage(text, true);

                homePost.teleport(player);
                return Command.SINGLE_SUCCESS;
            } else {
                text = TelepostMessages.getMessage(player, "telepost.no_invite", Formatting.RED);
                player.sendMessage(text, true);
                return 0;
            }
        }

        // /visit NamedPost
        Optional<NamedPost> namedPostOptional = Telepost.getDB().getNamedPost(postID);

        if (namedPostOptional.isPresent()) {
            Post namedPost = new Post(namedPostOptional.get());

            if (closestPost.isSame(namedPost)) {
                text = TelepostMessages.getMessage(player, "telepost.already-there", Formatting.RED);
                player.sendMessage(text, true);
                return 0;
            }

            text = TelepostMessages.getMessage(player, "telepost.teleport.named_post", Formatting.GREEN, namedPostOptional.get().name());
            player.sendMessage(text, true);

            namedPost.teleport(player);
            return Command.SINGLE_SUCCESS;
        }

        text = TelepostMessages.getMessage(player, "telepost.unknown_post", Formatting.RED);
        player.sendMessage(text, true);
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("visit")
                .requires(source -> Permissions.check(source, "telepost.visit", true))
                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                        .suggests(SuggestionsProvider.suggestPostNamesAndOnlinePlayers())
                        .executes(Visit::execute)
                )
        );

        dispatcher.register(CommandManager.literal("v")
                .requires(source -> Permissions.check(source, "telepost.visit", true))
                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                        .suggests(SuggestionsProvider.suggestPostNamesAndOnlinePlayers())
                        .executes(Visit::execute)
                )
        );
    }
}
