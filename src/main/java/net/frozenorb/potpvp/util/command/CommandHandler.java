package net.frozenorb.potpvp.util.command;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.util.command.bukkit.FrozenCommand;
import net.frozenorb.potpvp.util.command.bukkit.FrozenCommandMap;
import net.frozenorb.potpvp.util.command.bukkit.FrozenHelpTopic;
import net.frozenorb.potpvp.util.command.data.ParameterType;
import net.frozenorb.potpvp.util.command.parameter.*;
import net.frozenorb.potpvp.util.command.parameter.filter.NormalFilter;
import net.frozenorb.potpvp.util.command.parameter.filter.StrictFilter;
import net.frozenorb.potpvp.util.command.utils.ClassUtils;
import net.frozenorb.potpvp.util.command.utils.EasyClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class CommandHandler {
    public static CommandNode ROOT_NODE = new CommandNode();
    protected static Map<Class<?>, ParameterType<?>> PARAMETER_TYPE_MAP = new HashMap<>();
    protected static CommandMap commandMap;
    public static Map<String, Command> knownCommands;
    private static CommandConfiguration config;

    public static void init() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    CommandHandler.swapCommandMap();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(PotPvPSI.getInstance(), 5L);
    }

    public static void registerParameterType(Class<?> clazz, ParameterType<?> type) {
        PARAMETER_TYPE_MAP.put(clazz, type);
    }

    public static ParameterType getParameterType(Class<?> clazz) {
        return PARAMETER_TYPE_MAP.get(clazz);
    }

    public static CommandConfiguration getConfig() {
        return config;
    }

    public static void setConfig(CommandConfiguration config) {
        CommandHandler.config = config;
    }

    public static void registerMethod(Method method) {
        method.setAccessible(true);
        Set<CommandNode> nodes = new MethodProcessor().process(method);
        if (nodes != null) {
            nodes.forEach(node -> {
                if (node != null) {
                    FrozenCommand command = new FrozenCommand(node, JavaPlugin.getProvidingPlugin(method.getDeclaringClass()));
                    CommandHandler.register(command);
                    node.getChildren().values().forEach(n -> CommandHandler.registerHelpTopic(n, node.getAliases()));
                }
            });
        }
    }

    protected static void registerHelpTopic(CommandNode node, Set<String> aliases) {
        if (node.method != null) {
            Bukkit.getHelpMap().addTopic(new FrozenHelpTopic(node, aliases));
        }
        if (node.hasCommands()) {
            node.getChildren().values().forEach(n -> CommandHandler.registerHelpTopic(n, null));
        }
    }

    private static void register(FrozenCommand command) {
        try {
            Map<String, Command> knownCommands = CommandHandler.getKnownCommands();
            Iterator<Map.Entry<String, Command>> iterator = knownCommands.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Command> entry = iterator.next();
                if (!entry.getValue().getName().equalsIgnoreCase(command.getName())) continue;
                entry.getValue().unregister(commandMap);
                iterator.remove();
            }
            for (String alias : command.getAliases()) {
                knownCommands.put(alias, command);
            }
            command.register(commandMap);
            knownCommands.put(command.getName(), command);
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public static void registerClass(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            CommandHandler.registerMethod(method);
        }
    }

    public static void unregisterClass(Class<?> clazz) {
        Map<String, Command> knownCommands = CommandHandler.getKnownCommands();
        Iterator<Command> iterator = knownCommands.values().iterator();
        while (iterator.hasNext()) {
            CommandNode node;
            Command command = iterator.next();
            if (!(command instanceof FrozenCommand) || ((FrozenCommand) command).getNode().getOwningClass() != clazz)
                continue;
            command.unregister(commandMap);
            iterator.remove();
        }
    }

    public static void registerPackage(Plugin plugin, String packageName) {
        ClassUtils.getClassesInPackage(plugin, packageName).forEach(CommandHandler::registerClass);
    }

    public static void registerAll(Plugin plugin) {
        CommandHandler.registerPackage(plugin, plugin.getClass().getPackage().getName());
    }

    private static void swapCommandMap() throws Exception {
        Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        Object oldCommandMap = commandMapField.get(Bukkit.getServer());
        FrozenCommandMap newCommandMap = new FrozenCommandMap(Bukkit.getServer());
        Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(knownCommandsField, knownCommandsField.getModifiers() & 0xFFFFFFEF);
        knownCommandsField.set(newCommandMap, knownCommandsField.get(oldCommandMap));
        commandMapField.set(Bukkit.getServer(), newCommandMap);
    }

    private static CommandMap getCommandMap() {
        return (CommandMap) new EasyClass<>(Bukkit.getServer()).getField("commandMap").get();
    }

    private static Map<String, Command> getKnownCommands() {
        return (Map) new EasyClass<>(commandMap).getField("knownCommands").get();
    }

    static {
        config = new CommandConfiguration().setNoPermissionMessage("&cNo permission.");
        CommandHandler.registerParameterType(Boolean.TYPE, new BooleanParameterType());
        CommandHandler.registerParameterType(Integer.TYPE, new IntegerParameterType());
        CommandHandler.registerParameterType(Double.TYPE, new DoubleParameterType());
        CommandHandler.registerParameterType(Float.TYPE, new FloatParameterType());
        CommandHandler.registerParameterType(String.class, new StringParameterType());
        CommandHandler.registerParameterType(Player.class, new PlayerParameterType());
        CommandHandler.registerParameterType(World.class, new WorldParameterType());
        CommandHandler.registerParameterType(OfflinePlayer.class, new OfflinePlayerParameterType());
        CommandHandler.registerParameterType(NormalFilter.class, new NormalFilter());
        CommandHandler.registerParameterType(StrictFilter.class, new StrictFilter());
        commandMap = CommandHandler.getCommandMap();
        knownCommands = CommandHandler.getKnownCommands();
    }
}