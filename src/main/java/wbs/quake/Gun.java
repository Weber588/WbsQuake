package wbs.quake;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
import wbs.utils.util.WbsTime;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class Gun {

    public static final NamespacedKey GUN_KEY = new NamespacedKey(WbsQuake.getInstance(), "isGun");

    // TODO: Make this configurable?
    public String gunName = "&9&lRailgun";

    private int cooldown = 20;
    private double width = 0.2;
    private LocalDateTime lastUsed;

    private Material skin = Material.WOODEN_HOE;
    private boolean shiny;

    private double leapSpeed = 1.5;
    private int leapCooldown = 50;
    private LocalDateTime lastLeap;

    private int bounces = 0;
    private double multishotChance = 0f;
    private double cooldownModifier = 1;

    private final LineParticleEffect line;

    public Gun() {
        line = new LineParticleEffect();
        line.setRadius(0);
        Particle.DustOptions options = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.8f);

        line.setOptions(options);
        line.setScaleAmount(true);
        line.setAmount(3);
    }

    public Gun(ConfigurationSection section, String path) {
        this();
        cooldown = section.getInt(path + ".cooldown");
        skin = WbsEnums.materialFromString(section.getString(path + ".skin"), Material.WOODEN_HOE);
        shiny = section.getBoolean(path + ".shiny");
        leapSpeed = section.getDouble(path + ".leap-speed");
        leapCooldown = section.getInt(path + ".leap-cooldown");
        bounces = section.getInt(path + ".bounces");
        multishotChance = section.getDouble(path + ".multishot-chance");
    }

    public void writeToConfig(ConfigurationSection section, String path) {
        section.set(path + ".cooldown", cooldown);
        section.set(path + ".skin", skin.toString());
        section.set(path + ".shiny", shiny);
        section.set(path + ".leap-speed", leapSpeed);
        section.set(path + ".leap-cooldown", leapCooldown);
        section.set(path + ".bounces", bounces);
        section.set(path + ".multishot-chance", multishotChance);
    }


    public ItemStack buildGun() {
        ItemStack gunItem = new ItemStack(skin);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(skin);

        // If gun type is air
        if (meta == null) throw new IllegalArgumentException("Invalid skin: " + skin);

        meta.setDisplayName(WbsStrings.colourise(gunName));
        if (shiny) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.getPersistentDataContainer().set(GUN_KEY, PersistentDataType.STRING, "true");

        gunItem.setItemMeta(meta);

        if (shiny) {
            gunItem.addUnsafeEnchantment(Enchantment.LOYALTY, 1);
        }

        return gunItem;
    }

    private boolean offCooldown() {
        if (lastUsed == null) return true;
        return Math.abs(Duration.between(lastUsed, LocalDateTime.now()).toMillis() / 1000.0) > getCooldown() / 20.0;
    }

    private double getCooldown() {
        return cooldown / cooldownModifier;
    }

    public void addCooldownModifier(double modifier) {
        this.cooldownModifier *= modifier;
    }
    public void removeCooldownModifier(double modifier) {
        this.cooldownModifier /= modifier;
    }
    public void clearCooldownModifier() {
        cooldownModifier = 1;
    }

    /**
     * Fire the gun from the given player
     * @param player The player who fired
     */
    public boolean fire(QuakePlayer player) {
        if (!offCooldown()) return false;
        lastUsed = LocalDateTime.now();

        startXPTimer(player);

        Player bukkitPlayer = player.getPlayer();
        Vector facing = WbsEntities.getFacingVector(bukkitPlayer);
        World world = bukkitPlayer.getWorld();

        RayTraceResult result;
        Location shootLocation = bukkitPlayer.getEyeLocation();
        List<Player> ignorePlayers = new LinkedList<>();
        ignorePlayers.add(bukkitPlayer);

        int bouncesLeft = bounces;

        if (WbsMath.chance(multishotChance)) {
            // TODO: Set up multishot
        }

        Predicate<Entity> predicate =
                e -> {
                    if (!(e instanceof Player)) {
                        return false;
                    }
                    GameMode mode = ((Player) e).getGameMode();
                    if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
                        return false;
                    }

                    if (!QuakeLobby.getInstance().getPlayers().contains(PlayerManager.getPlayer((Player) e))) {
                        return false;
                    }

                    return !ignorePlayers.contains(e);
                };

        boolean running = true;
        boolean fired = false;
        while (running) {
            result = world.rayTrace(shootLocation, facing, 300, FluidCollisionMode.NEVER, true, width, predicate);
            if (result != null) {
                fired = true;
                Location hitPos = result.getHitPosition().toLocation(world);
                line.play(Particle.REDSTONE, shootLocation, hitPos);
                shootLocation = hitPos;

                if (result.getHitBlock() != null) {
                    if (bouncesLeft > 0) {
                        bouncesLeft--;

                        BlockFace blockFace = result.getHitBlockFace();
                        assert blockFace != null;
                        facing = WbsMath.reflectVector(facing, blockFace.getDirection());
                    } else {
                        running = false;
                    }
                }
                if (result.getHitEntity() != null) {
                    Player hitPlayer = (Player) result.getHitEntity();
                    ignorePlayers.add(hitPlayer);

                    QuakePlayer victim = PlayerManager.getPlayer(hitPlayer);

                    QuakeRound round = QuakeLobby.getInstance().getCurrentRound();
                    round.registerKill(victim, player);
                }
            } else {
                running = false;
            }
        }

        return fired;
    }

    private int xpTimerId = -1;
    private void startXPTimer(QuakePlayer player) {
        if (xpTimerId != -1) Bukkit.getScheduler().cancelTask(xpTimerId);

        xpTimerId = new BukkitRunnable() {
            float progress = 0;
            final float interval = (float)(1.0f / getCooldown());
            @Override
            public void run() {
                if (progress >= 1) {
                    progress = 1;
                    cancel();
                    xpTimerId = -1;
                }

                player.getPlayer().setExp(progress);
                progress += interval;
            }
        }.runTaskTimer(WbsQuake.getInstance(), 0, 1).getTaskId();
    }

    private boolean offLeapCooldown() {
        if (lastLeap == null) return true;
        return Math.abs(Duration.between(lastLeap, LocalDateTime.now()).toMillis() / 1000.0) > leapCooldown / 20.0;
    }

    public void leap(QuakePlayer player) {
        if (!offLeapCooldown()) {
            Duration timeLeft = WbsTime.timeLeft(lastLeap, Duration.ofNanos((long) (leapCooldown / 20.0 * 1000000000)));

            WbsQuake.getInstance().sendActionBar("&wYou can use that again in &h"
                    + WbsStringify.toString(timeLeft, false)
                    + "&r!", player.getPlayer());
            return;
        }
        lastLeap = LocalDateTime.now();

        WbsEntities.push(player.getPlayer(), leapSpeed);
    }

    public Material getSkin() {
        return skin;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setBounces(int bounces) {
        this.bounces = bounces;
    }

    public void setSkin(Material skin) {
        this.skin = skin;
    }

    public void setMultishotChance(double multishotChance) {
        this.multishotChance = multishotChance;
    }

    public double getLeapSpeed() {
        return leapSpeed;
    }

    public void setLeapSpeed(double leapSpeed) {
        this.leapSpeed = leapSpeed;
    }

    public int getLeapCooldown() {
        return leapCooldown;
    }

    public void setLeapCooldown(int leapCooldown) {
        this.leapCooldown = leapCooldown;
    }

    public int getBounces() {
        return bounces;
    }

    public double getMultishotChance() {
        return multishotChance;
    }
}
