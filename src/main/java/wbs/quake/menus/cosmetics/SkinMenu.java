package wbs.quake.menus.cosmetics;

import wbs.quake.menus.PlayerSpecifiMenu;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.plugin.WbsPlugin;

public class SkinMenu extends PlayerSpecifiMenu {

    public SkinMenu(WbsPlugin plugin, QuakePlayer player) {
        super(plugin, player, "&9&lSkins", 6, "skins");

    }
}
