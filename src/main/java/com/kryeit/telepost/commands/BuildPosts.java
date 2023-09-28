package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostPermissions;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.compat.CompatAddon;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Supplier;

import static com.kryeit.telepost.post.GridIterator.WORLDBORDER;

public class BuildPosts {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Supplier<Text> message;

        if (player == null || !Utils.isInOverworld(player) || !TelepostPermissions.isAdmin(player)) {
            message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        if (CompatAddon.GRIEF_DEFENDER.isLoaded()) {
            player.sendMessage(Text.of("Posts are starting to build and claim (GriefDefender is Loaded)"));
        } else {
            player.sendMessage(Text.of("Posts are starting to build"));
        }

        if (WORLDBORDER > 1_000_000) {
            player.sendMessage(Text.of("Your worldboder is " + WORLDBORDER + " please set your worldborder with /worldborder set <diameter>"));
            return 0;
        }

        Telepost.postBuilding = true;

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("buildposts")
                .executes(BuildPosts::execute)
        );
    }
}
