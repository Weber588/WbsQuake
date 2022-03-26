package wbs.quake.cosmetics;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.QuakePlayer;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.Arrays;
import java.util.List;

public class KillMessage extends SelectableCosmetic {
    public KillMessage(String id, String message, String headshot, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);

        format = message;
        headshotFormat = headshot;
    }

    private final String format;
    private final String headshotFormat;

    public KillMessage(ConfigurationSection section, String directory) {
        super(section, directory);

        format = section.getString("message");
        headshotFormat = section.getString("headshot");

        if (format == null) {
            throw new InvalidConfigurationException("Format missing.");
        }
        if (headshotFormat == null) {
            throw new InvalidConfigurationException("Headshot format missing.");
        }

        description.addAll(Arrays.asList(
                "&6Message: &b",
                "&7" + format(format, "%player_name%", "Steve"),
                "&6Headshot: &b",
                "&7" + format(headshotFormat, "%player_name%", "Steve")
        ));
    }

    private String format(String format, String attackerName, String victimName) {
        String tempFormat = format.replace("%attacker%", attackerName);
        tempFormat = tempFormat.replace("%victim%", victimName);
        return tempFormat;
    }

    public String format(QuakePlayer attacker, QuakePlayer victim) {
        return format(format, attacker.getName(), victim.getName());
    }

    public String formatHeadshot(QuakePlayer attacker, QuakePlayer victim) {
        return format(headshotFormat, attacker.getName(), victim.getName());
    }

    @Override
    public CosmeticType getCosmeticType() {
        return CosmeticType.KILL_MESSAGE;
    }

    @Override
    public void onSelect(QuakePlayer player, PlayerCosmetics cosmetics) {
        cosmetics.killMessage = this;
    }
}
