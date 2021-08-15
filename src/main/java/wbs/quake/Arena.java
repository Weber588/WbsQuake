package wbs.quake;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.player.QuakePlayer;
import wbs.quake.powerups.PowerUp;

import java.util.*;

public class Arena {

    private static WbsQuake plugin;
    public static void setPlugin(WbsQuake plugin) {
        Arena.plugin = plugin;
    }

    // Arena details
    private final List<Location> spawnPoints = new ArrayList<>();
    private final Map<Location, PowerUp> powerups = new HashMap<>();

    private final String name;
    private String description;
    private int maxPlayers;
    private int minPlayers;
    private int killsToWin = 10;
    private int secondsInRound = 60 * 5; // 5 minutes


    public Arena(String name) {
        this.name = name;
        minPlayers = 2;
        maxPlayers = 8;
    }

    public Arena(ConfigurationSection section) {
        name = section.getName();

        description = section.getString("description");
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

        for (QuakePlayer player : QuakeLobby.getInstance().getPlayers()) {
            if (!player.getPlayer().getWorld().equals(loc.getWorld())) continue;
            distance = Math.min(distance, player.getPlayer().getLocation().distanceSquared(loc));
        }

        return distance;
    }

    public QuakePlayer getClosestPlayer(QuakePlayer quakePlayer) {
        double distance = Double.MAX_VALUE;
        QuakePlayer closest = null;
        for (QuakePlayer player : QuakeLobby.getInstance().getPlayers()) {
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
        double distance = Double.MAX_VALUE;
        QuakePlayer closest = null;
        for (QuakePlayer player : QuakeLobby.getInstance().getPlayers()) {
            if (!player.getPlayer().getWorld().equals(location.getWorld())) continue;
            double thisDistance = player.getPlayer().getLocation().distanceSquared(location);

            if (thisDistance < distance) {
                distance = thisDistance;
                closest = player;
            }
        }

        return closest;
    }




    public boolean respawn(QuakePlayer player) {
        Location point = getRandomSpawnPoint(player);
        if (point == null) return false;
        player.teleport(point);
        return true;
    }


    private final Random random = new Random();
    private Location getRandomSpawnPoint(QuakePlayer player) {
        if (spawnPoints.size() == 0) return null;

        Location chosen = null;
        if (plugin.settings.findFurthestSpawnpoint) {
            if (QuakeLobby.getInstance().getPlayers().size() > 2) {
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

    private Location getNotClosestSpawnpoint(QuakePlayer player) {
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

    private Location getClosestSpawnpoint(QuakePlayer player) {
        double distanceToClosest = Double.MAX_VALUE;
        Location playerLoc = player.getPlayer().getLocation();
        Location closest = null;
        for (Location spawnPoint : spawnPoints) {
            if (!player.getPlayer().getWorld().equals(spawnPoint.getWorld())) continue;
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

    public void addSpawnPoint(Location loc) {
        spawnPoints.add(loc);
    }
    public boolean removeSpawnPoint(Location loc) {
        return spawnPoints.remove(loc);
    }

    public void addPowerUp(Location loc, PowerUp powerUp) {
        powerups.put(loc, powerUp);
    }
    public void removePowerUp(Location loc) {
        powerups.remove(loc);
    }

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

    public HashMap<Location, PowerUp> getPowerUps() {
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

    public void start() {
        for (Location loc : powerups.keySet()) {
            powerups.get(loc).spawnAt(loc);
        }
    }

    public void finish() {
        for (Location loc : powerups.keySet()) {
            powerups.get(loc).remove(loc);
        }
    }
}
