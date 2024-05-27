package com.kryeit.telepost.commands;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.compat.GriefDefenderImpl;
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
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.function.Supplier;

public class DeletePostClaim {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player))  {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Text text;

        String postID = Telepost.playerNamedPosts.getPostIDForPlayer(player.getUuid());

        if (postID == null) {
            text = TelepostMessages.getMessage(player, "telepost.unknown_post", Formatting.RED);
            player.sendMessage(text, true);
        }

        Optional<NamedPost> namedPostOptional = Telepost.getDB().getNamedPost(postID);

        if (namedPostOptional.isPresent()) {
            Post namedPost = new Post(namedPostOptional.get());
            Claim claim = GriefDefenderImpl.getClaim(namedPost);

            if (claim == null) return 0;
            GriefDefender.getCore().getClaimManager(GriefDefenderImpl.getWorldUUID()).deleteClaim(claim);

            text = Text.literal("Post claim deleted").formatted(Formatting.GREEN);
            player.sendMessage(text, true);
            return Command.SINGLE_SUCCESS;
        }

        text = TelepostMessages.getMessage(player, "telepost.unknown_post", Formatting.RED);
        player.sendMessage(text, true);
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("deletepostclaim")
                .requires(source -> Permissions.check(source, "telepost.deletepostclaim", true))
                .executes(DeletePostClaim::execute)
        );
    }
}
