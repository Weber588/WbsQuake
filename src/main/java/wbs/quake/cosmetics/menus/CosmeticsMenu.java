package wbs.quake.cosmetics.menus;

import org.bukkit.Material;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.menus.MenuManager;
import wbs.quake.menus.PlayerSpecificMenu;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

public class CosmeticsMenu extends PlayerSpecificMenu {
    public CosmeticsMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&d&lCosmetics", 3, "cosmetics");

        setOutline(new MenuSlot(plugin, Material.MAGENTA_STAINED_GLASS_PANE, "&r"));

        setSlot(2, 8, MenuManager.getBackToShopSlot(player));

        MenuSlot slot;

        CosmeticsStore store = CosmeticsStore.getInstance();

        if (store.skinsEnabled) {
            slot = new MenuSlot(plugin, Material.DIAMOND_HOE,
                    "&bGun Skin",
                    "&7Choose what item your gun",
                    "&7appears as!"
            );
            slot.setClickAction(inventoryClickEvent ->
                    MenuManager.openMenuFor(player, SkinMenu.class)
            );
            setNextFreeSlot(slot);
        }

        if (store.trailsEnabled) {
            slot = new MenuSlot(plugin, Material.FIREWORK_ROCKET,
                    "&bTrails",
                    "&7Choose what particle appears",
                    "&7when you shoot!"
            );
            slot.setClickAction(inventoryClickEvent ->
                    MenuManager.openMenuFor(player, TrailMenu.class)
            );
            setNextFreeSlot(slot);
        }

        if (store.armourEnabled) {
            slot = new MenuSlot(plugin, Material.DIAMOND_CHESTPLATE,
                    "&dArmour",
                    "&7Choose what armour you get",
                    "&7in rounds!"
            );
            slot.setClickAction(inventoryClickEvent ->
                    MenuManager.openMenuFor(player, ArmourMenu.class)
            );
            setNextFreeSlot(slot);
        }

        if (store.killMessagesEnabled) {
            slot = new MenuSlot(plugin, Material.OAK_SIGN,
                    "&8Kill Messages",
                    "&7Choose what message appears",
                    "&7when you kill a player!"
            );
            slot.setClickAction(inventoryClickEvent ->
                    MenuManager.openMenuFor(player, KillMessageMenu.class)
            );
            setNextFreeSlot(slot);
        }

        if (false) {
            slot = new MenuSlot(plugin, Material.SKELETON_SKULL,
                    "&5Death Effects",
                    "&7Choose what effect plays",
                    "&7when you die!"
            );
            setNextFreeSlot(slot);
        }

        if (store.deathSoundsEnabled) {
            slot = new MenuSlot(plugin, Material.NOTE_BLOCK,
                    "&cDeath Sounds",
                    "&7Choose what sounds plays",
                    "&7when you die!"
            );
            slot.setClickAction(inventoryClickEvent ->
                    MenuManager.openMenuFor(player, DeathSoundsMenu.class)
            );
            setNextFreeSlot(slot);
        }

        if (store.shootSoundsEnabled) {
            slot = new MenuSlot(plugin, Material.BELL,
                    "&eShoot Sounds",
                    "&7Choose what sounds plays",
                    "&7when you shoot!"
            );
            slot.setClickAction(inventoryClickEvent ->
                    MenuManager.openMenuFor(player, ShootSoundsMenu.class)
            );
            setNextFreeSlot(slot);
        }
    }
}
