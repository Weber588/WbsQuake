package wbs.quake.powerups;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.quake.*;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.string.WbsStrings;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PotionPowerUp extends PowerUp {
    public PotionPowerUp(WbsQuake plugin, ConfigurationSection section, String directory) {
        super(plugin, section, directory);

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

    private final PotionEffect effect;
    private PlayerTargeter.TargetType targetType;

    @Override
    public void apply(QuakePlayer player) {
        for (QuakePlayer target : PlayerTargeter.getTargets(player, targetType)) {
            target.getPlayer().addPotionEffect(effect);
        }
    }

    @Override
    public void removeFrom(QuakePlayer player) {
        player.getPlayer().removePotionEffect(effect.getType());
    }

    @Override
    protected Material getDefaultItem() {
        return Material.POTION;
    }
    @Override
    protected String getDefaultDisplay() {
        return "Potion";
    }

    @Override
    public String getDisplay() {
        return WbsStrings.capitalizeAll(effect.getType().getName());
    }

    @Override
    protected ItemStack getItem() {
        ItemStack itemStack = new ItemStack(item);

        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item);
        if (meta == null) {
            return itemStack;
        }

        if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;

            potionMeta.setColor(effect.getType().getColor());
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public PotionEffect getEffect() {
        return effect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PotionPowerUp)) return false;
        if (!super.equals(o)) return false;
        PotionPowerUp that = (PotionPowerUp) o;
        return Objects.equals(effect, that.effect) && targetType == that.targetType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), effect, targetType);
    }
}
