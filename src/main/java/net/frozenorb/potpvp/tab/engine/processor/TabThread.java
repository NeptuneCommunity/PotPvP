package net.frozenorb.potpvp.tab.engine.processor;

import net.frozenorb.potpvp.tab.engine.TabHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TabThread extends BukkitRunnable {

    private final Plugin protocolLib;

    public TabThread() {
        this.protocolLib = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");
    }

    @Override
    public void run() {
        while (this.protocolLib != null && this.protocolLib.isEnabled()) {
            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                try {
                    TabHandler.updatePlayer(online);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(250L);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }

}
