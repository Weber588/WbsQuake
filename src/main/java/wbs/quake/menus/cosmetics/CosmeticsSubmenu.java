package wbs.quake.menus.cosmetics;

import org.bukkit.Material;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.SelectableCosmetic;
import wbs.quake.menus.MenuManager;
import wbs.quake.menus.PlayerSelectionMenu;
import wbs.quake.cosmetics.CosmeticSlot;
import wbs.quake.menus.SelectableSlot;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.MenuSlot;

public class CosmeticsSubmenu<T extends SelectableCosmetic> extends PlayerSelectionMenu<T> {
    public CosmeticsSubmenu(WbsQuake plugin, QuakePlayer player, String title, String id) {
        super(plugin, player, title, 6, id);

        MenuSlot border = new MenuSlot(plugin, Material.MAGENTA_STAINED_GLASS_PANE, "&r");
        setOutline(border);
        setColumn(2, border);
        setSlot(2, 1, border);

        setSlot(3, 1, MenuManager.getBalSlot());
        setSlot(4, 1, MenuManager.getBackToCosmeticsSlot());
    }

    public void setCurrent(SelectableSlot<T> slot) {
        setSlot(1, 1, slot);
    }

    @Override
    protected CosmeticSlot<T> getSlotFor(T selected) {
        return new CosmeticSlot<>(this, selected);
    }

    public void updateSelected(CosmeticSlot<T> slot) {
        setSlot(1, 1, slot);
        super.updateSelected(slot);
    }
}
