package wbs.quake.cosmetics.menus;

import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.GunSkin;
import wbs.quake.player.QuakePlayer;

public class SkinMenu extends CosmeticsSubmenu<GunSkin> {

    public SkinMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&9&lSkins", "skins");

        setCurrent(player.getCosmetics().skin);
        CosmeticsStore.getInstance().allSkins().forEach(this::addSlot);
    }
    /*(
    skin -> {
            SelectableSlot<GunSkin> slot = new SelectableSlot<>(this, skin);

            slot.setClickAction();

            addCosmetic(slot);
        }
     */
}
