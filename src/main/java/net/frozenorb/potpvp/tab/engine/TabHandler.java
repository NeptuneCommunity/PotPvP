package net.frozenorb.potpvp.tab.engine;

import com.google.common.base.Preconditions;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.tab.engine.misc.LayoutProvider;
import net.frozenorb.potpvp.tab.engine.processor.TabListener;
import net.frozenorb.potpvp.tab.engine.processor.TabThread;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.HttpAuthenticationService;
import net.minecraft.util.com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.util.com.mojang.authlib.properties.PropertyMap;
import net.minecraft.util.com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TabHandler {

    private static boolean initiated;
    private static final AtomicReference<Object> defaultPropertyMap;
    private static LayoutProvider layoutProvider;
    private static Map<String, Tab> tabs;

    public static void init(PotPvPSI plugin) {
        Preconditions.checkState(!TabHandler.initiated);
        TabHandler.initiated = true;
        getDefaultPropertyMap();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new TabThread());
        plugin.getServer().getPluginManager().registerEvents(new TabListener(plugin), plugin);
    }

    public static void setLayoutProvider(final LayoutProvider provider) {
        TabHandler.layoutProvider = provider;
    }

    public static void addPlayer(final Player player) {
        TabHandler.tabs.put(player.getName(), new Tab(player));
    }

    public static void updatePlayer(Player player) {
        if (TabHandler.tabs.containsKey(player.getName())) {
            if (player.isOnline()) {
                TabHandler.tabs.get(player.getName()).update();
            }
        }
    }

    public static void removePlayer(final Player player) {
        TabHandler.tabs.remove(player.getName());
    }

    private static PropertyMap fetchSkin() {
        final GameProfile profile = new GameProfile(UUID.fromString("6b22037d-c043-4271-94f2-adb00368bf16"), "bananasquad");
        final HttpAuthenticationService authenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        final MinecraftSessionService sessionService = authenticationService.createMinecraftSessionService();
        final GameProfile profile2 = sessionService.fillProfileProperties(profile, true);
        return profile2.getProperties();
    }

    public static PropertyMap getDefaultPropertyMap() {
        Object value = TabHandler.defaultPropertyMap.get();
        if (value == null) {
            synchronized (TabHandler.defaultPropertyMap) {
                value = TabHandler.defaultPropertyMap.get();
                if (value == null) {
                    final PropertyMap actualValue = fetchSkin();
                    value = ((actualValue == null) ? TabHandler.defaultPropertyMap : actualValue);
                    TabHandler.defaultPropertyMap.set(value);
                }
            }
        }
        return (PropertyMap) ((value == TabHandler.defaultPropertyMap) ? null : value);
    }

    public static LayoutProvider getLayoutProvider() {
        return TabHandler.layoutProvider;
    }

    static {
        TabHandler.initiated = false;
        defaultPropertyMap = new AtomicReference<>();
        TabHandler.tabs = new ConcurrentHashMap<>();
    }
}