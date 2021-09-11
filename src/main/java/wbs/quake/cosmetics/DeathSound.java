package wbs.quake.cosmetics;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.QuakePlayer;

import java.util.List;

public class DeathSound extends SelectableSound<DeathSound> {
    public DeathSound(String id, Material material, String display, String permission, List<String> description, double price, Sound sound, float volume, float pitch) {
        super(id, material, display, permission, description, price, sound, volume, pitch);
    }

    public DeathSound(ConfigurationSection section, String directory) {
        super(section, directory);
    }

    @Override
    public CosmeticType getCosmeticType() {
        return CosmeticType.DEATH_SOUND;
    }

    @Override
    public void onSelect(QuakePlayer player, PlayerCosmetics cosmetics) {
        cosmetics.deathSound = this;
    }
}
