package wbs.quake.killperks;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import wbs.quake.menus.SelectableSlot;
import wbs.quake.menus.UpgradesMenu;

public class KillPerkSlot extends SelectableSlot<KillPerk> {

    private final UpgradesMenu menu;

    public KillPerkSlot(UpgradesMenu menu, KillPerk selectable) {
        super(selectable);

        this.menu = menu;
    }

    @Override
    protected void onSuccessfulSelection(InventoryClickEvent event, KillPerk cosmetic) {
     //   menu.getPlayer().getCosmetics().setCosmetic(cosmetic);
        menu.update(event.getSlot());
        menu.update(UpgradesMenu.BAL_SLOT);
        menu.setCurrent(cosmetic);

        menu.updateSelected( this);
    }

    @Override
    protected boolean isSelected(Player player, KillPerk selectable) {
        return false;
    }
}
