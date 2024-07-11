package com.kryeit.telepost.storage;

import com.kryeit.telepost.storage.bytes.HomePost;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.kryeit.telepost.storage.bytes.ReadableByteArray;
import com.kryeit.telepost.storage.bytes.WritableByteArray;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.kryeit.telepost.Telepost.ID;

public class LevelDBImpl implements IDatabase {
    private final DB postsDB;
    private final DB homesDB;

    public LevelDBImpl() {
        postsDB = createDB("posts");
        homesDB = createDB("homes");
    }

    private static DB createDB(String name) {
        Options options = new Options();
        options.createIfMissing(true);
        options.cacheSize(4_194_304); // 4 MB

        try {
            return Iq80DBFactory.factory.open(new File("world/" + ID + "/db/" + name), options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            postsDB.close();
            homesDB.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<HomePost> getHome(UUID playerID) {
        WritableByteArray key = new WritableByteArray(16);
        key.writeUUID(playerID);
        byte[] bytes = homesDB.get(key.toByteArray());
        return bytes == null ? Optional.empty() : Optional.of(HomePost.fromBytes(new ReadableByteArray(bytes)));
    }

    @Override
    public Optional<NamedPost> getNamedPost(String id) {
        WritableByteArray key = new WritableByteArray();
        key.writeString(id.toLowerCase());
        byte[] bytes = postsDB.get(key.toByteArray());
        return bytes == null ? Optional.empty() : Optional.of(NamedPost.fromBytes(id, new ReadableByteArray(bytes)));
    }

    @Override
    public String dump() {
        StringBuilder builder = new StringBuilder();
        builder.append("Posts:\n");
        for (NamedPost post : getNamedPosts()) {
            builder.append(MessageFormat.format(" -{0}: {1} at {2}\n", post.id(), post.name(), post.location()));
        }
        builder.append('\n');

        builder.append("Homes:\n");
        try (DBIterator iterator = homesDB.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                HomePost homePost = HomePost.fromBytes(new ReadableByteArray(iterator.peekNext().getValue()));
                builder.append(MessageFormat.format(" -{0}: {1}\n", homePost.playerID(), homePost.location()));
            }
        } catch (IOException ignored) {
        }
        return builder.toString();
    }

    @Override
    public List<NamedPost> getNamedPosts() {
        List<NamedPost> posts = new ArrayList<>();
        try (DBIterator iterator = postsDB.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String id = new ReadableByteArray(iterator.peekNext().getKey()).readString();
                posts.add(NamedPost.fromBytes(id, new ReadableByteArray(iterator.peekNext().getValue())));
            }
        } catch (IOException ignored) {
        }
        return posts;
    }

    @Override
    public void addNamedPost(NamedPost post) {
        WritableByteArray key = new WritableByteArray();
        key.writeString(post.id());
        postsDB.put(key.toByteArray(), post.toBytes());
    }

    @Override
    public void deleteNamedPost(String id) {
        WritableByteArray key = new WritableByteArray();
        key.writeString(id.toLowerCase());
        postsDB.delete(key.toByteArray());
    }

    @Override
    public void setHome(UUID playerID, HomePost homePost) {
        WritableByteArray key = new WritableByteArray(16);
        key.writeUUID(playerID);
        homesDB.put(key.toByteArray(), homePost.toBytes());
    }
}
