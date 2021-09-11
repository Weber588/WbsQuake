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

        settings = new QuakeSettings(this);
        settings.reload();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new QuakeListener(this), this);
        pm.registerEvents(new ItemsListener(this), this);

        new QuakeCommand(this, getCommand("wbsquake"));
    }

    @Override
    public void onDisable() {
        QuakeLobby.getInstance().kickAll();
        settings.savePlayers();
        settings.saveArenas();
        settings.saveLobbySpawn();

        ArenaManager.getAllArenas().forEach(Arena::finish);
    }
}
