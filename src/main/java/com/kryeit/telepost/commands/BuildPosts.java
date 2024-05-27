package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.compat.GriefDefenderImpl;
import com.kryeit.telepost.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Supplier;

import static com.kryeit.telepost.config.ConfigReader.WORLDBORDER;

public class BuildPosts {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Supplier<Text> message;

        if (player == null || !Utils.isInOverworld(player)) {
            message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        if (WORLDBORDER > 2_000_000) {
            player.sendMessage(Text.of("Your worldboder is " + WORLDBORDER + ", and would take too long to build posts. Select in config/telepost/config.yml a smaller border."));
            return 0;
        }

        if (CompatAddon.GRIEF_DEFENDER.isLoaded()) {
            player.sendMessage(Text.of("Posts are starting to build and claim (GriefDefender is Loaded)"));
            GriefDefenderImpl.createClaimGroup();
        } else {
            player.sendMessage(Text.of("Posts are starting to build"));
        }

        Telepost.postBuilding = true;
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("buildposts")
                .requires(source -> Permissions.check(source, "telepost.buildposts", false))
                .executes(BuildPosts::execute)
        );
    }
}
