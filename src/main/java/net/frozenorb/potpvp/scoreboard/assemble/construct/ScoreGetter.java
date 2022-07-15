package net.frozenorb.potpvp.scoreboard.assemble.construct;

import org.bukkit.entity.Player;

import java.util.List;

public interface ScoreGetter {

    void getScores(List<String> linkedList, Player player);

}