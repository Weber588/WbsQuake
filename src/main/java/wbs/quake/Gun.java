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
import wbs.quake.player.QuakePlayer;
import wbs.quake.upgrades.UpgradeableOption;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsTime;
import wbs.utils.util.database.WbsRecord;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class Gun {

    public static final NamespacedKey GUN_KEY = new NamespacedKey(WbsQuake.getInstance(), "isGun");
    public static final double MULTISHOT_ANGLE = 15;
    public static final double SCATTERSHOT_ANGLE = 10;

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
    private int scattershot = 0;
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

    public Gun(WbsRecord record) {
        this();

        int cooldownProgress =
                record.getOrDefault(QuakeDB.gunCooldownField, Integer.class);
        int leapSpeedProgress =
                record.getOrDefault(QuakeDB.leapSpeedField, Integer.class);
        int leapCooldownProgress =
                record.getOrDefault(QuakeDB.leapCooldownField, Integer.class);
        int speedProgress =
                record.getOrDefault(QuakeDB.speedField, Integer.class);
        int piercingProgress =
                record.getOrDefault(QuakeDB.piercingField, Integer.class);

        QuakeSettings settings = WbsQuake.getInstance().settings;
        cooldown = settings.getOption("cooldown", cooldownProgress);
        leapSpeed = settings.getOption("leap-speed", leapSpeedProgress);
        leapCooldown = settings.getOption("leap-cooldown", leapCooldownProgress);
        speed = settings.getOption("speed", speedProgress);
        piercing = settings.getOption("piercing", piercingProgress);
    }

    public void toRecord(WbsRecord record) {
        record.setField(QuakeDB.gunCooldownField, cooldown.getCurrentProgress());
        record.setField(QuakeDB.leapSpeedField, leapSpeed.getCurrentProgress());
        record.setField(QuakeDB.leapCooldownField, leapCooldown.getCurrentProgress());
        record.setField(QuakeDB.speedField, speed.getCurrentProgress());
        record.setField(QuakeDB.piercingField, piercing.getCurrentProgress());
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

    private final Predicate<Entity> BASE_PREDICATE = entity -> {
        if (!(entity instanceof Player)) {
            return false;
        }
        GameMode mode = ((Player) entity).getGameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
            return false;
        }

        return QuakeLobby.getInstance().isInRound((Player) entity);
    };

    /**
     * Fire the gun from the given player
     * @param player The player who fired
     */
    public boolean fire(QuakePlayer player) {
        if (QuakeLobby.getInstance().getState() != QuakeLobby.GameState.GAMEPLAY) return false;

        boolean freeReload = false;
        if (freeReloads <= 0) {
            if (!offCooldown()) return false;
        } else {
            freeReloads--;
            freeReload = true;
        }

        lastShot = LocalDateTime.now();

        Player bukkitPlayer = player.getPlayer();
        Vector facing = WbsEntityUtil.getFacingVector(bukkitPlayer);

        List<Entity> ignorePlayers = new LinkedList<>();
        ignorePlayers.add(bukkitPlayer);

        boolean fired = false;
        if (scattershot <= 0) {
            fired = fireFrom(player, WbsEntityUtil.getFacingVector(bukkitPlayer), ignorePlayers);

            if (WbsMath.chance(multishotChance)) {
                Vector localUp = WbsEntityUtil.getLocalUp(bukkitPlayer);

                Vector right = WbsMath.rotateVector(facing, localUp, MULTISHOT_ANGLE);
                fired |= fireFrom(player, right, ignorePlayers);

                Vector left = WbsMath.rotateVector(facing, localUp, -MULTISHOT_ANGLE);
                fired |= fireFrom(player, left, ignorePlayers);
            }
        } else {
            Vector direction = WbsEntityUtil.getFacingVector(bukkitPlayer);
            for (int i = 0; i < scattershot; i++) {
                Vector randomDir = WbsMath.randomVector().crossProduct(direction);

                fired |= fireFrom(player, WbsMath.rotateVector(direction, randomDir, Math.random() * SCATTERSHOT_ANGLE), ignorePlayers);
            }
        }

        if (fired) {
            if (!freeReload) {
                startXPTimer(player);
            }
            player.getCosmetics().shootSound.play(bukkitPlayer.getEyeLocation());
        }

        return fired;
    }

    private boolean fireFrom(QuakePlayer shooter, Vector direction, List<Entity> ignore) {
        return fireFrom(shooter,
                shooter.getPlayer().getEyeLocation(),
                direction,
                ignore,
                bounces,
                false);
    }

    /**
     * Perform the actual shot, handling bouncing, kills, and trails, but not sound.
     * @param shooter The player shooting
     * @param shootLocation The position from which to start the shot
     * @param direction The direction in which the shot should be performed
     * @param ignore The list of entities (players) already hit
     * @param bouncesLeft How many times this shot can bounce
     * @param isBounce Whether or not this shot was a reflected shot, alternating between true and false as the shot bounces
     *                 (Mostly for use in trail displays)
     * @return True if a shot was fired, false if there was nothing to hit
     */
    private boolean fireFrom(QuakePlayer shooter, Location shootLocation, Vector direction, List<Entity> ignore, int bouncesLeft, boolean isBounce) {
        Predicate<Entity> predicate = BASE_PREDICATE.and(entity -> !ignore.contains(entity));

        RayTraceResult result = shooter.getPlayer().getWorld().rayTrace(shootLocation, direction, 300, FluidCollisionMode.NEVER, true, width, predicate);
        if (result == null) {
            return false;
        }

        Location hitPos = result.getHitPosition().toLocation(shooter.getPlayer().getWorld());
        trail.playShot(shootLocation, hitPos, isBounce, shooter);

        if (result.getHitBlock() != null) {
            if (bouncesLeft > 0) {
                if (bounceIgnored.contains(result.getHitBlock().getType())) {
                    return true;
                }

                BlockFace blockFace = result.getHitBlockFace();
                assert blockFace != null;
                direction = WbsMath.reflectVector(direction, blockFace.getDirection());

                fireFrom(shooter, hitPos, direction, ignore, bouncesLeft - 1, !isBounce);
            }

            return true;
        }

        if (result.getHitEntity() != null) {
            Player hitPlayer = (Player) result.getHitEntity();
            ignore.add(hitPlayer);

            QuakePlayer victim = Objects.requireNonNull(QuakeLobby.getInstance().getPlayer(hitPlayer));

            Location hitLoc = result.getHitPosition().toLocation(hitPlayer.getWorld());

            double distanceToEyes = hitLoc.distance(hitPlayer.getEyeLocation());
            boolean headshot = distanceToEyes < WbsQuake.getInstance().settings.headshotThreshold;

            QuakeRound round = QuakeLobby.getInstance().getCurrentRound();
            round.registerKill(victim, shooter, headshot);
        }

        // - 1 to ignore the shooter themselves
        if ((ignore.size() - 1) >= piercing.intVal()) {
            return true;
        }

        fireFrom(shooter, hitPos, direction, ignore, bouncesLeft, isBounce);
        return true;
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

        WbsEntityUtil.push(player.getPlayer(), leapSpeed.val());
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
    public void setScattershot(int scattershot) {
        this.scattershot = scattershot;
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
    public int getScattershot() {
        return scattershot;
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
