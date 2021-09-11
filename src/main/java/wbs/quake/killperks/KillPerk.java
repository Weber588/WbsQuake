package wbs.quake.killperks;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.PlayerTargeter;
import wbs.quake.WbsQuake;
import wbs.quake.menus.MenuSelectable;
import wbs.quake.menus.SelectableSlot;
import wbs.quake.player.QuakePlayer;
import wbs.quake.powerups.*;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;

import java.util.LinkedList;
import java.util.List;

public abstract class KillPerk extends MenuSelectable {

    public static KillPerk getPerk(ConfigurationSection section, String directory) {
        KillPerkType type = WbsEnums.getEnumFromString(KillPerkType.class, section.getString("type", "POTION"));

        switch (type) {
            case POTION:
                return new PotionKillPerk(section, directory);
        }

        return null;
    }

    public enum KillPerkType {
        POTION
    }

    protected final double chance;

    public KillPerk(String id, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);

        chance = 100;
    }

    public KillPerk(ConfigurationSection section, String directory) {
        super(section, directory);

        chance = section.getDouble("chance", 100);
    }

    @Override
    public List<String> updateLore(List<String> lore) {
        lore.add("&6Chance: &h" + chance + "%");
        return lore;
    }

    public void apply(QuakePlayer player, QuakePlayer victim) {
        if (WbsMath.chance(chance)) {
            internalApply(player, victim);
        }
    }

    /**
     * Apply this perk to a player who just killed another.
     * @param player The player who has this perk active
     * @param victim The killed player who may not
     */
    protected abstract void internalApply(QuakePlayer player, QuakePlayer victim);

    @Override
    public String getPermission() {
        return "wbsquake.killperks." + getId();
    }
}
