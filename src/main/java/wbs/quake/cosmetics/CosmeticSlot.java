package wbs.quake.cosmetics;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import wbs.quake.menus.SelectableSlot;
import wbs.quake.menus.cosmetics.CosmeticsSubmenu;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;

public class CosmeticSlot<T extends SelectableCosmetic> extends SelectableSlot<T> {

    private final CosmeticsSubmenu<T> menu;

    public CosmeticSlot(CosmeticsSubmenu<T> menu, T cosmetic) {
        super(cosmetic);

        this.menu = menu;
    }

    @Override
    protected void onSuccessfulSelection(InventoryClickEvent event, T cosmetic) {
        menu.getPlayer().getCosmetics().setCosmetic(cosmetic);
        menu.setCurrent(cosmetic);

        menu.updateSelected(this);
    }

    @Override
    protected boolean isSelected(Player player, T cosmetic) {
        QuakePlayer quakePlayer = PlayerManager.getPlayer(player);
        PlayerCosmetics cosmetics = quakePlayer.getCosmetics();

        SelectableCosmetic current = cosmetics.getCosmetic(cosmetic.getCosmeticType());

        return current.equals(cosmetic);
    }
}
