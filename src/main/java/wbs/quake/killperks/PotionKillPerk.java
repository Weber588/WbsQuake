package wbs.quake.killperks;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.quake.PlayerTargeter;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.List;

public class PotionKillPerk extends KillPerk {
    public PotionKillPerk(String id, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);

        effect = new PotionEffect(PotionEffectType.SPEED, 200, 0, false, false, true);
    }

    public PotionKillPerk(ConfigurationSection section, String directory) {
        super(section, directory);

        WbsConfigReader.requireNotNull(section, "potion", settings, directory);

        String potionTypeString = section.getString("potion");
        assert potionTypeString != null;

        PotionEffectType type = PotionEffectType.getByName(potionTypeString);
        if (type == null) {
            settings.logError("Invalid potion type: " + potionTypeString + ". Defaulting to Speed.", directory + "/potion");
            type = PotionEffectType.SPEED;
        }

        int amplifier = section.getInt("amplifier", 1) - 1;
        if (amplifier < 0 || amplifier >= 256) {
            settings.logError("Invalid amplifier: " + (amplifier + 1) + " (must be between 1 and 256). Defaulting to 1.", directory + "/amplifier");
            amplifier = 0;
        }

        int duration = section.getInt("duration", 200);
        if (duration <- 0) {
            settings.logError("Invalid duration; must be greater than 0.", directory + "/duration");
            duration = 200;
        }

        effect = new PotionEffect(type, duration, amplifier, false, false, true);

        String targetString = section.getString("target");
        if (targetString == null) {
            targetType = PlayerTargeter.TargetType.PLAYER;
        } else {
            targetType = WbsEnums.getEnumFromString(PlayerTargeter.TargetType.class, targetString);

            if (targetType == null) {
                targetType = PlayerTargeter.TargetType.PLAYER;
                settings.logError("Invalid target: " + targetString, directory + "/target");
            }
        }
    }

    private PlayerTargeter.TargetType targetType = PlayerTargeter.TargetType.PLAYER;
    protected final PotionEffect effect;

    @Override
    protected void internalApply(QuakePlayer player, QuakePlayer victim) {
        for (QuakePlayer target : PlayerTargeter.getTargets(player, targetType)) {
            target.getPlayer().addPotionEffect(effect);
        }
    }
}
