package net.frozenorb.potpvp.util.command.parameter;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.util.command.data.ParameterType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OfflinePlayerParameterType implements ParameterType<OfflinePlayer> {
    @Override
    public OfflinePlayer transform(CommandSender sender, String source) {
        if (sender instanceof Player && (source.equalsIgnoreCase("self") || source.equals(""))) {
            return (Player) sender;
        }
        return PotPvPSI.getInstance().getServer().getOfflinePlayer(source);
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        ArrayList<String> completions= new ArrayList<>();
        for ( Player player : Bukkit.getOnlinePlayers() ) {
            if (sender.canSee(player)) {
                completions.add(player.getName());
            }
        }
        return completions;
    }
}

