package net.frozenorb.potpvp.nametag.framework;

import lombok.NoArgsConstructor;
import net.frozenorb.potpvp.PotPvPSI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

@NoArgsConstructor
public final class NametagListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (NametagHandler.isInitiated()) {
            event.getPlayer().setMetadata("PotPvPSINametag-LoggedIn", new FixedMetadataValue(PotPvPSI.getInstance(), true));
            NametagHandler.initiatePlayer(event.getPlayer());
            NametagHandler.reloadPlayer(event.getPlayer());
            NametagHandler.reloadOthersFor(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().removeMetadata("PotPvPSINametag-LoggedIn", PotPvPSI.getInstance());
        NametagHandler.getTeamMap().remove(event.getPlayer().getUniqueId());
    }
}
