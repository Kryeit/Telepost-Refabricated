package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.HomePost;
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

import java.util.function.Supplier;

public class SetHome {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player)) {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());

        Text text = TelepostMessages.getMessage(player, "telepost.homepost", Formatting.GREEN, post.getStringCoords());

        Telepost.getDB().setHome(player.getUuid(), new HomePost(player.getUuid(), post.getPos()));

        player.sendMessage(text);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sethome")
                .requires(source -> Permissions.check(source, "telepost.setpost", true))
                .executes(SetHome::execute)
        );

        dispatcher.register(CommandManager.literal("setpost")
                .requires(source -> Permissions.check(source, "telepost.setpost", true))
                .executes(SetHome::execute)
        );
    }
}
