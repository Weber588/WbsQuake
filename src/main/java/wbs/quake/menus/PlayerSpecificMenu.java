package wbs.quake.menus;

import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.WbsMenu;

public abstract class PlayerSpecificMenu extends WbsMenu {

    protected final QuakePlayer player;

    public PlayerSpecificMenu(WbsQuake plugin, QuakePlayer player, String title, int rows, String id) {
        super(plugin, title, rows, id + ":" + player.getName());

        this.player = player;
    }

    public QuakePlayer getPlayer() {
        return player;
    }
}
