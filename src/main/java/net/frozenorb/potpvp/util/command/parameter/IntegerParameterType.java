package net.frozenorb.potpvp.util.command.parameter;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.util.command.data.ParameterType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class IntegerParameterType
        implements ParameterType<Integer> {
    @Override
    public Integer transform(CommandSender sender, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + value + " is not a valid number.");
            return null;
        }
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String prefix) {
        return ImmutableList.of();
    }
}

