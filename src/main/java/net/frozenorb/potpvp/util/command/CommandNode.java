package net.frozenorb.potpvp.util.command;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.NonNull;
import mkremins.fanciful.FancyMessage;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.util.command.argument.Arguments;
import net.frozenorb.potpvp.util.command.data.Data;
import net.frozenorb.potpvp.util.command.data.FlagData;
import net.frozenorb.potpvp.util.command.data.ParameterData;
import net.frozenorb.potpvp.util.command.data.ParameterType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.spigotmc.SpigotConfig;

import java.beans.ConstructorProperties;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CommandNode {
    @NonNull
    private String name;
    private Set<String> aliases = new HashSet();
    @NonNull
    private String permission;
    private String description;
    private boolean async;
    private boolean hidden;
    protected Method method;
    protected Class<?> owningClass;
    private List<String> validFlags;
    private List<Data> parameters;
    private Map<String, CommandNode> children = new TreeMap();
    private CommandNode parent;
    private boolean logToConsole;

    public CommandNode(Class<?> owningClass) {
        this.owningClass = owningClass;
    }

    public void registerCommand(CommandNode commandNode) {
        commandNode.setParent(this);
        this.children.put(commandNode.getName(), commandNode);
    }

    public boolean hasCommand(String name) {
        return this.children.containsKey(name.toLowerCase());
    }

    public CommandNode getCommand(String name) {
        return this.children.get(name.toLowerCase());
    }

    public boolean hasCommands() {
        return this.children.size() > 0;
    }

    public CommandNode findCommand(Arguments arguments) {
        if (arguments.getArguments().size() > 0) {
            String trySub = arguments.getArguments().get(0);
            if (this.hasCommand(trySub)) {
                arguments.getArguments().remove(0);
                CommandNode returnNode = this.getCommand(trySub);
                return returnNode.findCommand(arguments);
            }
        }

        return this;
    }

    public boolean isValidFlag(String test) {
        return test.length() == 1 ? this.validFlags.contains(test) : this.validFlags.contains(test.toLowerCase());
    }

    public boolean canUse(CommandSender sender) {
        if (this.permission == null) {
            return true;
        } else {
            String var2 = this.permission;
            byte var3 = -1;
            switch (var2.hashCode()) {
                case 0:
                    if (var2.equals("")) {
                        var3 = 2;
                    }
                    break;
                case 3553:
                    if (var2.equals("op")) {
                        var3 = 1;
                    }
                    break;
                case 951510359:
                    if (var2.equals("console")) {
                        var3 = 0;
                    }
            }

            switch (var3) {
                case 0:
                    return sender instanceof ConsoleCommandSender;
                case 1:
                    return sender.isOp();
                case 2:
                    return true;
                default:
                    return sender.hasPermission(this.permission);
            }
        }
    }

    public FancyMessage getUsage(String realLabel) {
        FancyMessage usage = (new FancyMessage("Usage: /" + realLabel)).color(ChatColor.RED);
        if (!Strings.isNullOrEmpty(this.getDescription())) {
            usage.tooltip(ChatColor.YELLOW + this.getDescription());
        }

        List<FlagData> flags = Lists.newArrayList();
        flags.addAll(this.parameters.stream().filter((datax) -> {
            return datax instanceof FlagData;
        }).map((datax) -> {
            return (FlagData) datax;
        }).collect(Collectors.toList()));
        List<ParameterData> parameters = Lists.newArrayList();
        parameters.addAll(this.parameters.stream().filter((datax) -> {
            return datax instanceof ParameterData;
        }).map((datax) -> {
            return (ParameterData) datax;
        }).collect(Collectors.toList()));
        boolean flagFirst = true;
        if (!flags.isEmpty()) {
            usage.then("(").color(ChatColor.RED);
            if (!Strings.isNullOrEmpty(this.getDescription())) {
                usage.tooltip(ChatColor.YELLOW + this.getDescription());
            }

            Iterator var6 = flags.iterator();

            while (var6.hasNext()) {
                FlagData data = (FlagData) var6.next();
                String name = data.getNames().get(0);
                if (!flagFirst) {
                    usage.then(" | ").color(ChatColor.RED);
                    if (!Strings.isNullOrEmpty(this.getDescription())) {
                        usage.tooltip(ChatColor.YELLOW + this.getDescription());
                    }
                }

                flagFirst = false;
                usage.then("-" + name).color(ChatColor.AQUA);
                if (!Strings.isNullOrEmpty(data.getDescription())) {
                    usage.tooltip(ChatColor.GRAY + data.getDescription());
                }
            }

            usage.then(") ").color(ChatColor.RED);
            if (!Strings.isNullOrEmpty(this.getDescription())) {
                usage.tooltip(ChatColor.YELLOW + this.getDescription());
            }
        }

        if (!parameters.isEmpty()) {
            for (int index = 0; index < parameters.size(); ++index) {
                ParameterData data = parameters.get(index);
                boolean required = data.getDefaultValue().isEmpty();
                usage.then((required ? "<" : "[") + data.getName() + (data.isWildcard() ? "..." : "") + (required ? ">" : "]") + (index != parameters.size() - 1 ? " " : "")).color(ChatColor.RED);
                if (!Strings.isNullOrEmpty(this.getDescription())) {
                    usage.tooltip(ChatColor.YELLOW + this.getDescription());
                }
            }
        }

        return usage;
    }

    public FancyMessage getUsage() {
        FancyMessage usage = new FancyMessage("");
        List<FlagData> flags = Lists.newArrayList();
        flags.addAll(this.parameters.stream().filter((datax) -> {
            return datax instanceof FlagData;
        }).map((datax) -> {
            return (FlagData) datax;
        }).collect(Collectors.toList()));
        List<ParameterData> parameters = Lists.newArrayList();
        parameters.addAll(this.parameters.stream().filter((datax) -> {
            return datax instanceof ParameterData;
        }).map((datax) -> {
            return (ParameterData) datax;
        }).collect(Collectors.toList()));
        boolean flagFirst = true;
        if (!flags.isEmpty()) {
            usage.then("(").color(ChatColor.RED);
            Iterator var5 = flags.iterator();

            while (var5.hasNext()) {
                FlagData data = (FlagData) var5.next();
                String name = data.getNames().get(0);
                if (!flagFirst) {
                    usage.then(" | ").color(ChatColor.RED);
                }

                flagFirst = false;
                usage.then("-" + name).color(ChatColor.AQUA);
                if (!Strings.isNullOrEmpty(data.getDescription())) {
                    usage.tooltip(ChatColor.GRAY + data.getDescription());
                }
            }

            usage.then(") ").color(ChatColor.RED);
        }

        if (!parameters.isEmpty()) {
            for (int index = 0; index < parameters.size(); ++index) {
                ParameterData data = parameters.get(index);
                boolean required = data.getDefaultValue().isEmpty();
                usage.then((required ? "<" : "[") + data.getName() + (data.isWildcard() ? "..." : "") + (required ? ">" : "]") + (index != parameters.size() - 1 ? " " : "")).color(ChatColor.RED);
            }
        }

        return usage;
    }

    public boolean invoke(CommandSender sender, Arguments arguments) throws CommandException {
        if (this.method == null) {
            if (this.hasCommands()) {
                if (this.getSubCommands(sender, true).isEmpty()) {
                    if (this.isHidden()) {
                        sender.sendMessage(SpigotConfig.unknownCommandMessage);
                    } else {
                        sender.sendMessage(ChatColor.RED + "No permission.");
                    }
                }
            } else {
                sender.sendMessage(SpigotConfig.unknownCommandMessage);
            }

            return true;
        } else {
            List<Object> objects = new ArrayList(this.method.getParameterCount());
            objects.add(sender);
            int index = 0;
            Iterator var5 = this.parameters.iterator();

            while (true) {
                while (var5.hasNext()) {
                    Data unknownData = (Data) var5.next();
                    if (unknownData instanceof FlagData) {
                        FlagData flagData = (FlagData) unknownData;
                        boolean value = flagData.getDefaultValue();
                        Iterator var18 = flagData.getNames().iterator();

                        while (var18.hasNext()) {
                            String s = (String) var18.next();
                            if (arguments.hasFlag(s)) {
                                value = !value;
                                break;
                            }
                        }

                        objects.add(flagData.getMethodIndex(), value);
                    } else if (unknownData instanceof ParameterData) {
                        ParameterData parameterData = (ParameterData) unknownData;

                        String argument;
                        try {
                            argument = arguments.getArguments().get(index);
                        } catch (Exception var13) {
                            if (parameterData.getDefaultValue().isEmpty()) {
                                return false;
                            }

                            argument = parameterData.getDefaultValue();
                        }

                        if (parameterData.isWildcard() && (argument.isEmpty() || !argument.equals(parameterData.getDefaultValue()))) {
                            argument = arguments.join(index);
                        }

                        ParameterType<?> type = CommandHandler.getParameterType(parameterData.getType());
                        if (parameterData.getParameterType() != null) {
                            try {
                                type = (ParameterType) parameterData.getParameterType().newInstance();
                            } catch (IllegalAccessException | InstantiationException var12) {
                                var12.printStackTrace();
                                throw new CommandException("Failed to create ParameterType instance: " + parameterData.getParameterType().getName(), var12);
                            }
                        }

                        if (type == null) {
                            Class<?> t = parameterData.getType();
                            sender.sendMessage(ChatColor.RED + "No parameter type found: " + t.getSimpleName());
                            return true;
                        }

                        Object result = type.transform(sender, argument);
                        if (result == null) {
                            return true;
                        }

                        objects.add(parameterData.getMethodIndex(), result);
                        ++index;
                    }
                }

                try {
                    StopWatch stopwatch = new StopWatch();
                    stopwatch.start();
                    this.method.invoke(null, objects.toArray());
                    stopwatch.stop();
                    int executionThreshold = 10;
                    if (!this.async && this.logToConsole && stopwatch.getTime() >= (long) executionThreshold) {
                        PotPvPSI.getInstance().getLogger().warning("Command '/" + this.getFullLabel() + "' took " + stopwatch.getTime() + "ms!");
                    }

                    return true;
                } catch (InvocationTargetException | IllegalAccessException var11) {
                    var11.printStackTrace();
                    throw new CommandException("An error occurred while executing the command", var11);
                }
            }
        }
    }

    public List<String> getSubCommands(CommandSender sender, boolean print) {
        List<String> commands = new ArrayList();
        if (this.canUse(sender)) {
            String command = (sender instanceof Player ? "/" : "") + this.getFullLabel() + (this.parameters != null ? " " + this.getUsage().toOldMessageFormat() : "") + (!Strings.isNullOrEmpty(this.description) ? ChatColor.GRAY + " - " + this.getDescription() : "");
            if (this.parent == null) {
                commands.add(command);
            } else if (this.parent.getName() != null && CommandHandler.ROOT_NODE.getCommand(this.parent.getName()) != this.parent) {
                commands.add(command);
            }

            if (this.hasCommands()) {
                Iterator var5 = this.getChildren().values().iterator();

                while (var5.hasNext()) {
                    CommandNode n = (CommandNode) var5.next();
                    commands.addAll(n.getSubCommands(sender, false));
                }
            }
        }

        if (!commands.isEmpty() && print) {
            sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 35));
            Iterator var7 = commands.iterator();

            while (var7.hasNext()) {
                String command = (String) var7.next();
                sender.sendMessage(ChatColor.RED + command);
            }

            sender.sendMessage(ChatColor.BLUE.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 35));
        }

        return commands;
    }

    public Set<String> getRealAliases() {
        Set<String> aliases = this.getAliases();
        aliases.remove(this.getName());
        return aliases;
    }

    public String getFullLabel() {
        List<String> labels = new ArrayList();

        for (CommandNode node = this; node != null; node = node.getParent()) {
            String name = node.getName();
            if (name != null) {
                labels.add(name);
            }
        }

        Collections.reverse(labels);
        labels.remove(0);
        StringBuilder builder = new StringBuilder();
        labels.forEach((s) -> {
            builder.append(s).append(' ');
        });
        return builder.toString().trim();
    }

    public String getUsageForHelpTopic() {
        return this.method != null && this.parameters != null ? "/" + this.getFullLabel() + " " + ChatColor.stripColor(this.getUsage().toOldMessageFormat()) : "";
    }

    @ConstructorProperties({"name", "permission"})
    public CommandNode(@NonNull String name, @NonNull String permission) {
        if (name == null) {
            throw new NullPointerException("name");
        } else if (permission == null) {
            throw new NullPointerException("permission");
        } else {
            this.name = name;
            this.permission = permission;
        }
    }

    @ConstructorProperties({"name", "aliases", "permission", "description", "async", "hidden", "method", "owningClass", "validFlags", "parameters", "children", "parent", "logToConsole"})
    public CommandNode(@NonNull String name, Set<String> aliases, @NonNull String permission, String description, boolean async, boolean hidden, Method method, Class<?> owningClass, List<String> validFlags, List<Data> parameters, Map<String, CommandNode> children, CommandNode parent, boolean logToConsole) {
        if (name == null) {
            throw new NullPointerException("name");
        } else if (permission == null) {
            throw new NullPointerException("permission");
        } else {
            this.name = name;
            this.aliases = aliases;
            this.permission = permission;
            this.description = description;
            this.async = async;
            this.hidden = hidden;
            this.method = method;
            this.owningClass = owningClass;
            this.validFlags = validFlags;
            this.parameters = parameters;
            this.children = children;
            this.parent = parent;
            this.logToConsole = logToConsole;
        }
    }

    public CommandNode() {
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    public void setName(@NonNull String name) {
        if (name == null) {
            throw new NullPointerException("name");
        } else {
            this.name = name;
        }
    }

    public Set<String> getAliases() {
        return this.aliases;
    }

    @NonNull
    public String getPermission() {
        return this.permission;
    }

    public void setPermission(@NonNull String permission) {
        if (permission == null) {
            throw new NullPointerException("permission");
        } else {
            this.permission = permission;
        }
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAsync() {
        return this.async;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getOwningClass() {
        return this.owningClass;
    }

    public List<String> getValidFlags() {
        return this.validFlags;
    }

    public void setValidFlags(List<String> validFlags) {
        this.validFlags = validFlags;
    }

    public List<Data> getParameters() {
        return this.parameters;
    }

    public void setParameters(List<Data> parameters) {
        this.parameters = parameters;
    }

    public Map<String, CommandNode> getChildren() {
        return this.children;
    }

    public CommandNode getParent() {
        return this.parent;
    }

    public void setParent(CommandNode parent) {
        this.parent = parent;
    }

    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }
}