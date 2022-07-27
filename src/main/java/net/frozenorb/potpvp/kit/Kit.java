package net.frozenorb.potpvp.kit;

import lombok.Getter;
import lombok.Setter;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.util.ItemBuilder;
import net.frozenorb.potpvp.util.ItemUtils;
import net.frozenorb.potpvp.util.PatchedPlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;

public final class Kit {

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private int slot; // starts at 1, not 0
    @Getter
    @Setter
    private KitType type;
    @Getter
    @Setter
    private ItemStack[] inventoryContents;

    public static Kit ofDefaultKitCustomName(KitType kitType, String name) {
        return ofDefaultKit(kitType, name, 0);
    }

    public static Kit ofDefaultKit(KitType kitType) {
        return ofDefaultKit(kitType, "Default Kit", 0);
    }

    public static Kit ofDefaultKit(KitType kitType, String name, int slot) {
        Kit kit = new Kit();

        kit.setName(name);
        kit.setType(kitType);
        kit.setSlot(slot);
        kit.setInventoryContents(kitType.getDefaultInventory());

        return kit;
    }

    public void apply(Player player) {
        PatchedPlayerUtils.resetInventory(player);

        // we don't let players actually customize their armor, we just apply default
        player.getInventory().setArmorContents(type.getDefaultArmor());
        player.getInventory().setContents(inventoryContents);

        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        Match match = matchHandler.getMatchPlaying(player);

        if (type.getId().equalsIgnoreCase("Bridges")) {
            ItemStack[] armor;
            for (ItemStack amorStack : armor = new ItemStack[]{
                    new ItemStack(Material.LEATHER_BOOTS),
                    new ItemStack(Material.LEATHER_LEGGINGS),
                    new ItemStack(Material.LEATHER_CHESTPLATE),
                    new ItemStack(Material.LEATHER_HELMET)}) {

                LeatherArmorMeta meta = (LeatherArmorMeta) amorStack.getItemMeta();
                meta.setColor(match.getTeams().get(0) == match.getTeam(player.getUniqueId()) ? Color.BLUE : Color.RED);
                amorStack.setItemMeta(meta);
            }

            player.getInventory().setArmorContents(armor);

            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack != null) {
                    if (itemStack.getType() == Material.STAINED_CLAY) {
                        if (match.getTeams().get(0) == match.getTeam(player.getUniqueId())) {
                            itemStack.setDurability((short) 11);
                            return;
                        }


                        itemStack.setDurability((short) 14);
                    }
                }
            }
        }

        if (type.getId().equalsIgnoreCase("Bridges") || type.getId().equalsIgnoreCase("Boxing")) {
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack != null) {
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    itemMeta.spigot().setUnbreakable(true);
                    itemStack.setItemMeta(itemMeta);
                }
            }

            player.updateInventory();
        }

        Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), player::updateInventory, 1L);
    }

    public int countHeals() {
        return ItemUtils.countStacksMatching(inventoryContents, ItemUtils.INSTANT_HEAL_POTION_PREDICATE);
    }

    public int countDebuffs() {
        return ItemUtils.countStacksMatching(inventoryContents, ItemUtils.DEBUFF_POTION_PREDICATE);
    }

    public int countFood() {
        return ItemUtils.countStacksMatching(inventoryContents, ItemUtils.EDIBLE_PREDICATE);
    }

    public int countPearls() {
        return ItemUtils.countStacksMatching(inventoryContents, v -> v.getType() == Material.ENDER_PEARL);
    }

    // we use this method instead of .toSelectableBook().isSimilar()
    // to avoid the slight performance overhead of constructing
    // that itemstack every time
    public boolean isSelectionItem(ItemStack itemStack) {
        if (itemStack.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }

        ItemMeta meta = itemStack.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.YELLOW.toString() + ChatColor.BOLD + name);
    }

    public ItemStack createSelectionItem() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD + name);

        item.setItemMeta(itemMeta);
        return item;
    }
}