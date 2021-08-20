package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.menus.MenuManager;
import wbs.quake.menus.ShopMenu;
import wbs.quake.menus.UpgradesMenu;
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

        Player player = (Player) sender;

        WbsMenu menu = null;
        if (args.length > 1) {
            switch (args[1].toLowerCase()) {
                case "upgrade":
                case "upgrades":
                    menu = MenuManager.getMenu(player, UpgradesMenu.class);
                    break;
                case "cosmetics":
                    menu = MenuManager.getCosmeticMenu();
                    break;
            }
        }

        if (menu == null) {
            menu = MenuManager.getMenu(player, ShopMenu.class);
        }

        menu.showTo(player);

        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        choices.add("upgrades");
        choices.add("cosmetics");

        return choices;
    }
}
