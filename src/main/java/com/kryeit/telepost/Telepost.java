package com.kryeit.telepost;

import com.kryeit.telepost.autonaming.MonthlyCheckRunnable;
import com.kryeit.telepost.commands.*;
import com.kryeit.telepost.commands.cooldown.CooldownStorage;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.listeners.ServerTick;
import com.kryeit.telepost.post.StructureHandler;
import com.kryeit.telepost.storage.IDatabase;
import com.kryeit.telepost.storage.LevelDBImpl;
import com.kryeit.telepost.storage.NamedPostStorage;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import static com.kryeit.telepost.compat.BlueMapImpl.markerSet;
import static com.kryeit.telepost.post.Post.WORLD;

public class Telepost implements DedicatedServerModInitializer {

    private static final Timer MONTHLY_TIMER = new Timer(true);

    public static Telepost instance;
    public static final String ID = "telepost";
    public static final String NAME = "Telepost";

    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public IDatabase database;
    public static NamedPostStorage playerNamedPosts;
    public static Map<UUID, UUID> invites = new HashMap<>();

    public static CooldownStorage randomPostCooldown;
    public static boolean postBuilding = false;

    @Override
    public void onInitializeServer() {
        try {
            LOGGER.info("Reading config file...");
            ConfigReader.readFile(Path.of("config/" + ID));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initializeDatabases();
        instance = this;
        registerCommands();
        registerDisableEvent();
        registerEvents();
        registerMonthlyCheck();

        // Comment this out in dev environment
        StructureHandler.createStructures();
    }

    public void registerMonthlyCheck() {
        if (!ConfigReader.AUTONAMING) return;

        long interval = Duration.ofHours(1).toMillis();
        MONTHLY_TIMER.schedule(new MonthlyCheckRunnable(), interval, interval);
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicatedServer, commandFunction) -> {
            NearestPost.register(dispatcher);
            SetHome.register(dispatcher);
            Home.register(dispatcher);
            NamePost.register(dispatcher);
            UnnamePost.register(dispatcher);
            Invite.register(dispatcher);
            Visit.register(dispatcher);
            BuildPosts.register(dispatcher);
            PostList.register(dispatcher);
            ForceVisit.register(dispatcher);
            PostHelp.register(dispatcher);
            PrivatePost.register(dispatcher);

            // TODO: It's broken, fix it
            //RandomPost.register(dispatcher);

            if (CompatAddon.GRIEF_DEFENDER.isLoaded()) {
                DeletePostClaim.register(dispatcher);
            }
            
            //CommandDumpDB.register(dispatcher);
        });
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(new ServerTick());
        BlueMapAPI.onEnable(api ->
                api.getWorld(WORLD).ifPresent(blueWorld -> {
                    BlueMapImpl.loadMarkerSet();
                    for (BlueMapMap map : blueWorld.getMaps()) {
                        map.getMarkerSets().put("posts", markerSet);
                    }
                })
        );
    }

    public void initializeDatabases() {
        // Database of all posts and homeposts, with their locations and such
        database = new LevelDBImpl();

        // For /randompost cooldown
        try {
            randomPostCooldown = new CooldownStorage("world/" + ID + "/randompostcooldown");
            playerNamedPosts = new NamedPostStorage("world/" + ID, "playerPosts.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerDisableEvent() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            database.stop();
        });
    }

    public static Telepost getInstance() {
        return instance;
    }

    public static IDatabase getDB() {
        return getInstance().database;
    }
}
