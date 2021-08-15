package wbs.quake.powerups;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.Gun;
import wbs.quake.player.QuakePlayer;
import wbs.quake.WbsQuake;

import java.util.Objects;

public class RapidFire extends PowerUp {
    public RapidFire(WbsQuake plugin, ConfigurationSection section, String directory) {
        super(plugin, section, directory);

        multiplier = section.getInt("multiplier", 2);
    }

    private final double multiplier;

    @Override
    public void runOn(QuakePlayer player) {
        Gun gun = player.getCurrentGun();
        gun.addCooldownModifier(multiplier);
    }

    @Override
    public void remove(QuakePlayer player) {
        Gun gun = player.getCurrentGun();
        gun.removeCooldownModifier(multiplier);
        plugin.sendMessage(getDisplay() + " wears off...", player.getPlayer());
    }

    @Override
    protected Material getDefaultItem() {
        return Material.DIAMOND_HOE;
    }
    @Override
    protected String getDefaultDisplay() {
        return "Rapid Fire";
    }

    @Override
    public void writeToConfig(ConfigurationSection section, String path, Location location) {
        super.writeToConfig(section, path, location);
        section.set(path + ".multiplier", multiplier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RapidFire)) return false;
        if (!super.equals(o)) return false;
        RapidFire rapidFire = (RapidFire) o;
        return Double.compare(rapidFire.multiplier, multiplier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), multiplier);
    }
}
