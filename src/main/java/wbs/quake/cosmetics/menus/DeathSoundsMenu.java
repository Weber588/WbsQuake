package wbs.quake.cosmetics.menus;

import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.DeathSound;
import wbs.quake.player.QuakePlayer;

public class DeathSoundsMenu extends CosmeticsSubmenu<DeathSound> {
    public DeathSoundsMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&4&lDeath Sounds", "death_sounds");

        setCurrent(player.getCosmetics().deathSound);
        CosmeticsStore.getInstance().allDeathSounds().forEach(this::addSlot);
    }
}
