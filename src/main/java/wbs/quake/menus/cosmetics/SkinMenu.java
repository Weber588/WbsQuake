package wbs.quake.menus.cosmetics;

import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.GunSkin;
import wbs.quake.cosmetics.SelectableSlot;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.plugin.WbsPlugin;

public class SkinMenu extends CosmeticsSubmenu<GunSkin> {

    public SkinMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&9&lSkins", "skins");

        setCurrent(player.getCosmetics().skin);
        CosmeticsStore.getInstance().allSkins().forEach(this::addCosmetic);
    }
    /*(
    skin -> {
            SelectableSlot<GunSkin> slot = new SelectableSlot<>(this, skin);

            slot.setClickAction();

            addCosmetic(slot);
        }
     */
}
