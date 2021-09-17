package wbs.quake.powerups;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import wbs.quake.player.QuakePlayer;
import wbs.quake.WbsQuake;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.configuration.WbsConfigReader;

public class GiveItemPowerUp extends PowerUp {
    public GiveItemPowerUp(WbsQuake plugin, ConfigurationSection section, String directory) {
        super(plugin, section, directory);

        WbsConfigReader.requireNotNull(section, "item", settings, directory);

        item = section.getItemStack("item");

        if (item == null) {
            settings.logError("Invalid item.", directory + "/item");
            throw new InvalidConfigurationException();
        }
    }

    private final ItemStack item;

    @Override
    protected Material getDefaultItem() {
        return Material.STICK;
    }

    @Override
    protected String getDefaultDisplay() {
        return "";
    }

    @Override
    public void apply(QuakePlayer player) {

    }

    @Override
    public void removeFrom(QuakePlayer player) {

    }
}
