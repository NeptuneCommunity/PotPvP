package net.frozenorb.potpvp.match;

import lombok.experimental.UtilityClass;
import net.frozenorb.potpvp.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public final class SpectatorItems {

    public static final ItemStack SHOW_SPECTATORS_ITEM = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
    public static final ItemStack HIDE_SPECTATORS_ITEM = new ItemStack(Material.INK_SACK, 1, DyeColor.LIME.getDyeData());

    public static final ItemStack VIEW_INVENTORY_ITEM = new ItemStack(Material.BOOK);

    // these items both do the same thing but we change the name if
    // clicking the item will reuslt in the player being removed
    // from their party. both serve the function of returning a player
    // to the lobby.
    // https://github.com/FrozenOrb/PotPvP-SI/issues/37
    public static final ItemStack RETURN_TO_LOBBY_ITEM = new ItemStack(Material.FIRE);
    public static final ItemStack LEAVE_PARTY_ITEM = new ItemStack(Material.FIRE);

    static {
        ItemUtil.setDisplayName(SHOW_SPECTATORS_ITEM, ChatColor.YELLOW + "Show spectators");
        ItemUtil.setDisplayName(HIDE_SPECTATORS_ITEM, ChatColor.YELLOW + "Hide spectators");

        ItemUtil.setDisplayName(VIEW_INVENTORY_ITEM, ChatColor.YELLOW + "View player inventory");

        ItemUtil.setDisplayName(RETURN_TO_LOBBY_ITEM, ChatColor.YELLOW + "Return to lobby");
        ItemUtil.setDisplayName(LEAVE_PARTY_ITEM, ChatColor.YELLOW + "Leave party");
    }
}