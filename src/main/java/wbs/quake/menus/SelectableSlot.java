package wbs.quake.menus;

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
import wbs.quake.QuakeLobby;
import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.pluginhooks.PlaceholderAPIWrapper;
import wbs.utils.util.pluginhooks.VaultWrapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class SelectableSlot<T extends MenuSelectable> extends MenuSlot {

    protected final WbsQuake plugin;
    @NotNull
    protected final T selectable;
    @NotNull
    protected final PlayerSpecificMenu menu;

    public SelectableSlot(@NotNull T selectable, @NotNull PlayerSpecificMenu menu) {
        super(WbsQuake.getInstance(), selectable.material, selectable.display, selectable.description);

        this.menu = menu;
        this.selectable = selectable;
        plugin = WbsQuake.getInstance();

        setFillPlaceholders(true);

        setClickAction(this::onClick);
    }

    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!player.hasPermission(selectable.permission)) {
            if (selectable.purchasable) {
                QuakeDB.getPlayerManager().getAsync(player, qPlayer -> {
                    if (EconomyUtil.hasMoney(qPlayer, selectable.price)) {
                        EconomyUtil.takeMoney(qPlayer, selectable.price);

                        plugin.sendMessage("Bought for &h" + EconomyUtil.formatMoney(selectable.price) + "&r!", player);

                        QuakeLobby.getInstance().refreshScoreboard(qPlayer);

                        VaultWrapper.givePermission(player, selectable.permission);
                    } else {
                        plugin.sendMessage("Not enough money! Balance: &w"
                                + EconomyUtil.formatMoneyFor(qPlayer) + "&r. Cost: &h" + EconomyUtil.formatMoney(selectable.price), player);
                    }
                });
            } else {
                plugin.sendMessage(selectable.costAlternativeMessage, player);
                return;
            }
        }

        onSuccessfulSelection(event, selectable);
    }

    @Override
    public ItemStack getFormattedItem(@Nullable Player player) {
        ItemStack item = super.getFormattedItem(player);

        if (player != null) {
            ItemMeta meta = Objects.requireNonNull(item.getItemMeta());

            List<String> lore = selectable.description.stream()
                    .map(line -> "&7" + line)
                    .map(line -> PlaceholderAPIWrapper.setPlaceholders(player, line))
                    .collect(Collectors.toList());

            lore = selectable.updateLore(lore);

            QuakePlayer quakePlayer = menu.getPlayer();
            if (isSelected(quakePlayer, selectable)) {
                lore.add("&aSelected.");
                meta.addEnchant(Enchantment.LOYALTY, 1, true);
            } else {
                if (player.hasPermission(selectable.permission)) {
                    lore.add("&6Click to enable!");
                } else {
                    if (selectable.purchasable) {
                        lore.add("&6Cost: &h" + EconomyUtil.formatMoney(selectable.price));
                    } else {
                        lore.add(selectable.costAlternativeMessage);
                    }
                }
            }

            meta.setLore(plugin.colouriseAll(lore));
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_POTION_EFFECTS,
                    ItemFlag.HIDE_DYE);

            item.setItemMeta(meta);
        }

        return item;
    }

    protected void onSuccessfulSelection(InventoryClickEvent event, T selectable) {

    }

    protected abstract boolean isSelected(QuakePlayer player, T selectable);
}
