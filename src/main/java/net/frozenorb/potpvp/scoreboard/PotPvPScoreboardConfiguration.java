package net.frozenorb.potpvp.scoreboard;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.scoreboard.assemble.config.ScoreboardConfiguration;
import net.frozenorb.potpvp.scoreboard.assemble.construct.TitleGetter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PotPvPScoreboardConfiguration extends ScoreboardConfiguration {

    public PotPvPScoreboardConfiguration() {
//        List<String> titles = new ArrayList<>(Arrays.asList("&cP", "&PR", "PRA", "PRAC", "PRACT", "PRACTI", "PRACTIC", "PRACTICE"));
        List<String> titles = new ArrayList<>(Arrays.asList("&fPRACTICE", "&cP&fRACTICE", "&cPR&fACTICE", "&cPRA&fCTICE", "&cPRAC&fTICE", "&cPRACT&fICE", "&cPRACTI&fCE", "&cPRACTIC&fE", "&cPRACTICE", "&fPRACTICE", "&cPRACTICE"));

        AtomicInteger atomicInteger = new AtomicInteger();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (atomicInteger.get() == titles.size()) atomicInteger.set(0);

                setTitleGetter(new TitleGetter(titles.get(atomicInteger.getAndIncrement())));
            }
        }.runTaskTimerAsynchronously(PotPvPSI.getInstance(), 0L, 10L);

        setScoreGetter(new MultiplexingScoreGetter(new MatchScoreGetter(), new LobbyScoreGetter()));
    }
}
