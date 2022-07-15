package net.frozenorb.potpvp.rematch;

import lombok.experimental.UtilityClass;
import net.frozenorb.potpvp.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.DARK_PURPLE;
import static org.bukkit.ChatColor.GREEN;

@UtilityClass
public final class RematchItems {

    public static final ItemStack REQUEST_REMATCH_ITEM = new ItemStack(Material.DIAMOND);
    public static final ItemStack SENT_REMATCH_ITEM = new ItemStack(Material.DIAMOND);
    public static final ItemStack ACCEPT_REMATCH_ITEM = new ItemStack(Material.DIAMOND);

    static {
        ItemUtil.setDisplayName(REQUEST_REMATCH_ITEM, DARK_PURPLE + "Request Rematch");
        ItemUtil.setDisplayName(SENT_REMATCH_ITEM, GREEN + "Sent Rematch");
        ItemUtil.setDisplayName(ACCEPT_REMATCH_ITEM, GREEN + "Accept Rematch");
    }
}