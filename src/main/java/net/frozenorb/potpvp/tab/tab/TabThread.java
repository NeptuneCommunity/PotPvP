package net.frozenorb.potpvp.tab.tab;

import net.frozenorb.potpvp.PotPvPSI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TabThread extends Thread {

    private final Plugin commonLibs = Bukkit.getServer().getPluginManager().getPlugin("CommonLibs");
    private final Plugin protocolLib = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");

    public TabThread() {
        this.setName("PotPvPSI - Tab Thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {

        while (PotPvPSI.getInstance().isEnabled() && canRun()) {
            for (Player online : PotPvPSI.getInstance().getServer().getOnlinePlayers()) {
                try {
                    TabHandler.updatePlayer(online);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(250L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean canRun() {
        if (commonLibs == null) {
            return protocolLib != null && protocolLib.isEnabled();
        } else {
            return commonLibs.isEnabled();
        }
    }
}