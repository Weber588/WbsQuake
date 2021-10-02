package wbs.quake;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.database.*;

import java.nio.Buffer;
import java.util.*;
import java.util.function.Consumer;

public final class QuakeDB {

    private QuakeDB() {}

    private static WbsQuake plugin;

    private static WbsDatabase database;
    public static WbsDatabase getDatabase() {
        return database;
    }

    // Player table

    public static WbsTable playerTable;

    public static final WbsField uuidField = new WbsField("uuid", WbsFieldType.STRING);
    public static final WbsField nameField = new WbsField("name", WbsFieldType.STRING);
    public static final WbsField playedField = new WbsField("played", WbsFieldType.INT, 0);
    public static final WbsField winsField = new WbsField("wins", WbsFieldType.INT, 0);
    public static final WbsField killsField = new WbsField("kills", WbsFieldType.INT, 0);
    public static final WbsField headshotsField = new WbsField("headshots", WbsFieldType.INT, 0);
    public static final WbsField deathsField = new WbsField("deaths", WbsFieldType.INT, 0);

    public static final WbsField gunCooldownField = new WbsField("cooldown", WbsFieldType.INT, 0);
    public static final WbsField leapSpeedField = new WbsField("leap_speed", WbsFieldType.INT, 0);
    public static final WbsField leapCooldownField = new WbsField("leap_cooldown", WbsFieldType.INT, 0);
    public static final WbsField speedField = new WbsField("speed", WbsFieldType.INT, 0);
    public static final WbsField piercingField = new WbsField("piercing", WbsFieldType.INT, 0);

    public static final WbsField skinField =
            new WbsField("gun_skin", WbsFieldType.STRING, "WOODEN_HOE")
                    .setNotNull(true);
    public static final WbsField trailField = new WbsField("trail", WbsFieldType.STRING);
    public static final WbsField deathSoundField = new WbsField("death_sound", WbsFieldType.STRING);
    public static final WbsField shootSoundField = new WbsField("shoot_sound", WbsFieldType.STRING);

    //


    public static void setupDatabase() {
        plugin = WbsQuake.getInstance();
        database = new WbsDatabase(plugin, "quake");

        playerTable = new WbsTable(database, "players", uuidField);
        playerTable.addField(
                nameField,
                playedField,
                winsField,
                killsField,
                headshotsField,
                deathsField,

                gunCooldownField,
                leapSpeedField,
                leapCooldownField,
                speedField,

                skinField,
                trailField,
                deathSoundField
        );

        database.addTable(playerTable);

        if (!database.createDatabase()) {
            return;
        }

        if (database.createTables()) {
            addNewFields();
        }
    }


    /**
     * Add new fields added after the initial run.
     */
    private static void addNewFields() {
    //    playerTable.addFieldIfNotExists(newField);
    }
}
