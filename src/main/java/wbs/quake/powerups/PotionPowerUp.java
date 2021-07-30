package wbs.quake.powerups;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import wbs.quake.*;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
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
            targetType = TargetType.PLAYER;
        } else {
            targetType = WbsEnums.getEnumFromString(TargetType.class, targetString);

            if (targetType == null) {
                targetType = TargetType.PLAYER;
                settings.logError("Invalid target: " + targetString, directory + "/target");
            }
        }
    }

    private final PotionEffect effect;
    private TargetType targetType;

    @Override
    public void runOn(QuakePlayer player) {
        List<QuakePlayer> playersInArena = QuakeLobby.getInstance().getPlayers();
        switch (targetType) {
            case PLAYER:
                player.getPlayer().addPotionEffect(effect);
                break;
            case NOT_PLAYER:
                for (QuakePlayer otherPlayer : playersInArena) {
                    if (!otherPlayer.equals(player)) {
                        otherPlayer.getPlayer().addPotionEffect(effect);
                    }
                }
                break;
            case ALL:
                for (QuakePlayer otherPlayer : playersInArena) {
                    otherPlayer.getPlayer().addPotionEffect(effect);
                }
                break;
            case RANDOM:
                playersInArena.get(new Random().nextInt(playersInArena.size()))
                        .getPlayer().addPotionEffect(effect);
                break;
            case RANDOM_NOT_PLAYER:
                playersInArena.remove(player);
                playersInArena.get(new Random().nextInt(playersInArena.size()))
                        .getPlayer().addPotionEffect(effect);
                break;
            case CLOSEST:
                double closestDistance = Double.MAX_VALUE;
                QuakePlayer closest = null;
                for (QuakePlayer otherPlayer : playersInArena) {
                    if (otherPlayer.equals(player)) continue;
                    double distanceSquared = otherPlayer.getPlayer().getLocation().distanceSquared(player.getPlayer().getLocation());
                    if (distanceSquared < closestDistance) {
                        closest = otherPlayer;
                        closestDistance = distanceSquared;
                    }
                }

                if (closest == null) {
                    return;
                }

                closest.getPlayer().addPotionEffect(effect);
                break;
        }
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

    private enum TargetType {
        PLAYER, NOT_PLAYER, ALL, RANDOM, RANDOM_NOT_PLAYER, CLOSEST
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
