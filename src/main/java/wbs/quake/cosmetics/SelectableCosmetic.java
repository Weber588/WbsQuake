package wbs.quake.cosmetics;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.QuakeSettings;
import wbs.quake.WbsQuake;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.QuakePlayer;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class SelectableCosmetic<T extends SelectableCosmetic<T>> {

    public SelectableCosmetic(String id, Material material, String display, String permission, List<String> description, double price) {
        this.material = material;
        this.display = display;
        this.permission = permission;
        if (description != null)
            this.description.addAll(description);
        this.price = price;
        this.id = id;

        plugin = WbsQuake.getInstance();
        settings = plugin.settings;
    }

    public SelectableCosmetic(ConfigurationSection section, String directory) {
        WbsConfigReader.requireNotNull(section, "item", WbsQuake.getInstance().settings, directory);
        WbsConfigReader.requireNotNull(section, "display", WbsQuake.getInstance().settings, directory);
        WbsConfigReader.requireNotNull(section, "price", WbsQuake.getInstance().settings, directory);

        id = section.getName();
        this.material = WbsEnums.materialFromString(section.getString("item"), Material.BARRIER);
        this.display = section.getString("display");
        this.permission = section.getString("permission", "wbsquake.cosmetics." + getCosmeticType().name().toLowerCase() + "." + id);
        if (!section.getStringList("description").isEmpty()) {
            this.description.addAll(section.getStringList("description"));
        }
        this.price = section.getDouble("price");

        plugin = WbsQuake.getInstance();
        settings = plugin.settings;
    }

    private final String id;
    public final Material material;
    public final String display;
    public final String permission;
    public final List<String> description = new LinkedList<>();
    public final double price;

    protected final WbsQuake plugin;
    protected final QuakeSettings settings;

    public final String getId() {
        return id;
    }

    public abstract CosmeticType getCosmeticType();

    /**
     * Called when the cosmetic is selected by the given player
     * @param player The player who selected this cosmetic
     */
    public abstract void onSelect(QuakePlayer player, PlayerCosmetics cosmetics);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectableCosmetic)) return false;
        SelectableCosmetic<?> that = (SelectableCosmetic<?>) o;

        return this.id.equalsIgnoreCase(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
