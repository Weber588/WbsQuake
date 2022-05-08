package wbs.quake.upgrades;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.quake.EconomyUtil;
import wbs.quake.QuakeDB;
import wbs.quake.player.PlayerManager;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class UpgradePathSlot extends MenuSlot {

    private final UpgradeableOption option;
    private final String displayName;

    public UpgradePathSlot(@NotNull WbsPlugin plugin, UpgradeableOption option, @NotNull Material material, @NotNull String displayName) {
        super(plugin, material, displayName);
        this.option = option;
        this.displayName = displayName;

        setClickActionMenu(this::click);
    }

    public UpgradePathSlot(@NotNull WbsPlugin plugin, @NotNull ItemStack item, UpgradeableOption option, String displayName) {
        super(plugin, item);
        this.option = option;
        this.displayName = displayName;

        setClickActionMenu(this::click);
    }

    private void click(WbsMenu menu, InventoryClickEvent event) {
        Player player = ((Player) event.getWhoClicked());

        int current = option.getCurrentProgress();
        UpgradePath path = option.getPath();

        if (current >= path.length() - 1) {
            plugin.sendMessage("Fully upgraded already!", player);
            return;
        }

        double cost = path.getPrice( current + 1);
        double nextVal = path.getValue( current + 1);

        QuakeDB.getPlayerManager().getAsync(player, qPlayer -> {
            if (EconomyUtil.hasMoney(qPlayer, cost)) {
                EconomyUtil.takeMoney(qPlayer, cost);

                option.setCurrentProgress(current + 1);
                menu.update(event.getSlot());
                menu.update(UpgradesMenu.BAL_SLOT);

                plugin.sendMessage("Bought upgrade for &h" + EconomyUtil.formatMoney(cost) + "&r! New: " + path.format(nextVal), player);
            } else {
                plugin.sendMessage("Not enough money! Balance: &h"
                        + EconomyUtil.formatMoneyFor(qPlayer) + "&r. Cost: &h" + EconomyUtil.formatMoney(cost), player);
            }
        });
    }

    @Override
    public ItemStack getFormattedItem(@Nullable Player player) {
        ItemStack formatted = super.getFormattedItem(player);

        if (player == null || option == null) return formatted;

        ItemMeta meta = formatted.getItemMeta();
        assert meta != null;

        int current = option.getCurrentProgress();
        UpgradePath path = option.getPath();

        List<String> lore = new LinkedList<>();

        if (current >= path.length() - 1) {
            lore.add(plugin.dynamicColourise("&bFully upgraded!"));
            meta.addEnchant(Enchantment.LOYALTY, 0, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            double nextVal = path.getValue(current + 1);
            double nextPrice = path.getPrice(current + 1);

            lore.add(plugin.dynamicColourise("&6Next tier: &h" + path.format(nextVal)));
            lore.add(plugin.dynamicColourise("&6Cost: &h" + EconomyUtil.formatMoney(nextPrice)));
            lore.add(plugin.dynamicColourise("&7Click to upgrade!"));
        }

        double currentVal = path.getValue(current);

        String display = displayName.replaceAll("%value%", path.format(currentVal));
        meta.setDisplayName(plugin.dynamicColourise(display));
        meta.setLore(lore);

        formatted.setItemMeta(meta);

        return formatted;
    }
}
