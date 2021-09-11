package wbs.quake.cosmetics;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.QuakePlayer;

import java.util.List;

public class DeathEffect extends SelectableCosmetic<DeathEffect> {

    public DeathEffect(String id, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);
    }

    public DeathEffect(ConfigurationSection section, String directory) {
        super(section, directory);
    }

    @Override
    public CosmeticType getCosmeticType() {
        return CosmeticType.DEATH_EFFECT;
    }

    @Override
    public void onSelect(QuakePlayer player, PlayerCosmetics cosmetics) {

    }
}
