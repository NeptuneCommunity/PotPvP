package net.frozenorb.potpvp.queue;

import lombok.experimental.UtilityClass;
import net.frozenorb.potpvp.util.ItemUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.*;

@UtilityClass
public final class QueueItems {

    public static final ItemStack JOIN_SOLO_UNRANKED_QUEUE_ITEM = new ItemStack(Material.IRON_SWORD);
    public static final ItemStack LEAVE_SOLO_UNRANKED_QUEUE_ITEM = new ItemStack(Material.INK_SACK, 1, (byte) DyeColor.RED.getDyeData());

    public static final ItemStack JOIN_SOLO_RANKED_QUEUE_ITEM = new ItemStack(Material.DIAMOND_SWORD);
    public static final ItemStack LEAVE_SOLO_RANKED_QUEUE_ITEM = new ItemStack(Material.INK_SACK, 1, (byte) DyeColor.RED.getDyeData());

    public static final ItemStack JOIN_PARTY_UNRANKED_QUEUE_ITEM = new ItemStack(Material.IRON_SWORD);
    public static final ItemStack LEAVE_PARTY_UNRANKED_QUEUE_ITEM = new ItemStack(Material.ARROW);

    public static final ItemStack JOIN_PARTY_RANKED_QUEUE_ITEM = new ItemStack(Material.DIAMOND_SWORD);
    public static final ItemStack LEAVE_PARTY_RANKED_QUEUE_ITEM = new ItemStack(Material.ARROW);

    static {
        ItemUtil.setDisplayName(JOIN_SOLO_UNRANKED_QUEUE_ITEM, YELLOW + "Play Unranked");
        ItemUtil.setDisplayName(LEAVE_SOLO_UNRANKED_QUEUE_ITEM, RED + "Leave Unranked Queue");

        ItemUtil.setDisplayName(JOIN_SOLO_RANKED_QUEUE_ITEM, GOLD + "Play Ranked");
        ItemUtil.setDisplayName(LEAVE_SOLO_RANKED_QUEUE_ITEM, RED + "Leave Ranked Queue");

        ItemUtil.setDisplayName(JOIN_PARTY_UNRANKED_QUEUE_ITEM, BLUE + "Play 2v2 Unranked");
        ItemUtil.setDisplayName(LEAVE_PARTY_UNRANKED_QUEUE_ITEM, RED + "Leave 2v2 Unranked Queue");

        ItemUtil.setDisplayName(JOIN_PARTY_RANKED_QUEUE_ITEM, DARK_AQUA + "Join 2v2 Ranked");
        ItemUtil.setDisplayName(LEAVE_PARTY_RANKED_QUEUE_ITEM, RED + "Leave 2v2 Ranked Queue");
    }

}