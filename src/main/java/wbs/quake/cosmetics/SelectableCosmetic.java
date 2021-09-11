package wbs.quake.cosmetics;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.QuakeSettings;
import wbs.quake.WbsQuake;
import wbs.quake.menus.MenuSelectable;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class SelectableCosmetic extends MenuSelectable {

    public SelectableCosmetic(String id, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);
    }

    public SelectableCosmetic(ConfigurationSection section, String directory) {
        super(section, directory);
    }

    @Override
    public String getPermission() {
        return "wbsquake.cosmetics." + getCosmeticType().name().toLowerCase() + "." + id;
    }

    public abstract CosmeticType getCosmeticType();

    /**
     * Called when the cosmetic is selected by the given player
     * @param player The player who selected this cosmetic
     */
    public abstract void onSelect(QuakePlayer player, PlayerCosmetics cosmetics);
}
