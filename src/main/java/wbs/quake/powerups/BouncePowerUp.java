package wbs.quake.powerups;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.Gun;
import wbs.quake.QuakePlayer;
import wbs.quake.WbsQuake;

import java.util.Objects;

public class BouncePowerUp extends PowerUp {
    public BouncePowerUp(WbsQuake plugin, ConfigurationSection section, String directory) {
        super(plugin, section, directory);

        bounces = section.getInt("bounces", 2);
    }
    private final int bounces;

    @Override
    public void runOn(QuakePlayer player) {
        player.getCurrentGun().setBounces(bounces);
    }

    @Override
    public void remove(QuakePlayer player) {
        Gun gun = player.getCurrentGun();
        if (bounces == gun.getBounces()) {
            gun.setBounces(0);
            plugin.sendMessage(getDisplay() + " wears off...", player.getPlayer());
        }
    }

    @Override
    protected Material getDefaultItem() {
        return Material.SLIME_BALL;
    }
    @Override
    protected String getDefaultDisplay() {
        return "Bounce-Shot";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BouncePowerUp)) return false;
        if (!super.equals(o)) return false;
        BouncePowerUp that = (BouncePowerUp) o;
        return bounces == that.bounces;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bounces);
    }
}
