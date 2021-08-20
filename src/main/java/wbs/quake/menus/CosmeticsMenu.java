package wbs.quake.menus;

import org.bukkit.Material;
import wbs.quake.menus.cosmetics.ArmourMenu;
import wbs.quake.menus.cosmetics.SkinMenu;
import wbs.quake.menus.cosmetics.TrailMenu;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

public class CosmeticsMenu extends WbsMenu {
    public CosmeticsMenu(WbsPlugin plugin) {
        super(plugin, "&d&lCosmetics", 3, "cosmetics");

        setOutline(new MenuSlot(plugin, Material.MAGENTA_STAINED_GLASS_PANE, "&r"));

        setSlot(2, 8, MenuManager.getBackToShopSlot());

        MenuSlot slot;

        slot = new MenuSlot(plugin, Material.DIAMOND_HOE,
                "&bGun Skin",
                "&7Choose what item your gun",
                "&7appears as!"
        );
        slot.setClickAction(inventoryClickEvent ->
                MenuManager.openMenuFor(inventoryClickEvent.getWhoClicked(), SkinMenu.class)
        );
        setNextFreeSlot(slot);

        slot = new MenuSlot(plugin, Material.FIREWORK_ROCKET,
                "&bTrails",
                "&7Choose what particle appears",
                "&7when you shoot!"
        );
        slot.setClickAction(inventoryClickEvent ->
                MenuManager.openMenuFor(inventoryClickEvent.getWhoClicked(), TrailMenu.class)
        );
        setNextFreeSlot(slot);

        slot = new MenuSlot(plugin, Material.DIAMOND_CHESTPLATE,
                "&dArmour",
                "&7Choose what armour you get",
                "&7in rounds!"
        );
        slot.setClickAction(inventoryClickEvent ->
                MenuManager.openMenuFor(inventoryClickEvent.getWhoClicked(), ArmourMenu.class)
        );
        setNextFreeSlot(slot);

        slot = new MenuSlot(plugin, Material.OAK_SIGN,
                "&8Kill Messages",
                "&7Choose what message appears",
                "&7when you kill a player!"
        );
        setNextFreeSlot(slot);

        slot = new MenuSlot(plugin, Material.SKELETON_SKULL,
                "&5Kill Effects",
                "&7Choose what happens when",
                "&7when you kill a player!"
        );
        setNextFreeSlot(slot);

        slot = new MenuSlot(plugin, Material.NOTE_BLOCK,
                "&cKill Sounds",
                "&7Choose what sounds plays",
                "&7when you kill a player!"
        );
        setNextFreeSlot(slot);

        slot = new MenuSlot(plugin, Material.BELL,
                "&eShoot Sounds",
                "&7Choose what sounds plays",
                "&7when you shoot!"
        );
        setNextFreeSlot(slot);
    }
}
