package wbs.quake.killperks;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeLobby;
import wbs.quake.menus.MenuSelectable;
import wbs.quake.player.QuakePlayer;
import wbs.quake.powerups.PowerUp;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsMath;

import java.util.List;

public class KillPerk extends MenuSelectable {

    protected final PowerUp powerUp;
    protected final double chance;

    public KillPerk(String id, Material material, String display, String permission, List<String> description, double price, String powerUpId) {
        super(id, material, display, permission, description, price);

        chance = 100;
        powerUp = PowerUp.getPowerUp(powerUpId);
        if (powerUp == null) {
            throw new InvalidConfigurationException("Invalid power-up: " + powerUpId);
        }
    }

    public KillPerk(ConfigurationSection section, String directory) {
        super(section, directory);

        chance = section.getDouble("chance", 100);
        String powerUpId = section.getString("power-up");
        powerUp = PowerUp.getPowerUp(powerUpId);

        if (powerUp == null) {
            settings.logError("Invalid power-up: " + powerUpId, directory + "/power-up");
            throw new InvalidConfigurationException("Invalid power-up.");
        }
    }

    @Override
    public List<String> updateLore(List<String> lore) {
        lore.add("&6Chance: &h" + chance + "%");
        return lore;
    }

    public void apply(QuakePlayer player) {
        if (WbsMath.chance(chance)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    powerUp.removeFrom(player);
                }
            }.runTaskLater(plugin, powerUp.getDuration());

            powerUp.apply(player);

            plugin.sendActionBar("&b" + powerUp.getDisplay() + " &eKill Perk activated!", player.getPlayer());
        }
    }

    @Override
    public String getPermission() {
        return "wbsquake.killperks." + getId();
    }
}
