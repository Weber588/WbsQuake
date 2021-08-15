package wbs.quake;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import wbs.quake.command.QuakeCommand;
import wbs.quake.listeners.QuakeListener;
import wbs.quake.player.PlayerManager;
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

        ArenaManager.setPlugin(this);
        Arena.setPlugin(this);

        // To initialize the class in case it's never called before the plugin disables
        QuakeLobby.getInstance();
        PlayerManager.initialize();

        settings.loadArenas();
        settings.loadPlayers();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new QuakeListener(this), this);

        new QuakeCommand(this, getCommand("wbsquake"));
    }

    @Override
    public void onDisable() {
        QuakeLobby.getInstance().kickAll();
        settings.savePlayers();
        settings.saveArenas();

        ArenaManager.getAllArenas().forEach(Arena::finish);
    }
}
