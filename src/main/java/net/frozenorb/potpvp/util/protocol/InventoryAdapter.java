package net.frozenorb.potpvp.util.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.util.protocol.event.PlayerCloseInventoryEvent;
import net.frozenorb.potpvp.util.protocol.event.PlayerOpenInventoryEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InventoryAdapter extends PacketAdapter {

    private static final Set<UUID> currentlyOpen = new HashSet<>();

    public InventoryAdapter() {
        super(PotPvPSI.getInstance(), PacketType.Play.Client.CLIENT_COMMAND, PacketType.Play.Client.CLOSE_WINDOW);
    }

    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();
        if (packet.getType() == PacketType.Play.Client.CLIENT_COMMAND && packet.getClientCommands().size() != 0 && packet.getClientCommands().read(0) == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
            if (!currentlyOpen.contains(player.getUniqueId())) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(PotPvPSI.getInstance(), () -> Bukkit.getPluginManager().callEvent(new PlayerOpenInventoryEvent(player)));
            }
            currentlyOpen.add(player.getUniqueId());
        } else if (packet.getType() == PacketType.Play.Client.CLOSE_WINDOW) {
            if (currentlyOpen.contains(player.getUniqueId())) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(PotPvPSI.getInstance(), () -> Bukkit.getPluginManager().callEvent(new PlayerCloseInventoryEvent(player)));
            }
            currentlyOpen.remove(player.getUniqueId());
        }
    }

    public static Set<UUID> getCurrentlyOpen() {
        return currentlyOpen;
    }
}