package net.frozenorb.potpvp.util.command.parameter.filter;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.util.command.data.ParameterType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

abstract class BaseFilter implements ParameterType<String> {
    protected final Set<Pattern> bannedPatterns=new HashSet<>();

    BaseFilter() {
    }

    @Override
    public String transform(CommandSender sender, String value) {
        for ( Pattern bannedPattern : this.bannedPatterns ) {
            if (!bannedPattern.matcher(value).find()) continue;
            sender.sendMessage(ChatColor.RED + "Command contains inappropriate content.");
            return null;
        }
        return value;
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String prefix) {
        return ImmutableList.of();
    }
}

