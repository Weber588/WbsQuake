package wbs.quake.menus.cosmetics;

import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;

public class KillMessageMenu extends CosmeticsSubmenu {
    public KillMessageMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&7&lKill Messages", "kmsg");
    }
}
