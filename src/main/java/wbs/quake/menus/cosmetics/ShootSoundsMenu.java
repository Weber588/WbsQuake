package wbs.quake.menus.cosmetics;

import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.ShootSound;
import wbs.quake.player.QuakePlayer;

public class ShootSoundsMenu extends CosmeticsSubmenu<ShootSound> {
    public ShootSoundsMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&e&lShoot Sounds", "shoot_sounds");

        setCurrent(player.getCosmetics().shootSound);
        CosmeticsStore.getInstance().allShootSounds().forEach(this::addSlot);
    }
}
