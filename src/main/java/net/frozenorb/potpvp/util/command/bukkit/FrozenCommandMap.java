package net.frozenorb.potpvp.util.command.bukkit;

import net.frozenorb.potpvp.util.command.CommandNode;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FrozenCommandMap extends SimpleCommandMap {
    public FrozenCommandMap(Server server) {
        super(server);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(cmdLine, "Command line cannot null");
        int spaceIndex = cmdLine.indexOf(32);
        if (spaceIndex == -1) {
            ArrayList<String> completions = new ArrayList<>();
            Map<String, Command> knownCommands = this.knownCommands;
            String prefix = sender instanceof Player ? "/" : "";
            for (Map.Entry commandEntry : knownCommands.entrySet()) {
                String name = (String) commandEntry.getKey();
                if (!StringUtil.startsWithIgnoreCase(name, cmdLine)) continue;
                Command command = (Command) commandEntry.getValue();
                if (command instanceof FrozenCommand) {
                    CommandNode executionNode = ((FrozenCommand) command).node.getCommand(name);
                    if (executionNode == null) {
                        executionNode = ((FrozenCommand) command).node;
                    }
                    if (!executionNode.hasCommands()) {
                        CommandNode testNode = executionNode.getCommand(name);
                        if (testNode == null) {
                            testNode = ((FrozenCommand) command).node.getCommand(name);
                        }
                        if (!testNode.canUse(sender)) continue;
                        completions.add(prefix + name);
                        continue;
                    }
                    if (executionNode.getSubCommands(sender, false).size() == 0) continue;
                    completions.add(prefix + name);
                    continue;
                }
                if (!command.testPermissionSilent(sender)) continue;
                completions.add(prefix + name);
            }
            completions.sort(String.CASE_INSENSITIVE_ORDER);
            return completions;
        }
        String commandName = cmdLine.substring(0, spaceIndex);
        Command target = this.getCommand(commandName);
        if (target == null) {
            return null;
        }
        if (!target.testPermissionSilent(sender)) {
            return null;
        }
        String argLine = cmdLine.substring(spaceIndex + 1);
        String[] args = argLine.split(" ");
        try {
            List<String> completions = target instanceof FrozenCommand ? ((FrozenCommand) target).tabComplete(sender, cmdLine) : target.tabComplete(sender, commandName, args);
            if (completions != null) {
                completions.sort(String.CASE_INSENSITIVE_ORDER);
            }
            return completions;
        } catch (CommandException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new CommandException("Unhandled exception executing tab-completer for '" + cmdLine + "' in " + target, ex);
        }
    }
}