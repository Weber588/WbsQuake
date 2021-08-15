package wbs.quake.menus;

import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

public abstract class PlayerSpecifiMenu extends WbsMenu {

    protected final QuakePlayer player;

    public PlayerSpecifiMenu(WbsPlugin plugin, QuakePlayer player, String title, int rows, String id) {
        super(plugin, title, rows, id + ":" + player.getName());

        this.player = player;
    }
}
