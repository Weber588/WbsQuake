package wbs.quake;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.quake.player.QuakePlayer;
import wbs.quake.powerups.ArenaPowerUp;
import wbs.quake.powerups.PowerUp;

import java.util.*;

public class Arena {

    private static WbsQuake plugin;

    public static void setPlugin(WbsQuake plugin) {
        Arena.plugin = plugin;
    }

    // Arena details
    private final List<Location> spawnPoints = new ArrayList<>();
    private final Map<Location, ArenaPowerUp> powerups = new HashMap<>();

    @NotNull
    private final String name;
    @NotNull
    private String displayName;
    private String description;
    private int maxPlayers;
    private int minPlayers;
    private int killsToWin = 10;
    private int secondsInRound = 60 * 5; // 5 minutes

    private boolean needsSaving = false;
    @Nullable
    private QuakeRound currentRound;

    public Arena(@NotNull String name) {
        this.name = name;
        displayName = name;
        minPlayers = 2;
        maxPlayers = 8;
    }

    public Arena(ConfigurationSection section) {
        name = section.getName();

        description = section.getString("description");
        displayName = section.getString("display-name", name);
        minPlayers = section.getInt("min");
        maxPlayers = section.getInt("max");
        killsToWin = section.getInt("kills-to-win");
        secondsInRound = section.getInt("round-duration", 5 * 60);

        int failed = 0;
        ConfigurationSection spawnPointsSec = section.getConfigurationSection("spawn-points");
        if (spawnPointsSec != null)  {
            for (String key : spawnPointsSec.getKeys(false)) {
                String pointString = spawnPointsSec.getString(key);

                if (pointString == null) {
                    failed++;
                    continue;
                }

                Location spawnLoc = fromString(pointString);

                addSpawnPoint(spawnLoc);
            }
            if (failed > 0) plugin.logger.warning(failed + " failed to load.");
        }

        ConfigurationSection powerUpSec = section.getConfigurationSection("power-ups");
        if (powerUpSec != null) {

            failed = 0;
            for (String key : powerUpSec.getKeys(false)) {
                ConfigurationSection thisSec = powerUpSec.getConfigurationSection(key);
                assert thisSec != null;

                String pointString = thisSec.getString("location");

                if (pointString == null) {
                    failed++;
                    continue;
                }

                Location spawnLoc = fromString(pointString);

                if (spawnLoc == null) continue;

                String powerUpType = thisSec.getString("type");

                PowerUp powerUp = plugin.settings.powerUps.get(powerUpType);

                if (powerUp == null) {
                    plugin.logger.warning("Invalid power up: " + powerUpType);
                    continue;
                }
                addPowerUp(spawnLoc, powerUp);
            }
        }
    }



    private Location fromString(String pointString) {
        String[] components = pointString.split(", ");
        if (components.length != 6) {
            plugin.logger.warning("Invalid arg count: " + pointString);
            return null;
        }

        double x, y, z;
        float pitch, yaw;
        World world;

        try {
            x = Double.parseDouble(components[0]);
            y = Double.parseDouble(components[1]);
            z = Double.parseDouble(components[2]);
            pitch = Float.parseFloat(components[3]);
            yaw = Float.parseFloat(components[4]);
        } catch (NumberFormatException e) {
            plugin.logger.warning("Invalid coordinates: "
                    + components[0] + ", "
                    + components[1] + ", "
                    + components[2] + ", "
                    + components[3] + ", "
                    + components[4]);
            return null;
        }

        world = Bukkit.getWorld(components[5]);
        if (world == null) {
            plugin.logger.warning("Invalid world: " + components[5]);
            return null;
        }

        Location loc = new Location(world, x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);

        return loc;
    }

    public void writeToConfig(ConfigurationSection section) {
        section.set(name + ".description", description);
        section.set(name + ".display-name", displayName);
        section.set(name + ".min", minPlayers);
        section.set(name + ".max", maxPlayers);
        section.set(name + ".kills-to-win", killsToWin);
        section.set(name + ".round-duration", secondsInRound);

        section.set(name + ".spawn-points", null);
        section.set(name + ".power-ups", null);

        int i = 0;
        for (Location location : spawnPoints) {
            section.set(name + ".spawn-points." + i,
                    location.getX() + ", "
                            + location.getY() + ", "
                            + location.getZ() + ", "
                            + location.getPitch() + ", "
                            + location.getYaw() + ", "
                            + Objects.requireNonNull(location.getWorld()).getName());
            i++;
        }

        section.set(name + ".power-ups", null);

        i = 0;
        for (Location location : powerups.keySet()) {
            section.set(name + ".power-ups." + i + ".location",
                    location.getX() + ", "
                            + location.getY() + ", "
                            + location.getZ() + ", "
                            + "0, " // Don't need pitch and yaw
                            + "0, "
                            + Objects.requireNonNull(location.getWorld()).getName());
            section.set(name + ".power-ups." + i + ".type", powerups.get(location).getId());
            i++;
        }
    }

    private double getDistanceToClosestPlayer(Location loc) {
        double distance = Double.MAX_VALUE;

        if (currentRound == null) {
            return distance;
        }

        for (QuakePlayer player : currentRound.getActivePlayers()) {
            if (!player.getPlayer().getWorld().equals(loc.getWorld())) continue;
            distance = Math.min(distance, player.getPlayer().getLocation().distanceSquared(loc));
        }

        return distance;
    }

    public QuakePlayer getClosestPlayer(QuakePlayer quakePlayer) {
        if (currentRound == null) {
            return null;
        }

        double distance = Double.MAX_VALUE;
        QuakePlayer closest = null;
        for (QuakePlayer player : currentRound.getActivePlayers()) {
            if (player == quakePlayer) continue;
            if (!player.getPlayer().getWorld().equals(quakePlayer.getPlayer().getWorld())) continue;

            double thisDistance = player.getPlayer().getLocation().distanceSquared(quakePlayer.getPlayer().getLocation());

            if (thisDistance < distance) {
                distance = thisDistance;
                closest = player;
            }
        }

        return closest;
    }

    private QuakePlayer getClosestPlayer(Location location) {
        if (currentRound == null) {
            return null;
        }

        double distance = Double.MAX_VALUE;
        QuakePlayer closest = null;
        for (QuakePlayer player : currentRound.getActivePlayers()) {
            if (!player.getPlayer().getWorld().equals(location.getWorld())) continue;
            double thisDistance = player.getPlayer().getLocation().distanceSquared(location);

            if (thisDistance < distance) {
                distance = thisDistance;
                closest = player;
            }
        }

        return closest;
    }

    public boolean respawn(Player player) {
        Location point = getRandomSpawnPoint(player);
        if (point == null) return false;
        player.teleport(point);
        return true;
    }

    public boolean respawn(QuakePlayer player) {
        return respawn(player.getPlayer());
    }

    private final Random random = new Random();
    private Location getRandomSpawnPoint(Player player) {
        if (spawnPoints.size() == 0) return null;

        Location chosen = null;
        if (plugin.settings.findFurthestSpawnpoint && currentRound != null) {
            if (currentRound.getActivePlayers().size() > 2) {
                chosen = getFurthestSpawnpoint();
            } else {
                chosen = getNotClosestSpawnpoint(player);
            }
        }

        if (chosen == null) {
            chosen = getRandomSpawnPoint();
        }

        return chosen;
    }

    private Location getRandomSpawnPoint() {
        return spawnPoints.get(random.nextInt(spawnPoints.size()));
    }

    private Location getNotClosestSpawnpoint(Player player) {
        Location closestLocation = getClosestSpawnpoint(player);
        if (spawnPoints.size() == 1) {
            return spawnPoints.get(0);
        }

        Location foundLocation;
        do {
            foundLocation = getRandomSpawnPoint();
        } while (foundLocation.equals(closestLocation));

        return foundLocation;
    }

    private Location getClosestSpawnpoint(Player player) {
        double distanceToClosest = Double.MAX_VALUE;
        Location playerLoc = player.getLocation();
        Location closest = null;
        for (Location spawnPoint : spawnPoints) {
            if (!player.getWorld().equals(spawnPoint.getWorld())) continue;
            double distance = playerLoc.distance(spawnPoint);
            if (distance < distanceToClosest) {
                distanceToClosest = distance;
                closest = spawnPoint;
            }
        }

        return closest;
    }

    /**
     * @return The spawnpoint that has the greatest distance
     * to the closest player
     */
    private Location getFurthestSpawnpoint() {
        double distance = Double.MIN_VALUE;
        Location mostRemote = null;
        for (Location spawnPoint : spawnPoints) {
            double distanceToClosest = getDistanceToClosestPlayer(spawnPoint);
            if (distanceToClosest > distance) {
                distance = distanceToClosest;
                mostRemote = spawnPoint;
            }
        }

        return mostRemote;
    }

    public void distributePlayers() {
        if (currentRound == null) {
            throw new IllegalStateException("Current round must be defined when distributing players.");
        }

        List<Location> unusedSpawnpoints = new LinkedList<>(spawnPoints);
        Collections.shuffle(unusedSpawnpoints);

        List<QuakePlayer> playersToDistribute = currentRound.getActivePlayers();
        while (!playersToDistribute.isEmpty()) {
            QuakePlayer player = playersToDistribute.get(0);
            Location spawnPoint = unusedSpawnpoints.get(0);

            player.teleport(spawnPoint);

            unusedSpawnpoints.remove(0);
            playersToDistribute.remove(0);

            if (unusedSpawnpoints.isEmpty()) {
                unusedSpawnpoints.addAll(spawnPoints);
                Collections.shuffle(unusedSpawnpoints);
            }
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }
    public int getMinPlayers() {
        return minPlayers;
    }

    public void setKillsToWin(int killsToWin) {
        this.killsToWin = killsToWin;
    }
    public int getKillsToWin() {
        return killsToWin;
    }

    public int getKillsToWin(int playersInRound) {
        QuakeSettings settings = plugin.settings;

        int killsToWin = getKillsToWin();

        if (!settings.doKillScaling) {
            return killsToWin;
        }

        int extraPlayers = (playersInRound + settings.killScalingPlayerIncrement) - settings.minPlayersForExtraKills;
        int incrementsToAdd = Math.max(0, extraPlayers / settings.killScalingPlayerIncrement);

        return killsToWin + incrementsToAdd * settings.killScalingPointIncrement;
    }

    public void addSpawnPoint(Location loc) {
        spawnPoints.add(loc);
    }
    public void removeSpawnPoint(Location loc) {
        spawnPoints.remove(loc);
    }

    public void addPowerUp(Location loc, PowerUp powerUp) {
        powerups.put(loc, new ArenaPowerUp(loc, powerUp));
    }
    public void removePowerUp(Location loc) {
        powerups.remove(loc);
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }
    @NotNull
    public String getName() {
        return name;
    }

    public int getSecondsInRound() {
        return secondsInRound;
    }
    public void setSecondsInRound(int secondsInRound) {
        this.secondsInRound = secondsInRound;
    }

    public boolean removeSpawnPoint(int hash) {
        for (Location loc : spawnPoints) {
            if (loc.hashCode() == hash) {
                spawnPoints.remove(loc);
                return true;
            }
        }
        return false;
    }

    public List<Location> getSpawnpoints() {
        return new LinkedList<>(spawnPoints);
    }

    public Location getSpawnPoint(int hash) {
        for (Location loc : spawnPoints) {
            if (loc.hashCode() == hash) {
                return loc;
            }
        }
        return null;
    }

    // Powerups

    public HashMap<Location, ArenaPowerUp> getPowerUps() {
        return new HashMap<>(powerups);
    }

    /**
     * Remove a power up, given the hash of the location
     * @param hash The hash of the location
     * @return Whether or not a location was found.
     */
    public boolean removePowerUp(int hash) {
        for (Location loc : powerups.keySet()) {
            if (loc.hashCode() == hash) {
                removePowerUp(loc);
                return true;
            }
        }
        return false;
    }


    public Location getPowerUpLocation(int hash) {
        for (Location loc : powerups.keySet()) {
            if (loc.hashCode() == hash) {
                return loc;
            }
        }
        return null;
    }

    public void start(QuakeRound currentRound) {
        this.currentRound = currentRound;
        for (ArenaPowerUp powerUp : powerups.values()) {
            powerUp.spawn();
        }

        distributePlayers();
    }

    public void finish() {
        for (ArenaPowerUp powerUp : powerups.values()) {
            powerUp.removeAndCancel();
        }
    }

    public void setDisplayName(@NotNull String display) {
        this.displayName = display;
    }

    public void move(Location from, Location to) {
        int diffX = from.getBlockX() - to.getBlockX();
        int diffY = from.getBlockY() - to.getBlockY();
        int diffZ = from.getBlockZ() - to.getBlockZ();
        Vector diff = new Vector(diffX, diffY, diffZ);

        for (Location spawnpoint : spawnPoints) {
            spawnpoint.subtract(diff);
            spawnpoint.setWorld(to.getWorld());
        }

        Map<Location, ArenaPowerUp> newPowerUps = new HashMap<>();

        for (Location powerUpLocation : powerups.keySet()) {
            Location newLoc = powerUpLocation.clone();
            newLoc.subtract(diff);
            newLoc.setWorld(to.getWorld());

            newPowerUps.put(newLoc, powerups.get(powerUpLocation));
        }

        powerups.clear();
        powerups.putAll(newPowerUps);
    }

    public boolean needsSaving() {
        return needsSaving;
    }

    public void unmarkForSaving() {
        this.needsSaving = false;
    }

    public void markForSaving() {
        this.needsSaving = true;
    }
}
