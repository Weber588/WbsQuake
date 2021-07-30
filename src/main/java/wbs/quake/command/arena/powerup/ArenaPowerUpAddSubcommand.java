package wbs.quake.command.arena.powerup;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.quake.WbsQuake;
import wbs.quake.powerups.PowerUp;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ArenaPowerUpAddSubcommand extends WbsSubcommand {
    private WbsQuake plugin;
    public ArenaPowerUpAddSubcommand(WbsQuake plugin) {
        super(plugin, "add");

        this.plugin = plugin;
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (args.length <= start) {
            sendUsage("<powerup> <arena>", sender, label, args);
            return true;
        }

        PowerUp powerUp = plugin.settings.powerUps.get(args[start]);

        if (powerUp == null) {
            sendMessage("Invalid powerup: &w" + args[start]
                    + "&r. Please choose from the following: "
                    + String.join(", ", plugin.settings.powerUps.keySet()), sender);
            return true;
        }

        if (args.length <= start + 1) {
            sendUsage("<arena>", sender, label, args);
            return true;
        }

        Arena arena = ArenaManager.getArena(args[start + 1]);

        if (arena == null) {
            sendMessage("Invalid arena: &w" + args[start + 1]
                    + "&r. Please choose from the following: &h"
                    + String.join(", ", ArenaManager.getArenaNames()), sender);
            return true;
        }

        Player player = (Player) sender;

        arena.addPowerUp(player.getLocation().add(0, 1, 0), powerUp);
        sendMessage("Powerup created!", sender);

        return true;
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start) {
            return new LinkedList<>(plugin.settings.powerUps.keySet());
        } else if (args.length == start + 1) {
            return ArenaManager.getArenaNames();
        }

        return new LinkedList<>();
    }
}
