package wbs.quake.cosmetics.menus;

import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.trails.Trail;
import wbs.quake.player.QuakePlayer;

public class TrailMenu extends CosmeticsSubmenu<Trail> {
    public TrailMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&3&lTrails", "trails");

        setCurrent(player.getCosmetics().trail);
        CosmeticsStore.getInstance().allTrails().forEach(this::addSlot);
    }
}
