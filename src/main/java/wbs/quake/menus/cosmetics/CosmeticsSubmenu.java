package wbs.quake.menus.cosmetics;

import org.bukkit.Material;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.SelectableCosmetic;
import wbs.quake.menus.MenuManager;
import wbs.quake.menus.PlayerSpecificMenu;
import wbs.quake.cosmetics.SelectableSlot;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.MenuSlot;

public class CosmeticsSubmenu<T extends SelectableCosmetic> extends PlayerSpecificMenu {
    public CosmeticsSubmenu(WbsQuake plugin, QuakePlayer player, String title, String id) {
        super(plugin, player, title, 6, id);

        MenuSlot border = new MenuSlot(plugin, Material.MAGENTA_STAINED_GLASS_PANE, "&r");
        setOutline(border);
        setColumn(2, border);
        setSlot(2, 1, border);

        setSlot(3, 1, MenuManager.getBalSlot());
        setSlot(4, 1, MenuManager.getBackToCosmeticsSlot());
    }

    public void setCurrent(T cosmetic) {
        SelectableSlot<T> slot = new SelectableSlot<>(this, cosmetic);

        setSlot(1, 1, slot);
    }

    protected void addCosmetic(T cosmetic) {
        SelectableSlot<T> slot = new SelectableSlot<>(this, cosmetic);

        setNextFreeSlot(slot);
    }

    protected void addCosmetic(SelectableSlot<T> cosmeticSlot) {
        setNextFreeSlot(cosmeticSlot);
    }

    public void updateSelected(SelectableSlot<T> slot) {
        setSlot(1, 1, slot);
        update();
    }
}
