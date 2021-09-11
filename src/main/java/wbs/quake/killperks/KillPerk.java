package wbs.quake.killperks;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.menus.MenuSelectable;
import wbs.quake.menus.SelectableSlot;
import wbs.quake.player.QuakePlayer;

import java.util.List;

public abstract class KillPerk extends MenuSelectable {

    public KillPerk(String id, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);
    }

    public KillPerk(ConfigurationSection section, String directory) {
        super(section, directory);
    }

    /**
     * Apply this perk to a player who just killed another.
     * @param player The player who has this perk active
     * @param victim The killed player who may not
     */
    public abstract void apply(QuakePlayer player, QuakePlayer victim);

    public abstract SelectableSlot<KillPerk> buildSlot();
}
