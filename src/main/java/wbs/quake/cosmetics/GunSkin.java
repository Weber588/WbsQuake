package wbs.quake.cosmetics;

import org.bukkit.Material;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.QuakePlayer;

import java.util.List;

public class GunSkin extends SelectableCosmetic {
    public GunSkin(String id, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);
    }

    @Override
    public CosmeticType getCosmeticType() {
        return CosmeticType.SKIN;
    }

    @Override
    public void onSelect(QuakePlayer player, PlayerCosmetics cosmetics) {
        cosmetics.skin = this;
        player.getCurrentGun().setSkin(this.material);
    }
}
