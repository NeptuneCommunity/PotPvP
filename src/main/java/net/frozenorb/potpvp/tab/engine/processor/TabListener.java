package net.frozenorb.potpvp.tab.engine.processor;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.tab.engine.TabHandler;
import net.frozenorb.potpvp.tab.engine.TabLayout;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TabListener implements Listener {

    private final PotPvPSI plugin;

    public TabListener(PotPvPSI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        new BukkitRunnable() {
            public void run() {
                TabHandler.addPlayer(event.getPlayer());
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler
    public void onPlayerLeave(final PlayerQuitEvent event) {
        TabHandler.removePlayer(event.getPlayer());
        TabLayout.remove(event.getPlayer());
    }
}
