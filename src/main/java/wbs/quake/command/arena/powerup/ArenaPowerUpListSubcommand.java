package wbs.quake.command.arena.powerup;

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
import wbs.quake.powerups.PowerUp;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStringify;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArenaPowerUpListSubcommand extends WbsSubcommand {
    public ArenaPowerUpListSubcommand(WbsPlugin plugin) {
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

        HashMap<Location, PowerUp> powerups = arena.getPowerUps();

        if (powerups.isEmpty()) {
            sendMessage("No powerups defined for " + arena.getName(), sender);
            return true;
        }

        List<String> locationStrings = powerups.keySet().stream()
                .map(spawn ->
                        spawn.getBlockX() + ", " +
                                spawn.getBlockY() + ", " +
                                spawn.getBlockZ() + ", " +
                                Objects.requireNonNull(spawn.getWorld()).getName())
                .collect(Collectors.toList());


        sendMessage("Showing " + locationStrings.size() + " powerups (Hover for details):", sender);


        String tpPerm = "wbsquake.arena.powerup.tp";
        boolean canTeleport = sender.hasPermission(tpPerm);

        String deletePerm = "wbsquake.arena.powerup.remove";
        boolean canDelete = sender.hasPermission(deletePerm);

        int i = 0;
        for (Location loc : powerups.keySet()) {
            i++;

            TextComponent fullMessage = new TextComponent();

            // LOCATION
            String blockLoc = WbsStringify.toString(loc, false);
            String hoverString =
                    "&rType: &h" + powerups.get(loc).getId() + "\n" +
                            "&rX: &h" + loc.getX() + "\n" +
                            "&rY: &h" + loc.getY() + "\n" +
                            "&rZ: &h" + loc.getZ();

            Text hoverText = new Text(plugin.dynamicColourise(hoverString));

            String locationString = plugin.dynamicColourise("&6" + i + ") &r" + blockLoc);
            TextComponent locationMessage = new TextComponent(locationString);
            locationMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

            fullMessage.addExtra(locationMessage);

            // TELEPORT
            if (canTeleport) {
                String tpString = plugin.dynamicColourise(" &6&l[TP]&r");
                TextComponent tpMessage = new TextComponent(tpString);

                Text tpHover = new Text(plugin.dynamicColourise("&6Click to TP!."));
                tpMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tpHover));

                tpMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wbsquake arena powerup tp " + arena.getName() + " " + loc.hashCode()));

                fullMessage.addExtra(tpMessage);
            }

            if (canDelete) {
                String deleteString = plugin.dynamicColourise(" &w&l[DELETE]&r");
                TextComponent deleteMessage = new TextComponent(deleteString);

                Text deleteHover = new Text(plugin.dynamicColourise("&wClick to remove."));
                deleteMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, deleteHover));

                deleteMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wbsquake arena powerup remove " + arena.getName() + " " + loc.hashCode()));

                fullMessage.addExtra(deleteMessage);
            }

            sender.spigot().sendMessage(fullMessage);
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
