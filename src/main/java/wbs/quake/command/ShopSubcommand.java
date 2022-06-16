package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeDB;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.menus.*;
import wbs.quake.killperks.KillPerksMenu;
import wbs.quake.menus.MenuManager;
import wbs.quake.menus.ShopMenu;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.quake.upgrades.UpgradesMenu;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ShopSubcommand extends WbsSubcommand {
    public ShopSubcommand(@NotNull WbsPlugin plugin) {
        super(plugin, "shop");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        QuakeDB.getPlayerManager().getAsync((Player) sender, (quakePlayer -> openMenu(quakePlayer, args)));

        return true;
    }

    private void openMenu(QuakePlayer player, String[] args) {
        WbsMenu menu = null;
        if (args.length > 1) {
            switch (args[1].toLowerCase()) {
                case "upgrade":
                case "upgrades":
                    menu = MenuManager.getMenu(player, UpgradesMenu.class);
                    break;
                case "cosmetics":
                    menu = MenuManager.getMenu(player, CosmeticsMenu.class);
                    if (args.length > 2) {
                        switch (args[2].toLowerCase()) {
                            case "skins":
                                menu = MenuManager.getMenu(player, SkinMenu.class);
                                break;
                            case "trails":
                                menu = MenuManager.getMenu(player, TrailMenu.class);
                                break;
                            case "kill_messages":
                                menu = MenuManager.getMenu(player, KillMessageMenu.class);
                                break;
                            case "death_sounds":
                                menu = MenuManager.getMenu(player, DeathSoundsMenu.class);
                                break;
                            case "shoot_sounds":
                                menu = MenuManager.getMenu(player, ShootSoundsMenu.class);
                                break;
                        }
                    }
                    break;
                case "killperks":
                    menu = MenuManager.getMenu(player, KillPerksMenu.class);
                    break;
            }
        }

        if (menu == null) {
            menu = MenuManager.getMenu(player, ShopMenu.class);
        }

        menu.showTo(player.getPlayer());
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> choices = new LinkedList<>();

        if (args.length == start) {
            choices.add("upgrades");
            choices.add("cosmetics");
            choices.add("killperks");
        } else if (args.length == start + 1) {
            if ("cosmetics".equalsIgnoreCase(args[start - 1])) {
                choices.add("skins");
                choices.add("trails");
                choices.add("kill_messages");
                choices.add("death_sounds");
                choices.add("shoot_sounds");
            }
        }

        return choices;
    }
}
