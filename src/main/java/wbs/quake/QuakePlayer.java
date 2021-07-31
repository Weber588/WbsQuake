package wbs.quake;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import wbs.utils.util.WbsMath;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class QuakePlayer {

    private final UUID uuid;
    private String name;

    private Player player;

    private int headshots;
    private int kills;
    private int deaths;
    private int wins;
    private int played;

    private final Gun currentGun;

    public QuakePlayer(ConfigurationSection section) {
        uuid = UUID.fromString(section.getName());

        name = section.getString("name", "Unknown");
        played = section.getInt("played");
        wins = section.getInt("wins");
        kills = section.getInt("kills");
        deaths = section.getInt("deaths");

        currentGun = new Gun(section, uuid + ".gun");
    }

    public void writeToConfig(ConfigurationSection section) {
        if (name != null) {
            section.set(uuid + ".name", name);
        }
        section.set(uuid + ".played", played);
        section.set(uuid + ".wins", wins);
        section.set(uuid + ".kills", kills);
        section.set(uuid + ".deaths", deaths);

        currentGun.writeToConfig(section, uuid + ".gun");
    }

    public QuakePlayer(UUID uuid) {
        this.uuid = uuid;
        currentGun = new Gun();
        player = Bukkit.getPlayer(uuid);
        if (player != null) {
            name = player.getName();
        }
    }

    public Gun getCurrentGun() {
        return currentGun;
    }

    public void teleport(Location loc) {
        player.teleport(loc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuakePlayer that = (QuakePlayer) o;
        return uuid.equals(that.uuid);
    }

    public Player getPlayer() {
        if (player == null) {
            player = Bukkit.getPlayer(uuid);
            if (player != null) {
                name = player.getName();
            }
        }
        return player;
    }

    public int getHeadshots() {
        return headshots;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getWins() {
        return wins;
    }

    public int getPlayed() {
        return played;
    }

    public void addHeadshot() {
        headshots++;
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addWin() {
        wins++;
    }

    public void addPlayed() {
        played++;
    }

    public List<String> getStatsDisplay() {
        List<String> statsLines = new LinkedList<>();

        int kills = getKills();
        int deaths = getDeaths();
        int wins = getWins();
        int played = getPlayed();

        if (deaths > 0) {
            statsLines.add("K/D: &h" + WbsMath.roundTo(((double) kills) / deaths, 2));
        }
        statsLines.add("Kills: &h" + kills);
        statsLines.add("Headshots: &h" + getHeadshots());
        statsLines.add("Deaths: &h" + deaths);
        statsLines.add("Wins: &h" + wins);
        statsLines.add("Played: &h" + played);

        return statsLines;
    }

    public void playDeathEffect() {
        // TODO: Add death effects
    }

    public String getName() {
        if (name == null && player != null) {
            name = player.getName();
        }
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
