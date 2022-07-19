package net.frozenorb.potpvp.tab.engine.misc;

import com.comphenix.protocol.ProtocolLibrary;
import net.frozenorb.potpvp.tab.engine.TabHandler;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TabUtils {

    private static Map<String, GameProfile> cache;

    public static boolean is18(Player player) {
        return ProtocolLibrary.getProtocolManager().getProtocolVersion(player) > 20;
    }

    public static GameProfile getOrCreateProfile(String name, UUID id) {
        GameProfile player = TabUtils.cache.get(name);
        if (player == null) {
            player = new GameProfile(id, name);
            player.getProperties().putAll(Objects.requireNonNull(TabHandler.getDefaultPropertyMap()));
            TabUtils.cache.put(name, player);
        }
        return player;
    }

    public static GameProfile getOrCreateProfile(String name) {
        return getOrCreateProfile(name, new UUID(new Random().nextLong(), new Random().nextLong()));
    }

    static {
        TabUtils.cache = new ConcurrentHashMap<>();
    }

}