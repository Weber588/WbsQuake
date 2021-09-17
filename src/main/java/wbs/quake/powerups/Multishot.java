package wbs.quake.powerups;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.Gun;
import wbs.quake.player.QuakePlayer;
import wbs.quake.WbsQuake;

import java.util.Objects;

public class Multishot extends PowerUp {
    public Multishot(WbsQuake plugin, ConfigurationSection section, String directory) {
        super(plugin, section, directory);

        chance = section.getDouble("chance", 100);
    }

    private final double chance;

    @Override
    public void apply(QuakePlayer player) {
        Gun gun = player.getCurrentGun();
        gun.setMultishotChance(chance);
    }

    @Override
    public void removeFrom(QuakePlayer player) {
        Gun gun = player.getCurrentGun();
        if (chance == gun.getMultishotChance()) {
            gun.setMultishotChance(0);
            plugin.sendMessage(getDisplay() + " wears off...", player.getPlayer());
        }
    }

    @Override
    protected Material getDefaultItem() {
        return Material.CROSSBOW;
    }
    @Override
    protected String getDefaultDisplay() {
        return "Multi-Shot";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Multishot)) return false;
        if (!super.equals(o)) return false;
        Multishot multishot = (Multishot) o;
        return chance == multishot.chance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), chance);
    }
}
