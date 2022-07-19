package net.frozenorb.potpvp.tab.engine;

import net.frozenorb.potpvp.tab.engine.misc.PlayerInfoPacketMod;
import net.frozenorb.potpvp.tab.engine.misc.TabUtils;
import net.frozenorb.potpvp.tab.engine.misc.TeamPacket;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;

import java.util.*;

final class Tab {

    private final Player player;
    private final Map<String, String> previousNames;
    private final Map<String, Integer> previousPings;
    private String lastHeader;
    private String lastFooter;
    private final Set<String> createdTeams;
    private TabLayout initialLayout;
    private boolean initiated;
    private final StringBuilder removeColorCodesBuilder;

    public Tab(final Player player) {
        this.previousNames = new HashMap<>();
        this.previousPings = new HashMap<>();
        this.lastHeader = "{\"translate\":\"\"}";
        this.lastFooter = "{\"translate\":\"\"}";
        this.createdTeams = new HashSet<>();
        this.initiated = false;
        this.removeColorCodesBuilder = new StringBuilder();
        this.player = player;
    }

    private void createAndAddMember(final String name, final String member) {
        final TeamPacket scoreboardTeamAdd = new TeamPacket("$" + name, "", "", Collections.singletonList(member), 0);
        scoreboardTeamAdd.sendToPlayer(this.player);
    }

    private void init() {
        if (!this.initiated) {
            final TabLayout initialLayout = TabLayout.createEmpty(this.player);

            for (final Player n : Bukkit.getOnlinePlayers()) {
                this.updateTabList(n.getName(), 0, ((CraftPlayer) n).getProfile(), 4);
            }

            for (final String s : initialLayout.getTabNames()) {
                this.updateTabList(s, 0, 0);
                final String teamName = s.replaceAll("§", "");
                if (!this.createdTeams.contains(teamName)) {
                    this.createAndAddMember(teamName, s);
                    this.createdTeams.add(teamName);
                }
            }
            this.initialLayout = initialLayout;
            this.initiated = true;
        }
    }

    private void updateScore(final String score, final String prefix, final String suffix) {
        final TeamPacket scoreboardTeamModify = new TeamPacket(score, prefix, suffix, null, 2);
        scoreboardTeamModify.sendToPlayer(this.player);
    }

    public void updateTabList(final String name, final int ping, final int action) {
        this.updateTabList(name, ping, TabUtils.getOrCreateProfile(name), action);
    }

    private void updateTabList(final String name, final int ping, final GameProfile profile, final int action) {
        final PlayerInfoPacketMod playerInfoPacketMod = new PlayerInfoPacketMod("$" + name, ping, profile, action, TabUtils.is18(player));
        playerInfoPacketMod.sendToPlayer(this.player);
    }

    private String[] splitString(final String line) {
        if (line.length() <= 16) {
            return new String[]{line, ""};
        }
        return new String[]{line.substring(0, 16), line.substring(16)};
    }

    void update() {
        if (TabHandler.getLayoutProvider() != null) {
            final TabLayout tabLayout = TabHandler.getLayoutProvider().provide(this.player);
            if (tabLayout == null) {
                if (this.initiated) {
                    this.reset();
                }
                return;
            }
            this.init();
            for (int y = 0; y < TabLayout.HEIGHT; ++y) {
                for (int x = 0; x < (tabLayout.is18() ? 4 : 3); ++x) {
                    final String entry = tabLayout.getStringAt(x, y);
                    final int ping = tabLayout.getPingAt(x, y);
                    final String entryName = this.initialLayout.getStringAt(x, y);
                    this.removeColorCodesBuilder.setLength(0);
                    this.removeColorCodesBuilder.append(entryName);
                    int j = 0;
                    for (int i = 0; i < this.removeColorCodesBuilder.length(); ++i) {
                        if ('§' != this.removeColorCodesBuilder.charAt(i)) {
                            this.removeColorCodesBuilder.setCharAt(j++, this.removeColorCodesBuilder.charAt(i));
                        }
                    }
                    this.removeColorCodesBuilder.delete(j, this.removeColorCodesBuilder.length());
                    final String teamName = "$" + this.removeColorCodesBuilder;
                    if (this.previousNames.containsKey(entryName)) {
                        if (!this.previousNames.get(entryName).equals(entry)) {
                            this.update(entryName, teamName, entry, ping);
                        } else if (this.previousPings.containsKey(entryName) && this.previousPings.get(entryName) != ping) {
                            this.updateTabList(entryName, ping, 2);
                            this.previousPings.put(entryName, ping);
                        }
                    } else {
                        this.update(entryName, teamName, entry, ping);
                    }
                }
            }
            boolean sendHeader = false;
            boolean sendFooter = false;
            final String header = tabLayout.getHeader();
            final String footer = tabLayout.getFooter();
            if (!header.equals(this.lastHeader)) {
                sendHeader = true;
            }
            if (!footer.equals(this.lastFooter)) {
                sendFooter = true;
            }

            if (tabLayout.is18() && TabUtils.is18(player) && (sendHeader || sendFooter)) {

                final ProtocolInjector.PacketTabHeader packet = new ProtocolInjector.PacketTabHeader(ChatSerializer.a(header), ChatSerializer.a(footer));
                ((CraftPlayer) this.player).getHandle().playerConnection.sendPacket(packet);
                this.lastHeader = header;
                this.lastFooter = footer;
            }
        }
    }

    private void reset() {
        this.initiated = false;

        for (final String s : this.initialLayout.getTabNames()) {
            this.updateTabList(s, 0, 4);
        }

        EntityPlayer ePlayer = ((CraftPlayer) this.player).getHandle();
        this.updateTabList(this.player.getName(), ePlayer.ping, ePlayer.getProfile(), 0);
        int count = 1;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (this.player == player) {
                continue;
            }
            if (count > this.initialLayout.getTabNames().length - 1) {
                break;
            }
            ePlayer = ((CraftPlayer) player).getHandle();
            this.updateTabList(player.getName(), ePlayer.ping, ePlayer.getProfile(), 0);
            ++count;
        }
    }

    private void update(final String entryName, final String teamName, final String entry, final int ping) {
        final String[] entryStrings = this.splitString(entry);
        String prefix = entryStrings[0];
        String suffix = entryStrings[1];
        if (!suffix.isEmpty()) {
            if (prefix.charAt(prefix.length() - 1) == '§') {
                prefix = prefix.substring(0, prefix.length() - 1);
                suffix = '§' + suffix;
            }
            String suffixPrefix = ChatColor.RESET.toString();
            if (!ChatColor.getLastColors(prefix).isEmpty()) {
                suffixPrefix = ChatColor.getLastColors(prefix);
            }
            if (suffix.length() <= 14) {
                suffix = suffixPrefix + suffix;
            } else {
                suffix = suffixPrefix + suffix.substring(0, 14);
            }
        }
        updateScore(teamName, prefix, suffix);
        updateTabList(entryName, (ping == 0 ? -1 : ping), 2);
        previousNames.put(entryName, entry);
        previousPings.put(entryName, ping);

//        for (Player online : Bukkit.getOnlinePlayers()) {
//            PacketPlayOutPlayerInfo.removePlayer(online.getDisguisedName(), online.getUniqueId());
//        }
    }

}
