package wbs.quake;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import wbs.quake.command.QuakeCommand;
import wbs.quake.listeners.*;
import wbs.utils.util.plugin.WbsPlugin;

public class WbsQuake extends WbsPlugin {

    public QuakeSettings settings;

    private static WbsQuake instance;
    public static WbsQuake getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        QuakeDB.setupDatabase();

        settings = new QuakeSettings(this);
        settings.reload();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new QuakeListener(this), this);
        pm.registerEvents(new ItemsListener(this), this);

        new QuakeCommand(this, getCommand("wbsquake"));

        PlaceholderManager.registerPlaceholders();
        StatsManager.recalculateAllAsync();
    }

    @Override
    public void onDisable() {
        QuakeLobby.getInstance().kickAll();

        ArenaManager.getAllArenas().forEach(Arena::finish);

        SaveManager.save();
    }
}
