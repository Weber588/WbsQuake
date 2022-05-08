package wbs.quake.powerups;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;

import java.util.LinkedList;
import java.util.List;

public class RandomPowerUp extends PowerUp {

    private final String directory;

    public RandomPowerUp(WbsQuake plugin, ConfigurationSection section, String directory) {
        super(plugin, section, directory);
        this.directory = directory;

        powerUpNames.addAll(section.getStringList("power-ups"));

        if (powerUpNames.isEmpty()) {
            throw new InvalidConfigurationException("You must specify a list of powerup types!");
        }
    }

    private final List<String> powerUpNames = new LinkedList<>();
    private final List<PowerUp> powerUps = new LinkedList<>();

    private PowerUp current;

    @Override
    protected Material getDefaultItem() {
        return Material.CHEST;
    }

    @Override
    protected String getDefaultDisplay() {
        return "Random";
    }

    @Override
    public String getDisplay() {
        return current.getDisplay();
    }

    @Override
    public void apply(QuakePlayer player) {
        current.apply(player);
        current = getNext();
    }

    @NotNull
    protected PowerUp getNext() {
        return WbsCollectionUtil.getRandom(powerUps);
    }

    @Override
    public void removeFrom(QuakePlayer player) {
        for (PowerUp powerUp : powerUps) {
            powerUp.removeFrom(player);
        }
    }

    @Override
    protected boolean validate() {
        for (String id : powerUpNames) {
            PowerUp powerUp = PowerUp.getPowerUp(id);

            if (powerUp != null) {
                powerUps.add(powerUp);
            } else {
                settings.logError("Invalidate powerup: " + id, directory);
            }
        }

        current = getNext();

        return !powerUps.isEmpty();
    }
}
