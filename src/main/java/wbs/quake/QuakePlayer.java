package wbs.quake;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class QuakePlayer {

    private final UUID uuid;
    private String name;

    private Player player;

    private int kills;
    private int deaths;
    private int wins;
    private int played;

    private final Gun currentGun;

    public QuakePlayer(ConfigurationSection section) {
        uuid = UUID.fromString(section.getName());

        name = section.getString(uuid + ".name");
        played = section.getInt(uuid + ".played");
        wins = section.getInt(uuid + ".wins");
        kills = section.getInt(uuid + ".kills");
        deaths = section.getInt(uuid + ".deaths");

        currentGun = new Gun(section, uuid + ".gun");
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

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public Player getPlayer() {
        return player;
    }

    public void addDeath() {
        deaths++;
    }

    public void addKill() {
        kills++;
    }

    public void addWin() {
        wins++;
    }

    public void addPlayed() {
        played++;
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

    public void writeToConfig(ConfigurationSection section) {
        section.set(uuid + ".name", name);
        section.set(uuid + ".played", played);
        section.set(uuid + ".wins", wins);
        section.set(uuid + ".kills", kills);
        section.set(uuid + ".deaths", deaths);

        currentGun.writeToConfig(section, uuid + ".gun");
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
