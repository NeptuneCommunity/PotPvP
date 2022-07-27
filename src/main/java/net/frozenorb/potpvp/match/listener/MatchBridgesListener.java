package net.frozenorb.potpvp.match.listener;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.arena.Arena;
import net.frozenorb.potpvp.kit.Kit;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.match.MatchTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MatchBridgesListener implements Listener {

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        Match match = matchHandler.getMatchPlaying(player);

        if (match == null) return;

        if (!match.getKitType().getId().equalsIgnoreCase("Bridges")) return;

        if (event.getItem() != null) {
            if (event.getItem().getType() == Material.GOLDEN_APPLE) {
                event.getPlayer().setHealth(event.getPlayer().getMaxHealth());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        Match match = matchHandler.getMatchPlaying(player);

        if (match == null) {
            return;
        }

        if (!match.getKitType().getId().equalsIgnoreCase("Bridges")) return;

        if (event.getEntity().getKiller() != null) {
            match.getKills().put(player.getKiller().getUniqueId(), match.getKills().get(player.getKiller().getUniqueId()) + 1);
        }

        List<MatchTeam> teams = match.getTeams();
        MatchTeam ourTeam = match.getTeam(player.getUniqueId());

        player.spigot().respawn();

        player.teleport(ourTeam == teams.get(0) ? match.getArena().getTeam1Spawn() : match.getArena().getTeam2Spawn());
        match.getCurrentKit().getOrDefault(player.getUniqueId(), Kit.ofDefaultKit(match.getKitType())).apply(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        Match match = PotPvPSI.getInstance().getMatchHandler().getMatchPlaying(player);

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
            return;
        if (match == null) return;

        // is it done?
        Arena arena = match.getArena();

        if (!match.getKitType().getId().equalsIgnoreCase("Bridges")) return;

        List<MatchTeam> teams = match.getTeams();
        MatchTeam ourTeam = match.getTeam(player.getUniqueId());
        MatchTeam otherTeam = teams.get(0) == ourTeam ? teams.get(1) : teams.get(0);

        if (to.getBlock().getType() == Material.ENDER_PORTAL) {
            if (ourTeam == match.getTeams().get(0)) {
                if (arena.getTeam1Spawn().distance(to) < arena.getTeam2Spawn().distance(to)) {
                    player.teleport(ourTeam == match.getTeams().get(0) ? arena.getTeam1Spawn().clone().subtract(0.5, 1.5, 0.5) : arena.getTeam2Spawn().clone().subtract(0.5, 1.5, 0.5));
                    return;
                }
            }

            if (ourTeam == match.getTeams().get(1)) {
                if (arena.getTeam2Spawn().distance(player.getLocation()) < arena.getTeam1Spawn().distance(player.getLocation())) {
                    player.teleport(ourTeam == match.getTeams().get(0) ? arena.getTeam1Spawn().clone().subtract(0.5, 1.5, 0.5) : arena.getTeam2Spawn().clone().subtract(0.5, 1.5, 0.5));
                    return;
                }
            }

            if (ourTeam != null) {
                for (UUID ourTeamUUIDS : ourTeam.getAliveMembers()) {
                    Player ourTeamBukkit = Bukkit.getPlayer(ourTeamUUIDS);

                    ourTeam.setRoundsWon(ourTeam.getRoundsWon() + 1);

                    if (ourTeamBukkit != null) {
                        ourTeamBukkit.teleport(ourTeam == teams.get(0) ? arena.getTeam1Spawn() : arena.getTeam2Spawn());

                        if (ourTeam.getRoundsWon() != 3) {
                            Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), () -> ourTeamBukkit.setMetadata("Respawning", new FixedMetadataValue(PotPvPSI.getInstance(), true)), 10L);
                            ourTeamBukkit.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.YELLOW + " has scored. " + ChatColor.LIGHT_PURPLE + (3 - ourTeam.getRoundsWon()) + ChatColor.YELLOW + " more to win.");
                        }
                    }

                    match.getCurrentKit().getOrDefault(ourTeamUUIDS, Kit.ofDefaultKit(match.getKitType())).apply(ourTeamBukkit);
                }

                for (UUID otherTeamUUIDS : otherTeam.getAliveMembers()) {
                    Player otherTeamBukkit = Bukkit.getPlayer(otherTeamUUIDS);

                    if (otherTeamBukkit != null) {
                        otherTeamBukkit.teleport(otherTeam == teams.get(0) ? arena.getTeam1Spawn() : arena.getTeam2Spawn());

                        if (ourTeam.getRoundsWon() != 3) {
                            Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), () -> otherTeamBukkit.setMetadata("Respawning", new FixedMetadataValue(PotPvPSI.getInstance(), true)), 10L);
                            otherTeamBukkit.sendMessage(ChatColor.RED + player.getName() + ChatColor.YELLOW + " has scored. " + ChatColor.LIGHT_PURPLE + (3 - ourTeam.getRoundsWon()) + ChatColor.YELLOW + " more to win.");
                        }
                    }

                    match.getCurrentKit().getOrDefault(otherTeamUUIDS, Kit.ofDefaultKit(match.getKitType())).apply(otherTeamBukkit);
                }

                if (ourTeam.getRoundsWon() != 3) {
                    Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), () -> {
                        for (MatchTeam matchTeam : match.getTeams()) {
                            matchTeam.getAliveMembers().forEach(players -> {
                                Player playersBukkit = Bukkit.getPlayer(players);

                                if (playersBukkit != null) {
                                    playersBukkit.removeMetadata("Respawning", PotPvPSI.getInstance());
                                }
                            });
                        }
                    }, 5 * 20L);
                }

                match.checkEnded();
            }
        }

        if (event.getPlayer().hasMetadata("Respawning")) {
            event.setTo(from);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Player player = (Player) event.getEntity();
        Match match = PotPvPSI.getInstance().getMatchHandler().getMatchPlaying(player);

        //BasicPreventionListener handles this
        if (match == null) return;

        if (!match.getKitType().getId().equalsIgnoreCase("Bridges")) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Match match = PotPvPSI.getInstance().getMatchHandler().getMatchPlaying(player);

            if (match == null) return;

            if (match.getKitType().getId().equalsIgnoreCase("Bridges")) {
                event.setCancelled(true);
            }
        }
    }

    private final Map<UUID, Long> bowCooldown = new ConcurrentHashMap<>();

    @EventHandler
    public void EntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
            Match match = matchHandler.getMatchPlaying(player);

            if (match == null) return;

            if (!match.getKitType().getId().equalsIgnoreCase("Bridges")) return;

            if (player.hasMetadata("Respawning")) {
                event.setCancelled(true);
                return;
            }

            bowCooldown.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5));

            new BukkitRunnable() {

                public void run() {
                    long cooldownExpires = bowCooldown.getOrDefault(player.getUniqueId(), 0L);

                    if (cooldownExpires < System.currentTimeMillis()) {
                        cancel();
                        return;
                    }

                    int millisLeft = (int) (cooldownExpires - System.currentTimeMillis());
                    float percentLeft = (float) millisLeft / 5;

                    player.setExp(percentLeft);
                    player.setLevel(millisLeft / 1_000);
                }
            }.runTaskTimer(PotPvPSI.getInstance(), 1L, 1L);

            Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), () -> {
                player.getInventory().addItem(new ItemStack(Material.ARROW));
            }, 5 * 20L);
        }
    }
}