package wbs.quake.killperks;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.quake.menus.SelectableSlot;
import wbs.quake.player.QuakePlayer;
import wbs.quake.upgrades.UpgradesMenu;

public class KillPerkSlot extends SelectableSlot<KillPerk> {

    private final KillPerksMenu menu;

    public KillPerkSlot(KillPerksMenu menu, @NotNull KillPerk selectable) {
        super(selectable);

        this.menu = menu;
    }

    @Override
    public ItemStack getFormattedItem(@Nullable Player player) {
        ItemStack formattedItem = super.getFormattedItem(player);

        if (formattedItem.getItemMeta() instanceof PotionMeta) {
            if (selectable instanceof PotionKillPerk) {
                PotionKillPerk potionKillPerk = (PotionKillPerk) selectable;

                PotionMeta meta = (PotionMeta) formattedItem.getItemMeta();
                meta.addCustomEffect(potionKillPerk.effect, true);
                meta.setColor(potionKillPerk.effect.getType().getColor());

                formattedItem.setItemMeta(meta);
            }
        }

        return formattedItem;
    }

    @Override
    protected void onSuccessfulSelection(InventoryClickEvent event, KillPerk killPerk) {
        menu.getPlayer().killPerk = killPerk;
        menu.setCurrent(killPerk);

        menu.updateSelected(this);
    }

    @Override
    protected boolean isSelected(QuakePlayer player, KillPerk selectable) {
        return selectable.equals(player.killPerk);
    }
}
