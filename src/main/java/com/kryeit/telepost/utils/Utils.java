package com.kryeit.telepost.utils;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.offlines.Offlines;
import com.kryeit.telepost.post.GridIterator;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.kryeit.telepost.post.Post.WORLD;

public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
    }
    public static boolean isInOverworld(ServerPlayerEntity player) {
        return player.getWorld().equals(WORLD);
    }
    public static boolean isInvited(ServerPlayerEntity owner, ServerPlayerEntity invited) {
        if (!Telepost.invites.containsKey(invited.getUuid())) return false;

        return Telepost.invites.get(invited.getUuid()).equals(owner.getUuid());
    }
    public static void loadChunk(World world, int chunkX, int chunkZ) {
        ChunkManager chunkManager = world.getChunkManager();
        chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    public static boolean isPostNamedByAdmin(NamedPost post) {
        return Telepost.playerNamedPosts.hasPost(post.id());
    }

    public static String getNamedPostOwner(NamedPost post) {
        UUID id = Telepost.playerNamedPosts.getPlayerForPost(post.id());

        if (id == null) return "Admin";

        return Offlines.getNameByUUID(id);
    }

    public static List<Post> getPosts() {
        GridIterator iterator = new GridIterator();
        List<Post> posts = new ArrayList<>();

        while (iterator.hasNext()) {
            Vec3d loc = iterator.next();

            Post post = new Post(loc);
            posts.add(post);
            if (!post.isNamed()) {

            }
        }

        return posts;
    }

    public static List<Post> getUnnamedPosts() {
        List<Post> unnamed = getPosts();
        unnamed.removeIf(Post::isNamed);
        return unnamed;
    }

    public static void executeCommandAsServer(String command) {
        // Create a command source that represents the server
        ServerCommandSource source = MinecraftServerSupplier.getServer().getCommandSource();

        // Execute the command
        MinecraftServerSupplier.getServer().getCommandManager().executeWithPrefix(source, command);
    }

    public static boolean check(ServerCommandSource source, String permission, boolean defaultValue) {
        User user = LuckPermsProvider.get().getUserManager().getUser(source.getPlayer().getUuid());

        if (user == null) {
            return defaultValue;
        }

        return user.getNodes(NodeType.PERMISSION).stream()
                .filter(NodeType.PERMISSION::matches)
                .map(NodeType.PERMISSION::cast)
                .anyMatch(node -> node.getPermission().equalsIgnoreCase(permission) && node.getValue());
    }
}
