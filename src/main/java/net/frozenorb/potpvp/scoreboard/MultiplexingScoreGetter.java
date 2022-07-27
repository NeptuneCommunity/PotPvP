package net.frozenorb.potpvp.scoreboard;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.scoreboard.engine.construct.ScoreGetter;
import net.frozenorb.potpvp.setting.Setting;
import net.frozenorb.potpvp.setting.SettingHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;

final class MultiplexingScoreGetter implements ScoreGetter {


    private final BiConsumer<Player, List<String>> matchScoreGetter;
    private final BiConsumer<Player, List<String>> lobbyScoreGetter;

    MultiplexingScoreGetter(BiConsumer<Player, List<String>> matchScoreGetter, BiConsumer<Player, List<String>> lobbyScoreGetter) {
        this.matchScoreGetter = matchScoreGetter;
        this.lobbyScoreGetter = lobbyScoreGetter;
    }

    @Override
    public void getScores(List<String> scores, Player player) {
        if (PotPvPSI.getInstance() == null) return;
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        SettingHandler settingHandler = PotPvPSI.getInstance().getSettingHandler();

        if (!settingHandler.getSetting(player, Setting.SHOW_SCOREBOARD) && matchHandler.isPlayingOrSpectatingMatch(player)) return;

        scores.add("&a&7&m--------------------");

        if (matchHandler.isPlayingOrSpectatingMatch(player)) {
            matchScoreGetter.accept(player, scores);
        } else {
            lobbyScoreGetter.accept(player, scores);
        }

        scores.add("&a&7&m");
        scores.add(ChatColor.GRAY + "star.neptune");
        if (player.hasMetadata("ModMode")) {
            scores.add(ChatColor.GRAY + "In Silent Mode");
        }
        scores.add("&a&7&m--------------------");
    }
}