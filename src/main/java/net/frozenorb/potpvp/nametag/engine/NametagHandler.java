package net.frozenorb.potpvp.nametag.engine;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.nametag.engine.util.ScoreboardTeamPacketMod;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NametagHandler {

    public static final Map<UUID, Map<UUID, NametagInfo>> teamMap = new ConcurrentHashMap<>(); // prob change to uuid ok
    private static final List<NametagInfo> registeredTeams = Collections.synchronizedList(new ArrayList<>());
    private static int teamCreateIndex = 1;
    private static final List<NametagProvider> providers = new ArrayList<>();
    private static boolean nametagRestrictionEnabled = false;
    private static String nametagRestrictBypass = "";
    private static boolean initiated = false;
    private static boolean async = true;
    private static int updateInterval = 2; //surely that cant be just it idk lets try yes yes

    private NametagHandler() {
    }

    public static void init() {
        if (PotPvPSI.getInstance().getConfig().getBoolean("disableNametags", false)) {
            return;
        }

        Preconditions.checkState(!NametagHandler.initiated);
        NametagHandler.initiated = true;
        NametagHandler.nametagRestrictionEnabled = PotPvPSI.getInstance().getConfig().getBoolean("NametagPacketRestriction.Enabled", false);
        NametagHandler.nametagRestrictBypass = PotPvPSI.getInstance().getConfig().getString("NametagPacketRestriction.BypassPrefix").replace("&", "§");
        new NametagThread().start();
        PotPvPSI.getInstance().getServer().getPluginManager().registerEvents(new NametagListener(), PotPvPSI.getInstance());
        registerProvider(new NametagProvider.DefaultNametagProvider());
    }

    public static void registerProvider(NametagProvider newProvider) {
        NametagHandler.providers.add(newProvider);
        NametagHandler.providers.sort((a, b) -> Ints.compare(b.getWeight(), a.getWeight()));
    }

    public static void reloadPlayer(Player toRefresh) {
        NametagUpdate update = new NametagUpdate(toRefresh.getUniqueId());
        if (NametagHandler.async) {
            NametagThread.getPendingUpdates().put(update, true);
        }
        else {
            applyUpdate(update);
        }
    }

    public static void reloadOthersFor(Player refreshFor) {
        for (Player toRefresh : PotPvPSI.getInstance().getServer().getOnlinePlayers()) {
            if (refreshFor == toRefresh) {
                continue;
            }
            reloadPlayer(toRefresh, refreshFor);
        }
    }

    public static void reloadPlayer(Player toRefresh, Player refreshFor) {
        NametagUpdate update = new NametagUpdate(toRefresh.getUniqueId(), refreshFor.getUniqueId());
        if (NametagHandler.async) {
            NametagThread.getPendingUpdates().put(update, true);
        }
        else {
            applyUpdate(update);
        }
    }

    protected static void applyUpdate(NametagUpdate nametagUpdate) {
        Player toRefreshPlayer = PotPvPSI.getInstance().getServer().getPlayer(nametagUpdate.getToRefresh()); // does this work with disguises lol idk LOL why dont we just use uuids
        if (toRefreshPlayer == null) {
            return;
        }
        if (nametagUpdate.getRefreshFor() == null) {
            for (Player refreshFor : PotPvPSI.getInstance().getServer().getOnlinePlayers()) {
                reloadPlayerInternal(toRefreshPlayer, refreshFor);
            }
        }
        else {
            Player refreshForPlayer = PotPvPSI.getInstance().getServer().getPlayer(nametagUpdate.getRefreshFor());
            if (refreshForPlayer != null) {
                reloadPlayerInternal(toRefreshPlayer, refreshForPlayer);
            }
        }
    }

    protected static void reloadPlayerInternal(Player toRefresh, Player refreshFor) {
        if (!refreshFor.hasMetadata("PotPvPSINametag-LoggedIn")) {
            return;
        }

        NametagInfo provided = null;

        int index = 0;
        while (provided == null && index < providers.size()) {
            provided = providers.get(index).fetchNametag(toRefresh, refreshFor);
            index++;
        }
        if (provided == null) return;

        int version = ((CraftPlayer) refreshFor).getHandle().playerConnection.networkManager.getVersion();
        if (version > 5 && nametagRestrictionEnabled) {
            String prefix = provided.getPrefix();

            if (prefix != null && !prefix.equalsIgnoreCase(nametagRestrictBypass)) {
                return;
            }
        }

        Map<UUID, NametagInfo> infoMap = teamMap.getOrDefault(refreshFor.getUniqueId(), new HashMap<>());

        new ScoreboardTeamPacketMod(provided.getName(), Collections.singletonList(toRefresh.getName()), 3).sendToPlayer(refreshFor);

        infoMap.put(toRefresh.getUniqueId(), provided);
        teamMap.put(refreshFor.getUniqueId(), infoMap);
    }

    public static void initiatePlayer(Player player) {
        for (NametagInfo teamInfo : NametagHandler.registeredTeams) {
            teamInfo.getTeamAddPacket().sendToPlayer(player);
        }
    }

    protected static NametagInfo getOrCreate(String prefix, String suffix) {
        for (NametagInfo teamInfo : NametagHandler.registeredTeams) {
            if (teamInfo.getPrefix().equals(prefix) && teamInfo.getSuffix().equals(suffix)) {
                return teamInfo;
            }
        }
        NametagInfo newTeam = new NametagInfo(String.valueOf(NametagHandler.teamCreateIndex++), prefix, suffix);
        NametagHandler.registeredTeams.add(newTeam);
        ScoreboardTeamPacketMod addPacket = newTeam.getTeamAddPacket();
        for (Player player : PotPvPSI.getInstance().getServer().getOnlinePlayers()) {
            addPacket.sendToPlayer(player);
        }
        return newTeam;
    }

    protected static Map<UUID, Map<UUID, NametagInfo>> getTeamMap() {
        return NametagHandler.teamMap;
    }

    public static boolean isNametagRestrictionEnabled() {
        return NametagHandler.nametagRestrictionEnabled;
    }

    public static void setNametagRestrictionEnabled(boolean nametagRestrictionEnabled) {
        NametagHandler.nametagRestrictionEnabled = nametagRestrictionEnabled;
    }

    public static String getNametagRestrictBypass() {
        return NametagHandler.nametagRestrictBypass;
    }

    public static void setNametagRestrictBypass(String nametagRestrictBypass) {
        NametagHandler.nametagRestrictBypass = nametagRestrictBypass;
    }

    public static boolean isInitiated() {
        return NametagHandler.initiated;
    }

    public static boolean isAsync() {
        return NametagHandler.async;
    }

    public static void setAsync(boolean async) {
        NametagHandler.async = async;
    }

    public static int getUpdateInterval() {
        return NametagHandler.updateInterval;
    }

    public static void setUpdateInterval(int updateInterval) {
        NametagHandler.updateInterval = updateInterval;
    }


}