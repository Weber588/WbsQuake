package wbs.quake.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Gun;
import wbs.quake.QuakeDB;
import wbs.quake.QuakeLobby;
import wbs.quake.WbsQuake;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GunSubcommand extends WbsSubcommand {
    private final WbsQuake plugin;
    public GunSubcommand(WbsQuake plugin) {
        super(plugin, "gun"); // TODO: Change this to shop when making menus
        this.plugin = plugin;

    }

    private enum GunArg {
        BOUNCES, COOLDOWN, SKIN, SHINY, LEAP_COOLDOWN, LEAP_SPEED, SPEED, PIERCING, GET
    }

    private void sendGunUsage(CommandSender sender, String label, String[] args) {
        sendUsage("<" +
                WbsEnums.toStringList(GunArg.class).stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.joining("|"))
                + ">", sender, label, args);
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (args.length == 1 || WbsEnums.getEnumFromString(GunArg.class, args[1]) == null) {
            sendGunUsage(sender, label, args);
            return true;
        }

        QuakePlayer player = QuakeLobby.getInstance().getPlayer((Player) sender);
        if (player == null) {
            PlayerManager.getPlayerAsync(
                    ((Player) sender),
                    (quakePlayer) -> parseCommand(quakePlayer, sender, label, args)
            );
        } else {
            parseCommand(player, sender, label, args);
        }

        return true;
    }

    private void parseCommand(QuakePlayer player, CommandSender sender, String label, String[] args) {
        Gun gun = player.getCurrentGun();

        switch (args.length) {
            case 2:
                GunArg arg = WbsEnums.getEnumFromString(GunArg.class, args[1]);

                switch (arg) {
                    case BOUNCES:
                        sendMessage("Bounces: &h" + gun.getBounces(), sender);
                        break;
                    case COOLDOWN:
                        sendMessage("Cooldown: &h" + gun.getCooldownOption().formattedValue(), sender);
                        break;
                    case SKIN:
                        sendMessage("Skin: " + WbsEnums.toPrettyString(gun.getSkin()), sender);
                        break;
                    case SHINY:
                        sendMessage("Shiny: " + gun.getShiny(), sender);
                        break;
                    case LEAP_COOLDOWN:
                        sendMessage("Leap Cooldown: " + gun.getLeapCooldownOption().formattedValue(), sender);
                        break;
                    case LEAP_SPEED:
                        sendMessage("Leap Speed: " + gun.getLeapSpeedOption().formattedValue(), sender);
                        break;
                    case GET:
                        sendMessage("Given gun.", sender);
                        player.getPlayer().getInventory().addItem(gun.buildGun());
                        break;
                    case SPEED:
                        sendMessage("Speed: " + gun.getSpeedOption().formattedValue(), sender);
                        break;
                    case PIERCING:
                        sendMessage("Speed: " + gun.getPiercingOption().formattedValue(), sender);
                        break;
                    default:
                        sendGunUsage(sender, label, args);
                        break;
                }
                break;
            default:
                GunArg arg1 = WbsEnums.getEnumFromString(GunArg.class, args[1]);
                switch (arg1) {
                    case BOUNCES:
                        sendMessage("Setting bounces to &h" + args[2], sender);
                        gun.setBounces(Integer.parseInt(args[2]));
                        break;
                    case COOLDOWN:
                        sendMessage("Setting cooldown progress to &h"
                                + (gun.setCooldownProgress(Integer.parseInt(args[2]) - 1) + 1), sender);
                        break;
                    case SKIN:
                        Material skin = WbsEnums.materialFromString(args[2], Material.WOODEN_HOE);
                        gun.setSkin(skin);
                        sendMessage("Set skin to " + skin, sender);
                        break;
                    case SHINY:
                        sendMessage("Setting shiny to &h" + args[2], sender);
                        gun.setShiny(Boolean.parseBoolean(args[2]));
                        break;
                    case LEAP_COOLDOWN:
                        sendMessage("Setting leap cooldown progress to &h"
                                + (gun.setLeapCooldownProgress(Integer.parseInt(args[2]) - 1) + 1), sender);
                        break;
                    case LEAP_SPEED:
                        sendMessage("Setting leap speed progress to &h"
                                + (gun.setLeapSpeedProgress(Integer.parseInt(args[2]) - 1) + 1), sender);
                        break;
                    case GET:
                        sendMessage("Given gun.", sender);
                        player.getPlayer().getInventory().addItem(gun.buildGun());
                        break;
                    case SPEED:
                        sendMessage("Setting speed progress to &h"
                                + (gun.setSpeedProgress(Integer.parseInt(args[2]) - 1) + 1), sender);
                        break;
                    case PIERCING:
                        sendMessage("Setting piercing progress to &h"
                                + (gun.setPiercingProgress(Integer.parseInt(args[2]) - 1) + 1), sender);
                        break;
                    default:
                        sendGunUsage(sender, label, args);
                        break;
                }
                break;

        }
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        if (args.length == 2) {
            choices.addAll(WbsEnums.toStringList(GunArg.class));
        }

        return choices;
    }
}
