package wbs.quake.command.arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.quake.WbsQuake;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsMessageBuilder;

public class ArenaListSubcommand extends WbsSubcommand {
    public ArenaListSubcommand(WbsQuake plugin) {
        super(plugin, "list");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        boolean isPlayer = sender instanceof Player;

        String tpPerm = "wbsquake.arena.tp";
        boolean canTeleport = isPlayer && sender.hasPermission(tpPerm);

        int i = 1;
        for (Arena arena : ArenaManager.getAllArenas()) {
            WbsMessageBuilder builder = plugin.buildMessage("&6" + i + ") &h" + arena.getDisplayName()
                    + " &r(" + arena.getMinPlayers() + "-" + arena.getMaxPlayers() + " players)");

            if (canTeleport) {
                builder.append(" &6&l[TP]")
                        .addHoverText("Click to tp!")
                        .addClickCommand("/" + label + " arena tp " + arena.getName());
            }

            if (isPlayer) {
                builder.append(" &h&l[INFO]")
                        .addHoverText("Click to view info!")
                        .addClickCommand("/" + label + " arena info " + arena.getName());
            }

            builder.send(sender);

            i++;
        }

        return true;
    }
}
