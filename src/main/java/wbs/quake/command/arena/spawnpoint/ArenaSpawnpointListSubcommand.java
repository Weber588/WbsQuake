package wbs.quake.command.arena.spawnpoint;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStringify;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArenaSpawnpointListSubcommand extends WbsSubcommand {
    public ArenaSpawnpointListSubcommand(WbsPlugin plugin) {
        super(plugin, "list");
    }


    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (args.length <= start) {
            sendUsage("<arena>", sender, label, args);
            return true;
        }

        Arena arena = ArenaManager.getArena(args[start]);

        if (arena == null) {
            sendMessage("Invalid arena: &w" + args[start]
                    + "&r. Please choose from the following: &h"
                    + String.join(", ", ArenaManager.getArenaNames()), sender);
            return true;
        }

        List<Location> spawnpoints = arena.getSpawnpoints();

        if (spawnpoints.isEmpty()) {
            sendMessage("No spawnpoints defined for " + arena.getName(), sender);
            return true;
        }

        List<String> locationStrings = spawnpoints.stream()
                .map(spawn ->
                        spawn.getBlockX() + ", " +
                                spawn.getBlockY() + ", " +
                                spawn.getBlockZ() + ", " +
                                Objects.requireNonNull(spawn.getWorld()).getName())
                .collect(Collectors.toList());


        sendMessage("Showing " + locationStrings.size() + " spawnpoints (Hover for details):", sender);


        String tpPerm = "wbsquake.arena.spawnpoint.tp";
        boolean canTeleport = sender.hasPermission(tpPerm);

        String deletePerm = "wbsquake.arena.spawnpoint.remove";
        boolean canDelete = sender.hasPermission(deletePerm);

        int i = 0;
        for (Location loc : spawnpoints) {
            i++;

            String blockLoc = WbsStringify.toString(loc, false);
            String hoverString =
                    "&rX: &h" + loc.getX() + "\n" +
                            "&rY: &h" + loc.getY() + "\n" +
                            "&rZ: &h" + loc.getZ() + "\n" +
                            "&rPitch: &h" + loc.getPitch() + "\n" +
                            "&rYaw: &h" + loc.getYaw();

            WbsMessageBuilder messageBuilder = plugin.buildMessage("&6" + i + ") &r" + blockLoc)
                    .addHoverText(hoverString);

            // TELEPORT
            if (canTeleport) {
                messageBuilder.append(" &6&l[TP]&r")
                        .addClickCommand("/wbsquake arena spawnpoint tp " + arena.getName() + " " + loc.hashCode())
                        .addHoverText("&6Click to TP!");
            }

            if (canDelete) {
                messageBuilder.append(" &w&l[DELETE]&r")
                        .addClickCommand("/wbsquake arena spawnpoint remove " + arena.getName() + " " + loc.hashCode())
                        .addHoverText("&wClick to remove.");
            }

            messageBuilder.send(sender);
        }

        return true;
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start) {
            return ArenaManager.getArenaNames();
        }

        return new LinkedList<>();
    }
}
