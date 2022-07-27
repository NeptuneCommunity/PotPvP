package net.frozenorb.potpvp.tab.engine.misc;

import net.minecraft.server.v1_7_R4.EnumGamemode;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class PlayerInfoPacketMod {
    private final PacketPlayOutPlayerInfo packet;

    public PlayerInfoPacketMod(final String name, final int ping, final GameProfile profile, final int action, boolean is18) {
        this.packet = new PacketPlayOutPlayerInfo();

        if (is18) {
            this.setField("username", name);
            this.setField("ping", ping);
            this.setField("action", action);
            this.setField("gamemode18", EnumGamemode.SURVIVAL.getId());
            this.setField("gamemode", 0);
            this.setField("playerName", profile.getName());
            this.setField("playerUUID", profile.getId());
            this.setField("propertyMap", profile.getProperties());
            return;
        }


        this.setField("username", name);
        this.setField("ping", ping);
        this.setField("action", action);
        this.setField("gamemode", 0);
        this.setField("propertyMap", profile.getProperties());
    }

    public void setField(final String field, final Object value) {
        try {
            final Field fieldObject = this.packet.getClass().getDeclaredField(field);
            fieldObject.setAccessible(true);
            fieldObject.set(this.packet, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToPlayer(final Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(this.packet);
    }
}
