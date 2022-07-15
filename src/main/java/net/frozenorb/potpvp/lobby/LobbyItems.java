package net.frozenorb.potpvp.lobby;

import lombok.experimental.UtilityClass;
import net.frozenorb.potpvp.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static net.frozenorb.potpvp.PotPvPLang.LEFT_ARROW;
import static net.frozenorb.potpvp.PotPvPLang.RIGHT_ARROW;
import static org.bukkit.ChatColor.*;

@UtilityClass
public final class LobbyItems {

    public static final ItemStack SPECTATE_RANDOM_ITEM = new ItemStack(Material.COMPASS);
    public static final ItemStack SPECTATE_MENU_ITEM = new ItemStack(Material.PAPER);
    public static final ItemStack ENABLE_SPEC_MODE_ITEM = new ItemStack(Material.REDSTONE_TORCH_ON);
    public static final ItemStack DISABLE_SPEC_MODE_ITEM = new ItemStack(Material.LEVER);
    public static final ItemStack MANAGE_ITEM = new ItemStack(Material.ANVIL);
    public static final ItemStack UNFOLLOW_ITEM = new ItemStack(Material.INK_SACK, 1, DyeColor.RED.getDyeData());
    public static final ItemStack PLAYER_STATISTICS = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

    static {
        ItemUtil.setDisplayName(SPECTATE_RANDOM_ITEM, LEFT_ARROW + YELLOW.toString() + BOLD + "Spectate Random Match" + RIGHT_ARROW);
        ItemUtil.setDisplayName(SPECTATE_MENU_ITEM, LEFT_ARROW + GREEN.toString() + BOLD + "Spectate Menu" + RIGHT_ARROW);
        ItemUtil.setDisplayName(ENABLE_SPEC_MODE_ITEM, LEFT_ARROW + AQUA.toString() + BOLD + "Enable Spectator Mode" + RIGHT_ARROW);
        ItemUtil.setDisplayName(DISABLE_SPEC_MODE_ITEM, LEFT_ARROW + AQUA.toString() + BOLD + "Disable Spectator Mode" + RIGHT_ARROW);
        ItemUtil.setDisplayName(MANAGE_ITEM, RED + "Manage PotPvP");
        ItemUtil.setDisplayName(UNFOLLOW_ITEM, LEFT_ARROW + RED + BOLD.toString() + "Stop Following" + RIGHT_ARROW);
        ItemUtil.setDisplayName(PLAYER_STATISTICS, LEFT_ARROW + ChatColor.LIGHT_PURPLE.toString() + BOLD + "Statistics" + RIGHT_ARROW);
    }

}