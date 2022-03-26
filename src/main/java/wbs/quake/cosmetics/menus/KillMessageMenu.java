package wbs.quake.cosmetics.menus;

import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.KillMessage;
import wbs.quake.player.QuakePlayer;

public class KillMessageMenu extends CosmeticsSubmenu<KillMessage> {
    public KillMessageMenu(WbsQuake plugin, QuakePlayer player) {
        super(plugin, player, "&7&lKill Messages", "kmsg");

        setCurrent(player.getCosmetics().killMessage);
        CosmeticsStore.getInstance().allKillMessages().forEach(this::addSlot);
    }
}
