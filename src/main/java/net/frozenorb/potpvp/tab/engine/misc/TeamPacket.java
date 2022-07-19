package net.frozenorb.potpvp.tab.engine.misc;

import net.minecraft.server.v1_7_R4.PacketPlayOutScoreboardTeam;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class TeamPacket {

    private final PacketPlayOutScoreboardTeam packet;

    public TeamPacket(String name, String prefix, String suffix, Collection players, int paramInt) {
        this.packet = new PacketPlayOutScoreboardTeam();

        this.setField("a", name);
        this.setField("f", paramInt);

        if (paramInt == 0 || paramInt == 2) {
            this.setField("b", name);
            this.setField("c", prefix);
            this.setField("d", suffix);
            this.setField("g", 0);
        }

        if (paramInt == 0) {
            this.addAll(players);
        }
    }

    public TeamPacket(String name, Collection players, int paramInt) {
        this.packet = new PacketPlayOutScoreboardTeam();

        if (players == null) {
            players = new ArrayList();
        }

        this.setField("g", 0);
        this.setField("a", name);
        this.setField("f", paramInt);
        this.addAll(players);
    }

    public void sendToPlayer(Player bukkitPlayer) {
        ((CraftPlayer) bukkitPlayer).getHandle().playerConnection.sendPacket(this.packet);
    }

    public void setField(String field, Object value) {
        try {
            Field fieldObject = this.packet.getClass().getDeclaredField(field);
            fieldObject.setAccessible(true);
            fieldObject.set(this.packet, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAll(Collection col) {
        try {
            Field fieldObject = this.packet.getClass().getDeclaredField("e");
            fieldObject.setAccessible(true);
            ((Collection) fieldObject.get(this.packet)).addAll(col);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
