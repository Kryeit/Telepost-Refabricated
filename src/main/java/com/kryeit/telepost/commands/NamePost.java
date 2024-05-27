package com.kryeit.telepost.commands;

import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.compat.GriefDefenderImpl;
import com.kryeit.telepost.post.Post;
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

import static com.kryeit.telepost.compat.GriefDefenderImpl.NEEDED_CLAIMBLOCKS;

public class NamePost {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player)) {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());
        String postName = StringArgumentType.getString(context, "name");
        String postID = Utils.nameToId(postName);

        Text text;

        // Check if nearest named
        if (post.isNamed()) {
            text = TelepostMessages.getMessage(player, "telepost.already_named", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        // Check if name is in use
        Optional<NamedPost> namedPost = Telepost.getDB().getNamedPost(postID);
        if (namedPost.isPresent()) {
            text = TelepostMessages.getMessage(player, "telepost.already_named", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        // Check if player has already named a post
        if (Telepost.playerNamedPosts.hasPlayer(player.getUuid()) && !Utils.check(source, "telepost.namepost", false)) {
            text = TelepostMessages.getMessage(player, "telepost.already_named", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        if (CompatAddon.GRIEF_DEFENDER.isLoaded()) {
            if (GriefDefenderImpl.getClaimBlocks(player.getUuid()) < NEEDED_CLAIMBLOCKS) {
                text = TelepostMessages.getMessage(player, "telepost.name.claimblocks", Formatting.RED, NEEDED_CLAIMBLOCKS);
                player.sendMessage(text);
                return 0;
            }

            Claim claim = GriefDefenderImpl.getClaim(post);
            if (claim != null) {
                player.sendMessage(Text.literal("You've been granted manager trust in the post claim"));
                claim.addUserTrust(player.getUuid(), TrustTypes.MANAGER);
            }
        }

        if (!Utils.check(source, "telepost.namepost", false)){
            Telepost.playerNamedPosts.assignPostToPlayer(postID, player.getUuid());
        }

        Telepost.getDB().addNamedPost(new NamedPost(postID, postName, post.getPos()));

        text = TelepostMessages.getMessage(player, "telepost.named", Formatting.GREEN, postName, post.getStringCoords());
        player.sendMessage(text);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("namepost")
                .requires(source -> Permissions.check(source, "telepost.namepost", true) || source.hasPermissionLevel(4))
                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                        .executes(NamePost::execute)
                )
        );
    }
}
