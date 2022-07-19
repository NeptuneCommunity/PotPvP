package net.frozenorb.potpvp.tab.engine;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.tab.engine.misc.TabUtils;

public class TabAdapter extends PacketAdapter {

    public TabAdapter(PotPvPSI plugin) {
        super(plugin, PacketType.Play.Server.PLAYER_INFO);
    }


    public void onPacketSending(final PacketEvent event) {
        if (TabHandler.getLayoutProvider() != null) {

            final PacketContainer packetContainer = event.getPacket();

            final String name = packetContainer.getStrings().read(1);
            final boolean isOurs = packetContainer.getStrings().read(1).startsWith("$");
            final int action = packetContainer.getIntegers().read(0);

            if (!isOurs) {
                if (!TabUtils.is18(event.getPlayer())) {
                    if (action != 4) {
                        event.setCancelled(true);
                    }
                }
            } else {
                packetContainer.getStrings().write(1, name.replace("$", ""));
            }
        }
    }
}