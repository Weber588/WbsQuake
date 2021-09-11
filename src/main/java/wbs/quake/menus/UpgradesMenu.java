package wbs.quake.menus;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import wbs.quake.Gun;
import wbs.quake.WbsQuake;
import wbs.quake.killperks.KillPerk;
import wbs.quake.killperks.KillPerkSlot;
import wbs.quake.player.QuakePlayer;
import wbs.quake.upgrades.UpgradeableOption;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Arrays;
import java.util.Objects;

public class UpgradesMenu extends PlayerSelectionMenu<KillPerk> {

    public static final int BAL_SLOT = 8;

    public UpgradesMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&9&lUpgrades", 6, "upgrade:" + player.getName());
        MenuSlot borderItem = new MenuSlot(plugin, Material.CYAN_STAINED_GLASS_PANE, "&r");
        setOutline(borderItem);
        setRow(3, borderItem);

        setSlot(BAL_SLOT, MenuManager.getBalSlot());
        setSlot(5, 8, MenuManager.getBackToShopSlot());

        MenuSlot modifiersLabel = new MenuSlot(
                plugin,
                Material.ENCHANTED_GOLDEN_APPLE,
                "&b&lModifiers",
                plugin.dynamicColourise("&7Upgrade your gun here!"));
        setSlot(0, 4, modifiersLabel);

        MenuSlot killPerksLabel = new MenuSlot(
                plugin,
                Material.DIAMOND_SWORD,
                "&4&lKill Perks",
                true,
                plugin.colouriseAll(Arrays.asList(
                        "&7Kill perks are bonuses",
                        "&7that get applied when you",
                        "&7kill another player!",
                        "&7You can have 1 selected at a time."
                )));
        setSlot(3, 4, killPerksLabel);

        Gun gun = player.getCurrentGun();

        UpgradeableOption cooldownOption = gun.getCooldownOption();
        if (cooldownOption.getPath().length() > 1) {
            UpgradePathSlot slot =
                    new UpgradePathSlot(plugin,
                            cooldownOption,
                            Material.NETHERITE_HOE,
                            "&7Firing Cooldown: &h%value%");
            setNextFreeSlot(slot);
        }

        UpgradeableOption leapSpeedOption = gun.getLeapSpeedOption();
        if (leapSpeedOption.getPath().length() > 1) {
            UpgradePathSlot slot =
                    new UpgradePathSlot(plugin,
                            leapSpeedOption,
                            Material.RABBIT_FOOT,
                            "&2Leap Speed: &h%value%");
            setNextFreeSlot(slot);
        }

        UpgradeableOption leapCooldownOption = gun.getLeapCooldownOption();
        if (leapCooldownOption.getPath().length() > 1) {
            UpgradePathSlot slot =
                    new UpgradePathSlot(plugin,
                            leapCooldownOption,
                            Material.SLIME_BALL,
                            "&2Leap Cooldown: &h%value%");
            setNextFreeSlot(slot);
        }

        UpgradeableOption speedOption = gun.getSpeedOption();
        if (speedOption.getPath().length() > 1) {

            ItemStack speedPotion = new ItemStack(Material.POTION);

            PotionMeta meta = Objects.requireNonNull((PotionMeta) speedPotion.getItemMeta());
            meta.setColor(PotionEffectType.SPEED.getColor());

            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

            speedPotion.setItemMeta(meta);

            UpgradePathSlot slot =
                    new UpgradePathSlot(plugin,
                            speedPotion,
                            speedOption,
                            "&9Speed: &h%value%");
            setNextFreeSlot(slot);
        }

        UpgradeableOption piercingCooldownOption = gun.getPiercingOption();
        if (piercingCooldownOption.getPath().length() > 1) {
            UpgradePathSlot slot =
                    new UpgradePathSlot(plugin,
                            piercingCooldownOption,
                            Material.ARROW,
                            "&2Piercing: &h%value%");
            setNextFreeSlot(slot);
        }
    }

    @Override
    protected void setCurrent(SelectableSlot<KillPerk> slot) {

    }

    @Override
    protected SelectableSlot<KillPerk> getSlotFor(KillPerk selected) {
        return selected.buildSlot();
    }
}
