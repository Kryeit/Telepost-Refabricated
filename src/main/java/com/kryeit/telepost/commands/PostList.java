package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.kryeit.telepost.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class PostList {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        List<NamedPost> posts = Telepost.getInstance().database.getNamedPosts();

        if (posts.isEmpty()) {
            player.sendMessage(TelepostMessages.getMessage(player, "telepost.postlist.no-posts", Formatting.RED), false);
            return 1;
        }
        Collections.sort(posts);

        int pages = (int) Math.ceil((double) posts.size() / 10);
        int page;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException e) {
            page = 1;
        }

        if(page > pages || page < 1) {
            player.sendMessage(TelepostMessages.getMessage(player, "telepost.postlist.no-pages", Formatting.RED), false);
            return 1;
        }

        // Header
        player.sendMessage(Text.literal("------------").formatted(Formatting.DARK_GRAY), false);
        player.sendMessage(TelepostMessages.getMessage(player, "telepost.postlist.header", Formatting.GOLD), false);
        player.sendMessage(Text.literal("------------").formatted(Formatting.DARK_GRAY), false);

        int startIndex = (page - 1) * 10;
        int endIndex = Math.min(startIndex + 10, posts.size());

        for (int i = startIndex; i < startIndex + 10; i++) {
            if (i < endIndex) {
                NamedPost post = posts.get(i);
                String name = post.name();
                MutableText postText = Text.literal((i + 1) + ". ").formatted(Formatting.WHITE)
                        .append(getListEntry(post, name, player));
                player.sendMessage(postText, false);
            } else {
                player.sendMessage(Text.literal(""), false);
            }
        }

        // Footer - Pagination Arrows
        MutableText paginationText = TelepostMessages.getMessage(player, "telepost.postlist.total", Formatting.GRAY, posts.size()).copy()
                .append(Text.literal(" | ").formatted(Formatting.GRAY));

        if (hasPreviousPage(page)) {
            int finalPage1 = page;
            paginationText.append(Text.literal("<<").styled(style ->
                    style.withColor(Formatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/postlist " + (finalPage1 - 1)))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TelepostMessages.getMessage(player, "telepost.postlist.page.previous", Formatting.GRAY)))
            ));
        } else {
            paginationText.append(Text.literal("<<").formatted(Formatting.GRAY));
        }

        paginationText.append(Text.literal(" - ").formatted(Formatting.GREEN));

        if (hasNextPage(page, pages)) {
            int finalPage = page;
            paginationText.append(Text.literal(">>").styled(style ->
                    style.withColor(Formatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/postlist " + (finalPage + 1)))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TelepostMessages.getMessage(player, "telepost.postlist.page.next", Formatting.GRAY)))
            ));
        } else {
            paginationText.append(Text.literal(">>").formatted(Formatting.GRAY));
        }

        paginationText.append(Text.literal(" | ").formatted(Formatting.GRAY))
                .append(
                TelepostMessages.getMessage(player, "telepost.postlist.page", Formatting.GRAY, page, pages
        ));

        player.sendMessage(paginationText, false);


        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("postlist")
                .requires(source -> Permissions.check(source, "telepost.postlist", true))
                .executes(PostList::execute)
                .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                        .executes(PostList::execute))
        );
    }

    public static boolean hasPreviousPage(int currentPage) {
        return currentPage > 1;
    }

    public static boolean hasNextPage(int currentPage, int maxPages) {
        return currentPage < maxPages;
    }

    public static Text getListEntry(NamedPost post, String name, ServerPlayerEntity player) {
        return Text.literal(name).formatted(Utils.isPostNamedByAdmin(post) ? Formatting.GOLD : Formatting.WHITE)
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/v " + name))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        TelepostMessages.getMessage(player, "telepost.postlist.tooltip", Formatting.GRAY, new Post(post).getStringCoords(), name).copy().append(
                                                Text.literal("\nNamed by ").append(
                                                        Utils.getNamedPostOwner(post)
                                                ).formatted(Formatting.GRAY, Formatting.ITALIC))
                                        )));
    }

}

