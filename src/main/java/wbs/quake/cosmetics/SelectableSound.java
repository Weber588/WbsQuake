package wbs.quake.cosmetics;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public abstract class SelectableSound extends SelectableCosmetic<SelectableSound> {
    public SelectableSound(String id, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);
    }

    public SelectableSound(ConfigurationSection section, String directory) {
        super(section, directory);
    }
}
