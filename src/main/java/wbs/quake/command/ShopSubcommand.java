package wbs.quake.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.ArenaManager;
import wbs.quake.Gun;
import wbs.quake.PlayerManager;
import wbs.quake.QuakePlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class ShopSubcommand extends WbsSubcommand {
    public ShopSubcommand(WbsPlugin plugin) {
        super(plugin, "gun"); // TODO: Change this to shop when making menus


    }


    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        Player player = (Player) sender;
        QuakePlayer quakePlayer = PlayerManager.getPlayer(player);
        Gun gun = quakePlayer.getCurrentGun();

        switch (args.length) {
            case 1:
                sendUsage("<bounces|cooldown|skin|shiny>", sender, label, args);
                break;
            case 2:
                switch (args[1].toLowerCase()) {
                    case "bounces":
                    case "cooldown":
                    case "skin":
                    case "shiny":
                        sendUsage("<" + args[1] + ">", sender, label, args);
                        break;
                    default:
                        sendUsage("<bounces|cooldown|skin|shiny>", sender, label, args, args.length - 1);
                        break;
                }
                break;
            default:
                switch (args[1].toLowerCase()) {
                    case "bounces":
                        sendMessage("Setting bounces to &h" + args[2], sender);
                        gun.setBounces(Integer.parseInt(args[2]));
                        break;
                    case "cooldown":
                        sendMessage("Setting cooldown to &h" + args[2], sender);
                        gun.setCooldown(Integer.parseInt(args[2]));
                        break;
                    case "skin":
                        Material skin = WbsEnums.materialFromString(args[2], Material.WOODEN_HOE);
                        gun.setSkin(skin);
                        sendMessage("Set skin to " + skin, sender);
                        break;
                    case "shiny":
                        sendMessage("Setting shiny to &h" + args[2], sender);
                        gun.setShiny(Boolean.parseBoolean(args[2]));
                        break;
                    default:
                        sendUsage("<bounces|cooldown|skin|shiny>", sender, label, args);
                        break;
                }
                break;

        }

        return true;
    }
}
