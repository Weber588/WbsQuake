package wbs.quake;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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
import org.jetbrains.annotations.NotNull;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.trails.Trail;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.quake.upgrades.UpgradeableOption;
import wbs.utils.util.*;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class Gun {

    public static final NamespacedKey GUN_KEY = new NamespacedKey(WbsQuake.getInstance(), "isGun");

    @NotNull
    private UpgradeableOption cooldown; // 20
    @NotNull
    private UpgradeableOption leapSpeed; // 1.5
    @NotNull
    private UpgradeableOption leapCooldown; // 50
    @NotNull
    private UpgradeableOption speed;
    @NotNull
    private UpgradeableOption piercing;

    @NotNull
    private Trail trail;

    private double width = 0.2;

    private int bounces = 0;
    private double multishotChance = 0f;
    private double cooldownModifier = 1;

    private Material skin = Material.WOODEN_HOE;
    private boolean shiny;

    private LocalDateTime lastShot;
    private LocalDateTime lastLeap;
    private final WbsSound leapSound = new WbsSound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1, 2);

    public Gun() {
        QuakeSettings settings = WbsQuake.getInstance().settings;

        cooldown = settings.getOption("cooldown", 0);
        leapSpeed = settings.getOption("leap-speed", 0);
        leapCooldown = settings.getOption("leap-cooldown", 0);
        speed = settings.getOption("speed", 0);
        piercing = settings.getOption("piercing", 0);

        trail = CosmeticsStore.getInstance().getTrail("default");
    }

    public Gun(ConfigurationSection section, String path) {
        this();
        int cooldownProgress = section.getInt(path + ".cooldown", 0);
        int leapSpeedProgress = section.getInt(path + ".leap-speed", 0);
        int leapCooldownProgress = section.getInt(path + ".leap-cooldown", 0);
        int speedProgress = section.getInt(path + ".speed", 0);
        int piercingProgress = section.getInt(path + ".piercing", 0);

        QuakeSettings settings = WbsQuake.getInstance().settings;
        cooldown = settings.getOption("cooldown", cooldownProgress);
        leapSpeed = settings.getOption("leap-speed", leapSpeedProgress);
        leapCooldown = settings.getOption("leap-cooldown", leapCooldownProgress);
        speed = settings.getOption("speed", speedProgress);
        piercing = settings.getOption("piercing", piercingProgress);

        skin = WbsEnums.materialFromString(section.getString(path + ".skin"), Material.WOODEN_HOE);
        shiny = section.getBoolean(path + ".shiny", shiny);
    }

    public void writeToConfig(ConfigurationSection section, String path) {
        setIfNotZero(section, path + ".cooldown", cooldown);
        setIfNotZero(section, path + ".leap-speed", leapSpeed);
        setIfNotZero(section, path + ".leap-cooldown", leapCooldown);
        setIfNotZero(section, path + ".speed", speed);

        // Don't need to save skin, that's done in cosmetics
        section.set(path + ".shiny", shiny ? true : null);
    }

    private void setIfNotZero(ConfigurationSection section, String path, UpgradeableOption value) {
        section.set(path, value.getCurrentProgress() != 0 ? value.getCurrentProgress() : null);
    }

    public ItemStack buildGun() {
        ItemStack gunItem = new ItemStack(skin);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(skin);

        // If gun type is air
        if (meta == null) throw new IllegalArgumentException("Invalid skin: " + skin);

        meta.setDisplayName(WbsStrings.colourise(ItemManager.getQuakeGunName()));
        if (shiny) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        List<String> lore = new LinkedList<>();
        lore.add("&6Cooldown: &h" + cooldown.formattedValue());
        lore.add("&6Leap Speed: &h" + leapSpeed.formattedValue());
        lore.add("&6Leap Cooldown: &h" + leapCooldown.formattedValue());
        lore.add("&6Speed: &h" + speed.formattedValue());
        lore.add("&6Pierces: &h" + piercing.formattedValue());

        meta.setLore(WbsQuake.getInstance().colouriseAll(lore));

        meta.getPersistentDataContainer().set(GUN_KEY, PersistentDataType.STRING, "true");

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (speed.val() > 0) {
            AttributeModifier speedModifier =
                    new AttributeModifier(
                            Attribute.GENERIC_MOVEMENT_SPEED.name(),
                            speed.val() / 100,
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1
                    );
            meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, speedModifier);
        }

        gunItem.setItemMeta(meta);

        if (shiny) {
            gunItem.addUnsafeEnchantment(Enchantment.LOYALTY, 1);
        }

        return gunItem;
    }

    private boolean offCooldown() {
        if (lastShot == null) return true;
        return Math.abs(Duration.between(lastShot, LocalDateTime.now()).toMillis() / 1000.0) > getCooldown() / 20.0;
    }

    private int freeReloads = 0;

    public void instantReload(QuakePlayer player, int freeReloads) {
        lastShot = null;

        player.getPlayer().setExp(1);
        Bukkit.getScheduler().cancelTask(xpTimerId);

        this.freeReloads = freeReloads;
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

    private List<Material> bounceIgnored = Collections.singletonList(Material.BARRIER);

    /**
     * Fire the gun from the given player
     * @param player The player who fired
     */
    public boolean fire(QuakePlayer player) {
        if (QuakeLobby.getInstance().getState() != QuakeLobby.GameState.GAMEPLAY) return false;

        if (freeReloads <= 0) {
            if (!offCooldown()) return false;
        } else {
            freeReloads--;
        }

        lastShot = LocalDateTime.now();

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

        int playersKilled = 0;

        while (running) {
            result = world.rayTrace(shootLocation, facing, 300, FluidCollisionMode.NEVER, true, width, predicate);
            if (result != null) {
                fired = true;
                Location hitPos = result.getHitPosition().toLocation(world);
                trail.playShot(shootLocation, hitPos);
                shootLocation = hitPos;

                if (result.getHitBlock() != null) {
                    if (bouncesLeft > 0) {
                        if (bounceIgnored.contains(result.getHitBlock().getType())) {
                            running = false;
                        } else {
                            bouncesLeft--;

                            BlockFace blockFace = result.getHitBlockFace();
                            assert blockFace != null;
                            facing = WbsMath.reflectVector(facing, blockFace.getDirection());
                        }
                    } else {
                        running = false;
                    }
                }
                if (result.getHitEntity() != null) {
                    Player hitPlayer = (Player) result.getHitEntity();
                    ignorePlayers.add(hitPlayer);

                    QuakePlayer victim = PlayerManager.getPlayer(hitPlayer);

                    Location hitLoc = result.getHitPosition().toLocation(hitPlayer.getWorld());

                    double distanceToEyes = hitLoc.distance(hitPlayer.getEyeLocation());
                    boolean headshot = distanceToEyes < WbsQuake.getInstance().settings.headshotThreshold;

                    QuakeRound round = QuakeLobby.getInstance().getCurrentRound();
                    round.registerKill(victim, player, headshot);

                    playersKilled++;
                }
            } else {
                running = false;
            }

            if (playersKilled >= piercing.intVal()) {
                running = false;
            }
        }

        if (fired) {
            player.getCosmetics().shootSound.play(bukkitPlayer.getEyeLocation());
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
        return Math.abs(Duration.between(lastLeap, LocalDateTime.now()).toMillis() / 1000.0) > leapCooldown.intVal() / 20.0;
    }

    public void leap(QuakePlayer player) {
        if (!offLeapCooldown()) {
            Duration timeLeft = WbsTime.timeLeft(lastLeap, Duration.ofNanos((long) (leapCooldown.intVal() / 20.0 * 1000000000)));

            WbsQuake.getInstance().sendActionBar("&wYou can use that again in &h"
                    + WbsStringify.toString(timeLeft, false)
                    + "&r!", player.getPlayer());
            return;
        }
        lastLeap = LocalDateTime.now();

        WbsEntities.push(player.getPlayer(), leapSpeed.val());
        leapSound.play(player.getPlayer().getLocation());
    }

    public void setTrail(@NotNull Trail trail) {
        this.trail = trail;
    }

    public void setSkin(Material skin) {
        this.skin = skin;
    }
    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }
    public void setBounces(int bounces) {
        this.bounces = bounces;
    }
    public void setMultishotChance(double multishotChance) {
        this.multishotChance = multishotChance;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getCooldown() {
        return cooldown.intVal() / cooldownModifier;
    }
    public Material getSkin() {
        return skin;
    }
    public boolean getShiny() {
        return shiny;
    }
    public int getBounces() {
        return bounces;
    }
    public double getMultishotChance() {
        return multishotChance;
    }

    public int setCooldownProgress(int progress) {
        return cooldown.setCurrentProgress(progress);
    }
    public int setLeapSpeedProgress(int progress) {
        return leapSpeed.setCurrentProgress(progress);
    }
    public int setLeapCooldownProgress(int progress) {
        return leapCooldown.setCurrentProgress(progress);
    }
    public int setSpeedProgress(int i) {
        return speed.setCurrentProgress(i);
    }
    public int setPiercingProgress(int i) {
        return piercing.setCurrentProgress(i);
    }

    public UpgradeableOption getCooldownOption() {
        return cooldown;
    }
    public UpgradeableOption getLeapCooldownOption() {
        return leapCooldown;
    }
    public UpgradeableOption getLeapSpeedOption() {
        return leapSpeed;
    }
    public UpgradeableOption getSpeedOption() {
        return speed;
    }
    public UpgradeableOption getPiercingOption() {
        return piercing;
    }
}
