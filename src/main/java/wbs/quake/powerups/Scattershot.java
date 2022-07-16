package wbs.quake.powerups;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.Gun;
import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;

public class Scattershot extends PowerUp {
    public Scattershot(WbsQuake plugin, ConfigurationSection section, String directory) {
        super(plugin, section, directory);

        amount = section.getInt("amount", 3);
    }

    private final int amount;

    @Override
    protected Material getDefaultItem() {
        return Material.QUARTZ;
    }

    @Override
    protected String getDefaultDisplay() {
        return "Scattershot";
    }

    @Override
    public void apply(QuakePlayer player) {
        Gun gun = player.getCurrentGun();
        gun.setScattershot(amount);
    }

    @Override
    public void removeFrom(QuakePlayer player) {
        Gun gun = player.getCurrentGun();
        if (amount == gun.getScattershot()) {
            gun.setScattershot(0);
            plugin.sendMessage(getDisplay() + " wears off...", player.getPlayer());
        }
    }
}
