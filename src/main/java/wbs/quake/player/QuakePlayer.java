package wbs.quake.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import wbs.quake.Gun;
import wbs.quake.QuakeDB;
import wbs.quake.QuakeSettings;
import wbs.quake.WbsQuake;
import wbs.quake.killperks.KillPerk;
import wbs.quake.menus.SelectableSlot;
import wbs.utils.util.WbsMath;
import wbs.utils.util.database.WbsRecord;

import java.util.*;

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

    private final PlayerCosmetics cosmetics;

    public QuakePlayer(ConfigurationSection section) {
        uuid = UUID.fromString(section.getName());

        name = section.getString("name", "Unknown");
        played = section.getInt("played");
        wins = section.getInt("wins");
        kills = section.getInt("kills");
        deaths = section.getInt("deaths");

        killPerk = WbsQuake.getInstance().settings.getKillPerk(section.getString("kill-perk"));

        currentGun = new Gun(section, "gun");

        ConfigurationSection cosmeticsSection = section.getConfigurationSection("cosmetics");
        if (cosmeticsSection != null) {
            cosmetics = new PlayerCosmetics(this, cosmeticsSection);
        } else {
            cosmetics = new PlayerCosmetics(this);
        }
    }

    public QuakePlayer(UUID uuid) {
        this.uuid = uuid;
        currentGun = new Gun();

        tryGetPlayer();

        cosmetics = new PlayerCosmetics(this);
    }

    public QuakePlayer(WbsRecord record) {
        uuid = UUID.fromString(record.getValue(QuakeDB.uuidField, String.class));

        tryGetPlayer();

        if (name == null) {
            name = record.getValue(QuakeDB.nameField, String.class);
        }

        kills = record.getOrDefault(QuakeDB.killsField, Integer.class);
        headshots = record.getOrDefault(QuakeDB.headshotsField, Integer.class);
        wins = record.getOrDefault(QuakeDB.winsField, Integer.class);
        played = record.getOrDefault(QuakeDB.playedField, Integer.class);
        deaths = record.getOrDefault(QuakeDB.deathsField, Integer.class);

        currentGun = new Gun(record);
        cosmetics = new PlayerCosmetics(this, record);
    }

    public void tryGetPlayer() {
        player = Bukkit.getPlayer(uuid);
        if (player != null) {
            name = player.getName();
        }
    }

    public Gun getCurrentGun() {
        return currentGun;
    }

    public PlayerCosmetics getCosmetics() {
        return cosmetics;
    }

    public void teleport(Location loc) {
        player.teleport(loc);
    }

    public Player getPlayer() {
        if (player == null) {
            tryGetPlayer();
        }
        return player;
    }

    @Nullable
    public KillPerk killPerk;

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
        this.name = player.getName();
    }

    public void upsert() {
        toRecord().upsert(QuakeDB.playerTable);
    }

    public WbsRecord toRecord() {
        WbsRecord record = new WbsRecord(QuakeDB.getDatabase());

        record.setField(QuakeDB.uuidField, uuid);
        record.setField(QuakeDB.nameField, name);

        record.setField(QuakeDB.killsField, kills);
        record.setField(QuakeDB.headshotsField, headshots);
        record.setField(QuakeDB.winsField, wins);
        record.setField(QuakeDB.playedField, played);
        record.setField(QuakeDB.deathsField, deaths);

        currentGun.toRecord(record);
        cosmetics.toRecord(record);

        return record;
    }

    public void writeToConfig(ConfigurationSection section) {
        if (name != null) {
            section.set(uuid + ".name", name);
        }

        setIfNotZero(section, uuid + ".played", played);
        setIfNotZero(section, uuid + ".wins", wins);
        setIfNotZero(section, uuid + ".kills", kills);
        setIfNotZero(section, uuid + ".headshots", headshots);
        setIfNotZero(section, uuid + ".deaths", deaths);

        if (killPerk != null)
            section.set(uuid + ".kill-perk", killPerk.getId());

        currentGun.writeToConfig(section, uuid + ".gun");
        cosmetics.writeToConfig(section, uuid + ".cosmetics");

        // Remove cosmetics if everything was default and therefore empty
        ConfigurationSection cosmeticsSection = section.getConfigurationSection(uuid + ".cosmetics");
        if (cosmeticsSection != null && cosmeticsSection.getKeys(false).size() == 0) {
            section.set(uuid + ".cosmetics", null);
        }

        ConfigurationSection gunSection = section.getConfigurationSection(uuid + ".gun");
        if (gunSection != null && gunSection.getKeys(false).size() == 0) {
            section.set(uuid + ".gun", null);
        }

        // If it's just their name left because everything else was default, don't save this at all
        ConfigurationSection playerSection = section.getConfigurationSection(uuid.toString());
        if (playerSection != null && playerSection.getKeys(false).size() <= 1) {
            section.set(uuid.toString(), null);
        }
    }

    private void setIfNotZero(ConfigurationSection section, String path, int value) {
        section.set(path, value != 0 ? value : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuakePlayer)) return false;
        QuakePlayer that = (QuakePlayer) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
