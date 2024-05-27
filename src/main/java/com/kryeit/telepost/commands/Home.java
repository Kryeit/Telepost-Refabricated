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

import java.util.Optional;
import java.util.function.Supplier;

public class Home {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player)) {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());

        Text text;

        if (!post.isInside(player.getPos())) {
            text = TelepostMessages.getMessage(player, "telepost.standing", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        Optional<HomePost> home = Telepost.getDB().getHome(player.getUuid());

        if (home.isPresent()) {
            Post homePost = new Post(home.get());

            if (post.isSame(homePost)) {
                text = TelepostMessages.getMessage(player, "telepost.already-there", Formatting.RED);
                player.sendMessage(text, true);
                return 0;
            }

            text = TelepostMessages.getMessage(player, "telepost.teleport.homepost", Formatting.GREEN);
            player.sendMessage(text, true);
            homePost.teleport(player);
        } else {
            text = TelepostMessages.getMessage(player, "telepost.no_homepost", Formatting.RED);
            player.sendMessage(text, true);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("home")
                .requires(source -> Permissions.check(source, "telepost.homepost", true))
                .executes(Home::execute)
        );

        dispatcher.register(CommandManager.literal("h")
                .requires(source -> Permissions.check(source, "telepost.homepost", true))
                .executes(Home::execute)
        );
    }
}
