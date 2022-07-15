package net.frozenorb.potpvp.tab.tab;

import net.frozenorb.potpvp.PotPvPSI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TabListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        new BukkitRunnable() {
            public void run() {
                TabHandler.addPlayer(event.getPlayer());
            }
        }.runTaskLater(PotPvPSI.getInstance(), 5);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        TabHandler.removePlayer(event.getPlayer());
        TabLayout.remove(event.getPlayer());
    }
}