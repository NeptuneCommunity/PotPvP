package net.frozenorb.potpvp.party.menu.otherparties;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.lobby.LobbyHandler;
import net.frozenorb.potpvp.party.Party;
import net.frozenorb.potpvp.party.PartyHandler;
import net.frozenorb.potpvp.setting.Setting;
import net.frozenorb.potpvp.setting.SettingHandler;
import net.frozenorb.potpvp.util.menu.engine.Button;
import net.frozenorb.potpvp.util.menu.engine.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public final class OtherPartiesMenu extends Menu {

    public OtherPartiesMenu() {
        setPlaceholder(true);
        setAutoUpdate(true);
    }

    @Override
    public String getTitle(Player player) {
        return "Other parties";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        SettingHandler settingHandler = PotPvPSI.getInstance().getSettingHandler();
        PartyHandler partyHandler = PotPvPSI.getInstance().getPartyHandler();
        LobbyHandler lobbyHandler = PotPvPSI.getInstance().getLobbyHandler();

        Map<Integer, Button> buttons = new HashMap<>();
        List<Party> parties = new ArrayList<>(partyHandler.getParties());
        int index = 0;

        parties.sort(Comparator.comparing(p -> p.getMembers().size()));

        for (Party party : parties) {
            if (party.isMember(player.getUniqueId())) {
                continue;
            }

            if (!lobbyHandler.isInLobby(Bukkit.getPlayer(party.getLeader()))) {
                continue;
            }

            if (!settingHandler.getSetting(Bukkit.getPlayer(party.getLeader()), Setting.RECEIVE_DUELS)) {
                continue;
            }

            /* if (PotPvPSI.getInstance().getTournamentHandler().isInTournament(party)) {
                continue;
            } */

            buttons.put(index++, new OtherPartyButton(party));
        }

        return buttons;
    }

    // we lock the size of this inventory at full, otherwise we'll have
    // issues if it 'grows' into the next line while it's open (say we open
    // the menu with 8 entries, then it grows to 11 [and onto the second row]
    // - this breaks things)
    @Override
    public int size(Map<Integer, Button> buttons) {
        return 9 * 6;
    }
}