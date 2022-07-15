package net.frozenorb.potpvp.util.command.parameter;

import net.frozenorb.potpvp.util.command.data.ParameterType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayerParameterType
        implements ParameterType<Player> {
    @Override
    public Player transform(CommandSender sender, String value) {
        if (sender instanceof Player && (value.equalsIgnoreCase("self") || value.equals(""))) {
            return (Player) sender;
        }
        Player player=Bukkit.getServer().getPlayer(value);
        if (player == null || sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "No player with the name \"" + value + "\" found.");
            return null;
        }
        return player;
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        ArrayList<String> completions= new ArrayList<>();
        for ( Player player : Bukkit.getOnlinePlayers() ) {
            completions.add(player.getName());
        }
        return completions;
    }
}


