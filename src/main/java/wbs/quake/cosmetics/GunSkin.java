package wbs.quake.cosmetics;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class GunSkin extends SelectableCosmetic<GunSkin> {
    public GunSkin(String id, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);
    }

    @Override
    public CosmeticType getCosmeticType() {
        return CosmeticType.SKIN;
    }
}
