package com.kryeit.telepost.commands.completion;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class SuggestionsProvider {
    public static SuggestionProvider<ServerCommandSource> suggestOnlinePlayers() {
        return (context, builder) -> suggestMatchingPlayerNames(builder, MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList());
    }

    public static SuggestionProvider<ServerCommandSource> suggestPostNamesAndOnlinePlayers() {
        return (context, builder) -> {
            suggestMatchingPlayerNames(builder, MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList());
            suggestPostNames(builder);
            return builder.buildFuture();
        };
    }

    private static CompletableFuture<Suggestions> suggestMatchingPlayerNames(SuggestionsBuilder builder, Collection<ServerPlayerEntity> players) {
        String remaining = builder.getRemaining().toLowerCase();

        players.stream()
                .map(player -> player.getName().getString())
                .filter(name -> name.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static void suggestPostNames(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        for (NamedPost namedPost : Telepost.getDB().getNamedPosts()) {
            if (namedPost.name().toLowerCase().startsWith(remaining) && !namedPost.isPrivate()) {
                builder.suggest(namedPost.name());
            }
        }
    }
}
