package wbs.quake.powerups;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;

public class ReloadPowerUp extends PowerUp {
    public ReloadPowerUp(WbsQuake plugin, ConfigurationSection section, String directory) {
        super(plugin, section, directory);

        freeReloads = section.getInt("free-reloads", 1);
    }

    private final int freeReloads;

    @Override
    protected Material getDefaultItem() {
        return Material.TIPPED_ARROW;
    }

    @Override
    protected String getDefaultDisplay() {
        return "Instant Reload";
    }

    @Override
    public void apply(QuakePlayer player) {
        player.getCurrentGun().instantReload(player, freeReloads);
    }

    @Override
    public void removeFrom(QuakePlayer player) {
        player.getCurrentGun().instantReload(player, 0);
    }
}
