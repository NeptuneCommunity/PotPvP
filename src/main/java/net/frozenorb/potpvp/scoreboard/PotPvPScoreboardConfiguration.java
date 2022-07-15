package net.frozenorb.potpvp.scoreboard;

import net.frozenorb.potpvp.scoreboard.assemble.config.ScoreboardConfiguration;
import net.frozenorb.potpvp.scoreboard.assemble.construct.TitleGetter;

public class PotPvPScoreboardConfiguration extends ScoreboardConfiguration {

    public PotPvPScoreboardConfiguration() {
        this.setTitleGetter(new TitleGetter("&cPractice"));
        this.setScoreGetter(new MultiplexingScoreGetter(new MatchScoreGetter(), new LobbyScoreGetter()));
    }

}
