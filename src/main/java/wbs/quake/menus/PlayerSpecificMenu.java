package wbs.quake.menus;

import org.jetbrains.annotations.NotNull;
import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.WbsMenu;

public abstract class PlayerSpecificMenu extends WbsMenu {

    @NotNull
    protected final QuakePlayer player;

    public PlayerSpecificMenu(WbsQuake plugin, @NotNull QuakePlayer player, String title, int rows, String id) {
        super(plugin, title, rows, id + ":" + player.getName());

        this.player = player;
    }

    @NotNull
    public QuakePlayer getPlayer() {
        return player;
    }
}
