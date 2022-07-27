package net.frozenorb.potpvp.scoreboard;

import com.google.common.collect.ImmutableMap;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.HealingMethod;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.match.MatchState;
import net.frozenorb.potpvp.match.MatchTeam;
import net.frozenorb.potpvp.match.listener.MatchBoxingListener;
import net.frozenorb.potpvp.util.PlayerUtils;
import net.frozenorb.potpvp.util.TimeUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiConsumer;

// the list here must be viewed as rendered javadoc to make sense. In IntelliJ, click on
// 'MatchScoreGetter' and press Control+Q

/**
 * Implements the scoreboard as defined in {@link net.frozenorb.potpvp.scoreboard}<br />
 * This class is divided up into multiple prodcedures to reduce overall complexity<br /><br />
 * <p>
 * Although there are many possible outcomes, for a 4v4 match this code would take the
 * following path:<br /><br />
 *
 * <ul>
 *   <li>accept()</li>
 *   <ul>
 *     <li>renderParticipantLines()</li>
 *     <ul>
 *       <li>render4v4MatchLines()</li>
 *       <ul>
 *         <li>renderTeamMemberOverviewLines()</li>
 *         <li>renderTeamMemberOverviewLines()</li>
 *       </ul>
 *     </ul>
 *     <li>renderMetaLines()</li>
 *   </ul>
 * </ul>
 */
final class MatchScoreGetter implements BiConsumer<Player, List<String>> {

    // we can't count heals on an async thread so we use
    // a task to count and then report values (via this map) to
    // the scoreboard thread
    private Map<UUID, Integer> healsLeft = ImmutableMap.of();

    MatchScoreGetter() {
        Bukkit.getScheduler().runTaskTimer(PotPvPSI.getInstance(), () -> {
            MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
            Map<UUID, Integer> newHealsLeft = new HashMap<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                Match playing = matchHandler.getMatchPlaying(player);

                if (playing == null) {
                    continue;
                }

                HealingMethod healingMethod = playing.getKitType().getHealingMethod();

                if (healingMethod == null) {
                    continue;
                }

                int count = healingMethod.count(player.getInventory().getContents());
                newHealsLeft.put(player.getUniqueId(), count);
            }

            this.healsLeft = newHealsLeft;
        }, 10L, 10L);
    }

    @Override
    public void accept(Player player, List<String> scores) {
        Optional<UUID> followingOpt = PotPvPSI.getInstance().getFollowHandler().getFollowing(player);
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        Match match = matchHandler.getMatchPlayingOrSpectating(player);

        // this method shouldn't have even been called if
        // they're not in a match
        if (match == null) {
            followingOpt.ifPresent(uuid -> scores.add("&dFollowing: *&7" + PotPvPSI.getInstance().uuidCache.name(uuid)));

            if (player.hasMetadata("ModMode")) {
                scores.add(ChatColor.GRAY.toString() + ChatColor.BOLD + "In Silent Mode");
            }
            return;
        }

        boolean participant = match.getTeam(player.getUniqueId()) != null;

        if (participant) {
            renderParticipantLines(scores, match, player);
        } else {
            MatchTeam previousTeam = match.getPreviousTeam(player.getUniqueId());
            renderSpectatorLines(scores, match, previousTeam);
        }

        renderMetaLines(scores, match, participant);

        if (match.getKitType().getId().equalsIgnoreCase("Boxing")) {
            renderBoxingLines(scores, match, player);
        }

        if (match.getKitType().getId().equalsIgnoreCase("Bridges")) {
            renderBridgesLines(scores, match, player);
        }

        renderPingLines(scores, match, player);

        // this definitely can be a .ifPresent, however creating the new lambda that often
        // was causing some performance issues, so we do this less pretty (but more efficent)
        // check (we can't define the lambda up top and reference because we reference the
        // scores variable)
        followingOpt.ifPresent(uuid -> scores.add("&dFollowing: *&7" + PotPvPSI.getInstance().uuidCache.name(uuid)));

        if (player.hasMetadata("ModMode")) {
            scores.add(ChatColor.GRAY.toString() + ChatColor.BOLD + "In Silent Mode");
        }
    }

    private void renderParticipantLines(List<String> scores, Match match, Player player) {
        List<MatchTeam> teams = match.getTeams();

        // only render scoreboard if we have two teams
        if (teams.size() != 2) {
            return;
        }

        // this method won't be called if the player isn't a participant
        MatchTeam ourTeam = match.getTeam(player.getUniqueId());
        MatchTeam otherTeam = teams.get(0) == ourTeam ? teams.get(1) : teams.get(0);

        // we use getAllMembers instead of getAliveMembers to avoid
        // mid-match scoreboard changes as players die / disconnect
        int ourTeamSize = ourTeam.getAllMembers().size();
        int otherTeamSize = otherTeam.getAllMembers().size();

        if (ourTeamSize == 1 && otherTeamSize == 1) {
            render1v1MatchLines(scores, otherTeam);
        } else if (ourTeamSize <= 2 && otherTeamSize <= 2) {
            render2v2MatchLines(scores, ourTeam, otherTeam, player, match.getKitType().getHealingMethod());
        } else if (ourTeamSize <= 4 && otherTeamSize <= 4) {
            render4v4MatchLines(scores, ourTeam, otherTeam);
        } else if (ourTeam.getAllMembers().size() <= 9) {
            renderLargeMatchLines(scores, ourTeam, otherTeam);
        } else {
            renderJumboMatchLines(scores, ourTeam, otherTeam);
        }

    }

    private void render1v1MatchLines(List<String> scores, MatchTeam otherTeam) {
        scores.add("&c&lOpponent: &f" + PotPvPSI.getInstance().uuidCache.name(otherTeam.getFirstMember()));

    }

    private void render2v2MatchLines(List<String> scores, MatchTeam ourTeam, MatchTeam otherTeam, Player player, HealingMethod healingMethod) {
        // 2v2, but potentially 1v2 / 1v1 if players have died
        UUID partnerUuid = null;

        for (UUID teamMember : ourTeam.getAllMembers()) {
            if (teamMember != player.getUniqueId()) {
                partnerUuid = teamMember;
                break;
            }
        }

        if (partnerUuid != null) {
            String healthStr;
            String healsStr;
            String namePrefix;

            if (ourTeam.isAlive(partnerUuid)) {
                Player partnerPlayer = Bukkit.getPlayer(partnerUuid); // will never be null (or isAlive would've returned false)
                double health = Math.round(partnerPlayer.getHealth()) / 2D;
                int heals = healsLeft.getOrDefault(partnerUuid, 0);

                ChatColor healthColor;
                ChatColor healsColor;

                if (health > 8) {
                    healthColor = ChatColor.GREEN;
                } else if (health > 6) {
                    healthColor = ChatColor.YELLOW;
                } else if (health > 4) {
                    healthColor = ChatColor.GOLD;
                } else if (health > 1) {
                    healthColor = ChatColor.RED;
                } else {
                    healthColor = ChatColor.DARK_RED;
                }

                if (heals > 20) {
                    healsColor = ChatColor.GREEN;
                } else if (heals > 12) {
                    healsColor = ChatColor.YELLOW;
                } else if (heals > 8) {
                    healsColor = ChatColor.GOLD;
                } else if (heals > 3) {
                    healsColor = ChatColor.RED;
                } else {
                    healsColor = ChatColor.DARK_RED;
                }

                namePrefix = "&a";
                healthStr = healthColor.toString() + health + " *❤*" + ChatColor.GRAY;

                if (healingMethod != null) {
                    healsStr = " &l⏐ " + healsColor + heals + " " + (heals == 1 ? healingMethod.getShortSingular() : healingMethod.getShortPlural());
                } else {
                    healsStr = "";
                }
            } else {
                namePrefix = "&7&m";
                healthStr = "&4RIP";
                healsStr = "";
            }

            scores.add(namePrefix + PotPvPSI.getInstance().uuidCache.name(partnerUuid));
            scores.add(healthStr + healsStr);
            scores.add("&b");
        }

        scores.add("&c&lOpponents");
        scores.addAll(renderTeamMemberOverviewLines(otherTeam));

        // Removes the space
        if (PotPvPSI.getInstance().getMatchHandler().getMatchPlaying(player).getState() == MatchState.IN_PROGRESS) {
            scores.add("&c");
        }
    }

    private void render4v4MatchLines(List<String> scores, MatchTeam ourTeam, MatchTeam otherTeam) {
        // Above a 2v2, but up to a 4v4.
        scores.add("&aTeam &a(" + ourTeam.getAliveMembers().size() + "/" + ourTeam.getAllMembers().size() + ")");
        scores.addAll(renderTeamMemberOverviewLinesWithHearts(ourTeam));
        scores.add("&b");
        scores.add("&cOpponents &c(" + otherTeam.getAliveMembers().size() + "/" + otherTeam.getAllMembers().size() + ")");
        scores.addAll(renderTeamMemberOverviewLines(otherTeam));
        if (PotPvPSI.getInstance().getMatchHandler().getMatchPlaying(Bukkit.getPlayer(ourTeam.getFirstAliveMember())).getState() == MatchState.IN_PROGRESS) {
            scores.add("&c");
        }
    }

    private void renderLargeMatchLines(List<String> scores, MatchTeam ourTeam, MatchTeam otherTeam) {
        // We just display THEIR team's names, and the other team is a number.
        scores.add("&aTeam &a(" + ourTeam.getAliveMembers().size() + "/" + ourTeam.getAllMembers().size() + ")");
        scores.addAll(renderTeamMemberOverviewLinesWithHearts(ourTeam));
        scores.add("&b");
        scores.add("&cOpponents: &f" + otherTeam.getAliveMembers().size() + "/" + otherTeam.getAllMembers().size());
    }

    private void renderJumboMatchLines(List<String> scores, MatchTeam ourTeam, MatchTeam otherTeam) {
        // We just display numbers.
        scores.add("&aTeam: &f" + ourTeam.getAliveMembers().size() + "/" + ourTeam.getAllMembers().size());
        scores.add("&cOpponents: &f" + otherTeam.getAliveMembers().size() + "/" + otherTeam.getAllMembers().size());
    }

    private void renderSpectatorLines(List<String> scores, Match match, MatchTeam oldTeam) {
        String rankedStr = match.isRanked() ? " (R)" : "";
        scores.add("&eKit: &f" + match.getKitType().getColoredDisplayName() + rankedStr);

        List<MatchTeam> teams = match.getTeams();

        if (match.getKitType().getId().equalsIgnoreCase("Bridges") || match.getKitType().getId().equalsIgnoreCase("Boxing")) return;

        // only render team overview if we have two teams
        if (teams.size() == 2) {
            MatchTeam teamOne = teams.get(0);
            MatchTeam teamTwo = teams.get(1);

            if (teamOne.getAllMembers().size() != 1 && teamTwo.getAllMembers().size() != 1) {
                // spectators who were on a team see teams as they releate
                // to them, not just one/two.
                if (oldTeam == null) {
                    scores.add("&dTeam One: &f" + teamOne.getAliveMembers().size() + "/" + teamOne.getAllMembers().size());
                    scores.add("&bTeam Two: &f" + teamTwo.getAliveMembers().size() + "/" + teamTwo.getAllMembers().size());
                } else {
                    MatchTeam otherTeam = oldTeam == teamOne ? teamTwo : teamOne;

                    scores.add("&aTeam: &f" + oldTeam.getAliveMembers().size() + "/" + oldTeam.getAllMembers().size());
                    scores.add("&cOpponents: &f" + otherTeam.getAliveMembers().size() + "/" + otherTeam.getAllMembers().size());
                }
            }
        }
    }

    private void renderMetaLines(List<String> scores, Match match, boolean participant) {
        Date startedAt = match.getStartedAt();
        Date endedAt = match.getEndedAt();
        String formattedDuration;

        // short circuit for matches which are still counting down
        // or which ended before they started (if a player disconnects
        // during countdown)
        if (startedAt == null) {
            return;
        } else {
            // we go from when it started to either now (if it's in progress)
            // or the timestamp at which the match actually ended
            formattedDuration = TimeUtils.formatLongIntoMMSS(ChronoUnit.SECONDS.between(
                    startedAt.toInstant(),
                    endedAt == null ? Instant.now() : endedAt.toInstant()
            ));
        }

        // spectators don't have any bold entries on their scoreboard
        scores.add("&6&lDuration: &f" + formattedDuration);
    }

    private void renderPingLines(List<String> scores, Match match, Player ourPlayer) {
        List<MatchTeam> teams = match.getTeams();
        if (teams.size() == 2) {
            MatchTeam teamOne = teams.get(0);
            MatchTeam teamTwo = teams.get(1);

            Set<UUID> teamOnePlayers = teamOne.getAllMembers();
            Set<UUID> teamTwoPlayers = teamTwo.getAllMembers();

            if (teamOnePlayers.size() == 1 && teamTwoPlayers.size() == 1) {
                if (teamOne.getAliveMembers().contains(ourPlayer.getUniqueId()) || teamTwo.getAliveMembers().contains(ourPlayer.getUniqueId())) {
                    scores.add("&7&b&4"); // spaceer
                    scores.add("&aYour Ping: &f" + PlayerUtils.getPing(ourPlayer) + " ms");
                    Player otherPlayer = Bukkit.getPlayer(match.getTeam(ourPlayer.getUniqueId()) == teamOne ? teamTwo.getFirstMember() : teamOne.getFirstMember());
                    if (otherPlayer == null) return;

                    scores.add("&cTheir Ping: &f" + PlayerUtils.getPing(otherPlayer) + " ms");
                } else {
                    Player teamOneBukkit = Bukkit.getPlayer(teamOne.getFirstMember());
                    Player teamTwoBukkit = Bukkit.getPlayer(teamTwo.getFirstMember());
                    scores.add("&7&b&4"); // spaceer

                    if (teamOneBukkit != null) {
                        scores.add("&d" + teamOneBukkit.getName() + "'s Ping: &f" + PlayerUtils.getPing(teamOneBukkit) + " ms");
                    }
                    if (teamTwoBukkit != null) {
                        scores.add("&b" + teamTwoBukkit.getName() + "'s Ping: &f" + PlayerUtils.getPing(teamTwoBukkit) + " ms");
                    }
                }
            }
        }
    }

    private void renderBoxingLines(List<String> scores, Match match, Player player) {
        List<MatchTeam> teams = match.getTeams();
        MatchTeam teamOne = teams.get(0);
        MatchTeam teamTwo = teams.get(1);

        UUID ourPlayerUUID = player.getUniqueId();
        UUID otherPlayerUUID = match.getTeam(ourPlayerUUID) == teamOne ? teamTwo.getFirstMember() : teamOne.getFirstMember();

        int ourHits = match.getTotalHits().getOrDefault(ourPlayerUUID, 0);
        int ourCombo = match.getCombos().getOrDefault(ourPlayerUUID, 0);

        int themCombo = match.getCombos().getOrDefault(otherPlayerUUID, 0);
        int themHits = match.getTotalHits().getOrDefault(otherPlayerUUID, 0);

        String difference = (ourHits >= themHits ? "&a(+" + (ourHits - themHits) + ")" : "&c(-" + (themHits - ourHits) + ")");
        MatchTeam oldTeam = match.getPreviousTeam(ourPlayerUUID);

        if (match.isSpectator(ourPlayerUUID)) {
            if (oldTeam == null) {
                scores.add("&b&l&d&o&d");
                scores.add("&dTeam One: &f" + teamOne.getAliveMembers() + "/" + teamOne.getAllMembers());
                scores.add("&fHits: &e" + teamOne.getHitMap().getOrDefault(teamOne, 0) + "/" + (teamTwo.getAllMembers().size() * 100));
                scores.add("&d&e&a&d&l&c");
                scores.add("&bTeam Two: &f" + teamTwo.getAliveMembers() + "/" + teamTwo.getAllMembers());
                scores.add("&fHits: &e" + teamTwo.getHitMap().getOrDefault(teamTwo, 0) + "/" + (teamOne.getAllMembers().size() * 100));
            } else {
                MatchTeam otherTeam = oldTeam == teamOne ? teamTwo : teamOne;

                scores.add("&b&l&d&o&d");
                scores.add("&aTeam: &f" + oldTeam.getAliveMembers() + "/" + oldTeam.getAllMembers());
                scores.add("&fHits: &e" + oldTeam.getHitMap().getOrDefault(oldTeam, 0) + "/" + (otherTeam.getAllMembers().size() * 100));
                scores.add("&d&e&a&d&l&c");
                scores.add("&cOpponents: &f" + otherTeam.getAliveMembers() + "/" + otherTeam.getAllMembers());
                scores.add("&fHits: &e" + otherTeam.getHitMap().getOrDefault(otherTeam, 0) + "/" + (oldTeam.getAllMembers().size() * 100));
            }
            return;
        }

        scores.add("&7&b&4&d&l"); // spaceer
        scores.add(ChatColor.RED + "Hits: " + difference);
        scores.add(" &aYou: &f" + ourHits + ((ourCombo >= 2 && match.getLastHit().containsKey(ourPlayerUUID)) ? " &e(" + ourCombo + " Combo" + ")" : ""));
        scores.add(" &cThem: &f" + themHits + ((themCombo >= 2 && match.getLastHit().containsKey(otherPlayerUUID)) ? " &e(" + themCombo + " Combo" + ")" : ""));
    }

    private void renderBridgesLines(List<String> scores, Match match, Player player) {
        List<MatchTeam> teams = match.getTeams();

        MatchTeam firstTeam = teams.get(0);
        MatchTeam secondTeam = teams.get(1);

        int ourKills = match.getKills().getOrDefault(player.getUniqueId(), 0);
        int ourGoals = match.getGoals().getOrDefault(player.getUniqueId(), 0);

        if (match.isSpectator(player.getUniqueId())) {
            scores.add("&7&b&4&d&l"); // spaceer
            scores.add("&c[R] " + StringUtils.repeat("&c⬤", secondTeam.getRoundsWon()) + StringUtils.repeat("&7⬤", 3 - secondTeam.getRoundsWon()));
            scores.add("&9[B] " + StringUtils.repeat("&9⬤", firstTeam.getRoundsWon()) + StringUtils.repeat("&7⬤", 3 - firstTeam.getRoundsWon()));
        } else {
            scores.add("&c[R] " + StringUtils.repeat("&c⬤", secondTeam.getRoundsWon()) + StringUtils.repeat("&7⬤", 3 - secondTeam.getRoundsWon()));
            scores.add("&9[B] " + StringUtils.repeat("&9⬤", firstTeam.getRoundsWon()) + StringUtils.repeat("&7⬤", 3 - firstTeam.getRoundsWon()));
            scores.add("&7&b&4&d&l"); // spaceer
            scores.add("&aKills: &f" + ourKills);
            scores.add("&aGoals: &f" + ourGoals);
        }
    }

    /* Returns the names of all alive players, colored + indented, followed
       by the names of all dead players, colored + indented. */

    private List<String> renderTeamMemberOverviewLinesWithHearts(MatchTeam team) {
        List<String> aliveLines = new ArrayList<>();
        List<String> deadLines = new ArrayList<>();

        // seperate lists to sort alive players before dead
        // + color differently
        for (UUID teamMember : team.getAllMembers()) {
            if (team.isAlive(teamMember)) {
                aliveLines.add(" &f" + PotPvPSI.getInstance().uuidCache.name(teamMember) + " " + getHeartString(team, teamMember));
            } else {
                deadLines.add(" &7&m" + PotPvPSI.getInstance().uuidCache.name(teamMember));
            }
        }

        List<String> result = new ArrayList<>();

        result.addAll(aliveLines);
        result.addAll(deadLines);

        return result;
    }

    private List<String> renderTeamMemberOverviewLines(MatchTeam team) {
        List<String> aliveLines = new ArrayList<>();
        List<String> deadLines = new ArrayList<>();

        // seperate lists to sort alive players before dead
        // + color differently
        for (UUID teamMember : team.getAllMembers()) {
            if (team.isAlive(teamMember)) {
                aliveLines.add(" &f" + PotPvPSI.getInstance().uuidCache.name(teamMember));
            } else {
                deadLines.add(" &7&m" + PotPvPSI.getInstance().uuidCache.name(teamMember));
            }
        }

        List<String> result = new ArrayList<>();

        result.addAll(aliveLines);
        result.addAll(deadLines);

        return result;
    }

    private String getHeartString(MatchTeam ourTeam, UUID partnerUuid) {
        if (partnerUuid != null) {
            String healthStr;

            if (ourTeam.isAlive(partnerUuid)) {
                Player partnerPlayer = Bukkit.getPlayer(partnerUuid); // will never be null (or isAlive would've returned false)
                double health = Math.round(partnerPlayer.getHealth()) / 2D;

                ChatColor healthColor;

                if (health > 8) {
                    healthColor = ChatColor.GREEN;
                } else if (health > 6) {
                    healthColor = ChatColor.YELLOW;
                } else if (health > 4) {
                    healthColor = ChatColor.GOLD;
                } else if (health > 1) {
                    healthColor = ChatColor.RED;
                } else {
                    healthColor = ChatColor.DARK_RED;
                }

                healthStr = healthColor + "(" + health + " ❤)";
            } else {
                healthStr = "&4(RIP)";
            }

            return healthStr;
        } else {
            return "&4(RIP)";
        }
    }
}