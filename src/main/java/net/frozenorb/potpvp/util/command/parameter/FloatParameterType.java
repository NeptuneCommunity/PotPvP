package net.frozenorb.potpvp.util.command.parameter;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.util.command.data.ParameterType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class FloatParameterType
        implements ParameterType<Float> {
    @Override
    public Float transform(CommandSender sender, String value) {
        if (value.toLowerCase().contains("e")) {
            sender.sendMessage(ChatColor.RED + value + " is not a valid number.");
            return null;
        }
        try {
            float parsed=Float.parseFloat(value);
            if (Float.isNaN(parsed) || !Float.isFinite(parsed)) {
                sender.sendMessage(ChatColor.RED + value + " is not a valid number.");
                return null;
            }
            return Float.valueOf(parsed);
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

