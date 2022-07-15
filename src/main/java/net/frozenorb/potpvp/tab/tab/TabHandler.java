package net.frozenorb.potpvp.tab.tab;

import com.google.common.base.Preconditions;
import net.frozenorb.potpvp.PotPvPSI;
import net.minecraft.util.com.mojang.authlib.properties.PropertyMap;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TabHandler {

    private static boolean initiated = false;
    private static final AtomicReference<Object> propertyMapSerializer = new AtomicReference<>();
    private static final AtomicReference<Object> defaultPropertyMap = new AtomicReference<>();
    private static LayoutProvider layoutProvider;
    private static final Map<UUID, Tab> tabs = new ConcurrentHashMap<>();

    public static void init() {
        if (PotPvPSI.getInstance().getConfig().getBoolean("disableTab", false)) {
            return;
        }
        Preconditions.checkState((!initiated));
        initiated = true;
        TabHandler.getDefaultPropertyMap();
        new TabThread().start();
        PotPvPSI.getInstance().getServer().getPluginManager().registerEvents(new TabListener(), PotPvPSI.getInstance());
    }

    public static void setLayoutProvider(LayoutProvider provider) {
        layoutProvider = provider;
    }

    protected static void addPlayer(Player player) {
        tabs.put(player.getUniqueId(), new Tab(player));
    }

    protected static void updatePlayer(Player player) {
        if (tabs.containsKey(player.getUniqueId())) {
            tabs.get(player.getUniqueId()).update();
        }
    }

    protected static void removePlayer(Player player) {
        tabs.remove(player.getUniqueId());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PropertyMap getDefaultPropertyMap() {
        Object value = defaultPropertyMap.get();
        if (value == null) {
            synchronized (defaultPropertyMap) {
                value = defaultPropertyMap.get();
                if (value == null) {
                    defaultPropertyMap.set(defaultPropertyMap);
                }
            }
        }
        return (PropertyMap)(value == defaultPropertyMap ? null : value);
    }

    public static LayoutProvider getLayoutProvider() {
        return layoutProvider;
    }

    public static Map<UUID, Tab> getTabs() {
        return tabs;
    }

}
