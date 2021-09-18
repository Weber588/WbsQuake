package wbs.quake;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import wbs.utils.util.WbsEnums;

import java.util.List;
import java.util.Objects;

public class ItemManager {

    public static final NamespacedKey SHOP_ITEM_KEY = new NamespacedKey(WbsQuake.getInstance(), "lobby-shop");
    public static final NamespacedKey LEAVE_ITEM_KEY = new NamespacedKey(WbsQuake.getInstance(), "lobby-leave");
    public static final NamespacedKey QUAKE_COMPASS_KEY = new NamespacedKey(WbsQuake.getInstance(), "quake-compass");


    private static final WbsQuake plugin = WbsQuake.getInstance();

    private static ItemStack leaveItem;
    public static ItemStack getLeaveItem() {
        return leaveItem;
    }

    private static int leaveItemSlot = 8;
    public static int getLeaveItemSlot() {
        return leaveItemSlot;
    }

    private static ItemStack shopItem;
    public static ItemStack getShopItem() {
        return shopItem;
    }

    private static int shopItemSlot = 4;
    public static int getShopItemSlot() {
        return shopItemSlot;
    }

    private static ItemStack quakeCompass;
    public static ItemStack getQuakeCompass() {
        return quakeCompass;
    }

    private static int quakeCompassSlot = 1;
    public static int getQuakeCompassSlot() {
        return quakeCompassSlot;
    }

    private static int quakeGunSlot = 0;
    public static int getQuakeGunSlot() {
        return quakeGunSlot;
    }

    private static String quakeGunName = "&9&lRailgun";
    public static String getQuakeGunName() {
        return quakeGunName;
    }

    public static void loadItems(ConfigurationSection section, String directory) {
        ConfigurationSection lobbySection = section.getConfigurationSection("lobby");

        if (lobbySection == null) {
            plugin.logger.info("Lobby section missing from misc.yml! It is recommended that you regenerate the file.");
        } else {
            configureLobbyItems(lobbySection, directory + "/lobby");
        }

        ConfigurationSection quakeSection = section.getConfigurationSection("quake");
        if (quakeSection == null) {
            plugin.logger.info("Quake section missing from misc.yml! Using default items.");
        } else {
            configureQuakeItems(quakeSection, directory + "/quake");
        }
    }

    public static void configureQuakeItems(ConfigurationSection section, String directory) {
        ConfigurationSection compassSection = section.getConfigurationSection("quake-compass");
        if (compassSection == null) {
            plugin.settings.logError("quake-compass item missing!", directory + "/quake-compass");
        } else {
            compassSection.set("item", "COMPASS");
            quakeCompass = loadItem(compassSection, directory + "/quake-compass", "&6Nearest Player", QUAKE_COMPASS_KEY);
            quakeCompassSlot = getSlot(compassSection, quakeCompassSlot);
        }

        ConfigurationSection gunSection = section.getConfigurationSection("quake-gun");
        if (gunSection == null) {
            plugin.settings.logError("quake-gun item missing!", directory + "/quake-gun");
        } else {
            quakeGunSlot = getSlot(gunSection, quakeGunSlot);
            quakeGunName = gunSection.getString("display", quakeGunName);
        }
    }

    public static void configureLobbyItems(ConfigurationSection section, String directory) {
        ConfigurationSection leaveSection = section.getConfigurationSection("lobby-leave");
        if (leaveSection == null) {
            plugin.settings.logError("lobby-leave item missing. It is recommended that you regenerate your config.", directory + "/lobby-leave");
        } else {
            leaveItem = loadItem(leaveSection, directory + "/lobby-leave", "&c&lExit", LEAVE_ITEM_KEY);
            leaveItemSlot = getSlot(leaveSection, leaveItemSlot);
        }

        ConfigurationSection shopSection = section.getConfigurationSection("lobby-shop");
        if (shopSection == null) {
            plugin.settings.logError("lobby-shop item missing. It is recommended that you regenerate your config.", directory + "/lobby-shop");
        } else {
            shopItem = loadItem(shopSection, directory + "/lobby-shop", "&b&lShop", SHOP_ITEM_KEY);
            shopItemSlot = getSlot(shopSection, shopItemSlot);
        }
    }

    private static int getSlot(ConfigurationSection section, int defaultInt) {
        return section.getInt("slot", defaultInt);
    }

    private static ItemStack loadItem(ConfigurationSection section, String directory, String defaultName, NamespacedKey key) {
        String materialString = section.getString("item", "null");
        Material type = WbsEnums.getEnumFromString(Material.class, materialString);

        if (type == null) {
            plugin.settings.logError("Invalid item type: " + materialString, directory + "/item");
            type = Material.BARRIER;
        }

        String display = section.getString("display", defaultName);
        List<String> lore = section.getStringList("lore");

        ItemStack item = new ItemStack(type);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());

        meta.setDisplayName(plugin.dynamicColourise(display));
        meta.setLore(plugin.colouriseAll(lore));

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");

        item.setItemMeta(meta);

        return item;
    }
}
