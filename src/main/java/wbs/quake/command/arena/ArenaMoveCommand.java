package wbs.quake.command.arena;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.WbsQuake;

import java.util.HashMap;
import java.util.Map;

public class ArenaMoveCommand extends AbstractArenaCommand {

    private final WbsQuake plugin;

    public ArenaMoveCommand(@NotNull WbsQuake plugin) {
        super(plugin, "move");
        this.plugin = plugin;
    }

    private final Map<Player, Location> arenaMoveInstances = new HashMap<>();

    @Override
    public boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        Player player = (Player) sender;

        if (!arenaMoveInstances.containsKey(player)) {
            if (!player.getWorld().equals(arena.getSpawnpoints().get(0).getWorld())) {
                sendMessage("You must be in the same world as your arena!", player);
                return true;
            }

            arenaMoveInstances.put(player, player.getLocation());
            sendMessage("Location saved relative to the arena! Repeat this command to move to the new location.", player);
        } else {
            Location from = arenaMoveInstances.get(player);
            Location to = player.getLocation();

            arena.move(from, to);
            plugin.runAsync(() -> {
                plugin.settings.saveArenas();
                sendMessage("Arena moved!", player);
            });
            arenaMoveInstances.remove(player);
        }

        return true;
    }
}
