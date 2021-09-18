package wbs.quake.menus;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.quake.WbsQuake;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.pluginhooks.VaultWrapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class SelectableSlot<T extends MenuSelectable> extends MenuSlot {

    protected final WbsQuake plugin;
    @NotNull
    protected final T selectable;

    public SelectableSlot(@NotNull T selectable) {
        super(WbsQuake.getInstance(), selectable.material, selectable.display, selectable.description);

        this.selectable = selectable;
        plugin = WbsQuake.getInstance();

        setClickAction(this::onClick);
    }

    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!player.hasPermission(selectable.permission)) {
            if (selectable.purchasable) {
                if (VaultWrapper.hasMoney(player, selectable.price)) {
                    VaultWrapper.takeMoney(player, selectable.price);

                    plugin.sendMessage("Bought for &h" + VaultWrapper.formatMoney(selectable.price) + "&r!", player);

                    VaultWrapper.givePermission(player, selectable.permission);
                } else {
                    plugin.sendMessage("Not enough money! Balance: &w"
                            + VaultWrapper.formatMoneyFor(player) + "&r. Cost: &h" + VaultWrapper.formatMoney(selectable.price), player);
                    return;
                }
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

            List<String> lore = selectable.description.stream().map(line -> "&7" + line).collect(Collectors.toList());

            lore = selectable.updateLore(lore);

            QuakePlayer quakePlayer = PlayerManager.getPlayer(player);
            if (isSelected(quakePlayer, selectable)) {
                lore.add("&aSelected.");
                meta.addEnchant(Enchantment.LOYALTY, 1, true);
            } else {
                if (player.hasPermission(selectable.permission)) {
                    lore.add("&6Click to enable!");
                } else {
                    if (selectable.purchasable) {
                        lore.add("&6Cost: &h" + VaultWrapper.formatMoney(selectable.price));
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
