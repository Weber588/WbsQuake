package wbs.quake.menus.cosmetics;

import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.plugin.WbsPlugin;

public class SkinMenu extends CosmeticsSubmenu {

    public SkinMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&9&lSkins", "skins");
    }
}
