package net.frozenorb.potpvp.postmatchinv.command;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.postmatchinv.PostMatchInvHandler;
import net.frozenorb.potpvp.postmatchinv.PostMatchPlayer;
import net.frozenorb.potpvp.postmatchinv.menu.PostMatchMenu;
import net.frozenorb.potpvp.util.command.annotation.Command;
import net.frozenorb.potpvp.util.command.annotation.Param;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class CheckPostMatchInvCommand {

    @Command(names = {"checkPostMatchInv", "_"}, permission = "")
    public static void checkPostMatchInv(Player sender, @Param(name = "target") UUID target) {
        PostMatchInvHandler postMatchInvHandler = PotPvPSI.getInstance().getPostMatchInvHandler();
        Map<UUID, PostMatchPlayer> players = postMatchInvHandler.getPostMatchData(sender.getUniqueId());

        if (players.containsKey(target)) {
            new PostMatchMenu(players.get(target)).openMenu(sender);
        } else {
            sender.sendMessage(ChatColor.RED + "Data for " + PotPvPSI.getInstance().uuidCache.name(target) + " not found.");
        }
    }

}