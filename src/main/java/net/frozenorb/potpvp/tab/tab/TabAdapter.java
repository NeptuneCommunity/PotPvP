package net.frozenorb.potpvp.tab.tab;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.frozenorb.potpvp.PotPvPSI;
import net.minecraft.server.v1_7_R4.*;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public class TabAdapter extends PacketAdapter {

    private static Field playerField;
    private static Field namedEntitySpawnField;

    public TabAdapter() {
        super(PotPvPSI.getInstance(), PacketType.Play.Server.PLAYER_INFO, PacketType.Play.Server.NAMED_ENTITY_SPAWN);
    }

    public void onPacketSending(PacketEvent event) {
        if (TabHandler.getLayoutProvider() == null || !this.shouldForbid(event.getPlayer())) {
            return;
        }
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
            PacketContainer packetContainer = event.getPacket();
            String name = packetContainer.getStrings().read(0);
            boolean isOurs = packetContainer.getStrings().read(0).startsWith("$");
            int action = packetContainer.getIntegers().read(1);
            if (!isOurs) {
                if (action != 4 && this.shouldCancel(event.getPlayer(), event.getPacket())) {
                    //event.setCancelled(true);
                }
            } else {
                packetContainer.getStrings().write(0, name.replace("$", ""));
            }
        } else if (event.getPacketType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN && TabUtils.is18(event.getPlayer()) && Bukkit.getPluginManager().getPlugin("UHC") == null) {
            PacketPlayOutNamedEntitySpawn packet = (PacketPlayOutNamedEntitySpawn) event.getPacket().getHandle();
            GameProfile gameProfile;
            try {
                gameProfile = (GameProfile) TabAdapter.namedEntitySpawnField.get(packet);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            final Player[] bukkitPlayer = new Player[1];
            Bukkit.getScheduler().runTask(PotPvPSI.getInstance(), () -> {
                bukkitPlayer[0] = Bukkit.getPlayer(gameProfile.getId());
                if (bukkitPlayer[0] != null) {
                    ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(PacketPlayOutPlayerInfo.removePlayer(((CraftPlayer) bukkitPlayer[0]).getHandle()));
                }
            });
        }
    }

    private boolean shouldCancel(Player player, PacketContainer packetContainer) {
        if (!TabUtils.is18(player)) {
            return true;
        }
        PacketPlayOutPlayerInfo playerInfoPacket = (PacketPlayOutPlayerInfo) packetContainer.getHandle();
        EntityPlayer recipient = ((CraftPlayer) player).getHandle();
        UUID tabPacketPlayer;
        try {
            tabPacketPlayer = ((GameProfile) TabAdapter.playerField.get(playerInfoPacket)).getId();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Player bukkitPlayer = Bukkit.getPlayer(tabPacketPlayer);
        if (bukkitPlayer == null) {
            return true;
        }
        EntityTrackerEntry trackerEntry = (EntityTrackerEntry) ((WorldServer) ((CraftPlayer) bukkitPlayer).getHandle().getWorld()).getTracker().trackedEntities.get(bukkitPlayer.getEntityId());
        return trackerEntry == null || !trackerEntry.trackedPlayers.contains(recipient);
    }

    private boolean shouldForbid(Player player) {
        Tab playerTab = TabHandler.getTabs().get(player.getUniqueId());
        return playerTab != null && playerTab.isInitiated();
    }

    static {
        try {
            (TabAdapter.playerField = PacketPlayOutPlayerInfo.class.getDeclaredField("player")).setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            (TabAdapter.namedEntitySpawnField = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("b")).setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}