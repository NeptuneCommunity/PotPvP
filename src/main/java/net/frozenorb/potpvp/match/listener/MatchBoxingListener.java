package net.frozenorb.potpvp.match.listener;

import lombok.Getter;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.match.MatchTeam;
import net.frozenorb.potpvp.util.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.UUID;

public class MatchBoxingListener implements Listener {

    @Getter
    private final HashMap<UUID, Integer> victimHitMap = new HashMap<>();

    @Getter
    static HashMap<MatchTeam, Integer> hitMapTeam = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        Player victim = (Player) event.getEntity();
        Player attacker = PlayerUtils.getDamageSource(event.getDamager());

        if (attacker == null) return;

        Match match = matchHandler.getMatchPlaying(attacker);

        if (match == null) return;

        if (match.getKitType().getId().equals("BOXING")) {
            MatchTeam ourTeam = match.getTeam(attacker.getUniqueId());
            MatchTeam otherTeam = match.getTeams().get(0) == ourTeam ? match.getTeams().get(1) : match.getTeams().get(0);

            //1v1
            if (match.getTeams().size() == 2) {
                if (ourTeam.getAllMembers().size() == 1 && otherTeam.getAllMembers().size() == 1) {
                    if (match.getTotalHits().getOrDefault(attacker.getUniqueId(), 0) >= 100) {
                        victim.damage(victim.getHealth() + 20F);
                        match.markDead(victim);
                    }
                } else {
                    //2v2++
                    if (victimHitMap.getOrDefault(victim.getUniqueId(), 0) >= 100) {
                        victim.damage(victim.getHealth() + 20F);
                        match.markDead(victim);
                    }

                    if (match.getTeam(attacker.getUniqueId()).getAllMembers().contains(victim.getUniqueId())) {
                        return;
                    }

                    MatchTeam attackerTeam = match.getTeam(attacker.getUniqueId());

                    hitMapTeam.put(attackerTeam, hitMapTeam.getOrDefault(attackerTeam, 0) + 1);
                    victimHitMap.put(victim.getUniqueId(), victimHitMap.getOrDefault(victim.getUniqueId(), 0) + 1);
                }
            }

            event.setDamage(0F);
        }
    }
}