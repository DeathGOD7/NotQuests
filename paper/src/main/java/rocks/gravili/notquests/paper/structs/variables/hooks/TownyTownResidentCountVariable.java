package rocks.gravili.notquests.paper.structs.variables.hooks;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.structs.variables.Variable;

import java.util.List;

public class TownyTownResidentCountVariable extends Variable<Integer> {
    public TownyTownResidentCountVariable(NotQuests main) {
        super(main);
    }

    @Override
    public Integer getValue(Player player, Object... objects) {
        if (!main.getIntegrationsManager().isTownyEnabled()) {
            return 0;
        }
        if (player != null) {
            Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
            if (resident != null && resident.getTownOrNull() != null && resident.hasTown()) {
                Town town = resident.getTownOrNull();
                return town.getNumResidents();
            }  else {
                return 0;
            }

        } else {
            return 0;

        }
    }

    @Override
    public boolean setValueInternally(Integer newValue, Player player, Object... objects) {
        return false;
    }

    @Override
    public List<String> getPossibleValues(Player player, Object... objects) {
        return null;
    }

    @Override
    public String getPlural() {
        return "Residents in Town";
    }

    @Override
    public String getSingular() {
        return "Resident in Town";
    }
}
