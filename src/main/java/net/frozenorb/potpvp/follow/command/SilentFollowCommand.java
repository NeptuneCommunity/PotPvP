package net.frozenorb.potpvp.follow.command;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.match.command.LeaveCommand;
import net.frozenorb.potpvp.util.command.annotation.Command;
import net.frozenorb.potpvp.util.command.annotation.Param;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class SilentFollowCommand {

    @Command(names = "silentfollow", permission = "potpvp.silent")
    public static void silentfollow(Player sender, @Param(name = "target") Player target) {
        sender.setMetadata("ModMode", new FixedMetadataValue(PotPvPSI.getInstance(), true));
        sender.setMetadata("invisible", new FixedMetadataValue(PotPvPSI.getInstance(), true));

        if (PotPvPSI.getInstance().getPartyHandler().hasParty(sender)) {
            LeaveCommand.leave(sender);
        }

        FollowCommand.follow(sender, target);
    }

}
