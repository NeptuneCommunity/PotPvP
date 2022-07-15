package net.frozenorb.potpvp.util.command.data;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public interface ParameterType<T> {
    T transform(CommandSender var1, String var2);

    List<String> tabComplete(Player var1, Set<String> var2, String var3);
}