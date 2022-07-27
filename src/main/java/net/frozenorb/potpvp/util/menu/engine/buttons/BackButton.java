package net.frozenorb.potpvp.util.menu.engine.buttons;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.util.menu.engine.Button;
import net.frozenorb.potpvp.util.menu.engine.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class BackButton extends Button {

    private final Menu menu;

    public BackButton(Menu menu) {
        this.menu = menu;
    }


    @Override
    public String getName(Player player) {
        return ChatColor.GOLD + "Go Back";
    }

    @Override
    public List<String> getDescription(Player player) {
        return null;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.BED;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        PotPvPSI.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(PotPvPSI.getInstance(), () -> {
            if (menu == null) {
                player.closeInventory();
            } else {
                menu.openMenu(player);
            }
        }, 1L);
    }
}