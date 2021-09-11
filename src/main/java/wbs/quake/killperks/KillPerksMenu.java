package wbs.quake.killperks;

import org.bukkit.Material;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.menus.MenuManager;
import wbs.quake.menus.PlayerSelectionMenu;
import wbs.quake.menus.SelectableSlot;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.MenuSlot;

public class KillPerksMenu extends PlayerSelectionMenu<KillPerk> {
    public KillPerksMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&4&lKill Perks", 6, "killperks");

        MenuSlot border = new MenuSlot(plugin, Material.RED_STAINED_GLASS_PANE, "&r");
        setOutline(border);
        setColumn(2, border);
        setSlot(2, 1, border);

        setSlot(3, 1, MenuManager.getBalSlot());
        setSlot(4, 1, MenuManager.getBackToShopSlot());

        minSlot = 2;

        setCurrent(player.killPerk);
        plugin.settings.allKillPerks().forEach(this::addSlot);
    }

    @Override
    protected void setCurrent(SelectableSlot<KillPerk> slot) {
        if (slot != null) {
            setSlot(1, 1, slot);
        }
    }

    @Override
    protected SelectableSlot<KillPerk> getSlotFor(KillPerk selected) {
        if (selected == null) return null;
        return new KillPerkSlot(this, selected);
    }
}
