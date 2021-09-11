package wbs.quake.menus;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.QuakeSettings;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.SelectableCosmetic;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class MenuSelectable {

    protected final WbsQuake plugin;
    protected final QuakeSettings settings;

    protected final String id;
    public final Material material;
    public final String display;
    public final String permission;
    public final List<String> description = new LinkedList<>();
    public final double price;

    public MenuSelectable(String id, Material material, String display, String permission, List<String> description, double price) {
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

    public MenuSelectable(ConfigurationSection section, String directory) {
        WbsConfigReader.requireNotNull(section, "item", WbsQuake.getInstance().settings, directory);
        WbsConfigReader.requireNotNull(section, "display", WbsQuake.getInstance().settings, directory);
        WbsConfigReader.requireNotNull(section, "price", WbsQuake.getInstance().settings, directory);

        id = section.getName();
        this.material = WbsEnums.materialFromString(section.getString("item"), Material.BARRIER);
        this.display = section.getString("display");
        if (!section.getStringList("description").isEmpty()) {
            this.description.addAll(section.getStringList("description"));
        }
        this.price = section.getDouble("price");

        plugin = WbsQuake.getInstance();
        settings = plugin.settings;

        this.permission = section.getString("permission", getPermission());
    }

    public final String getId() {
        return id;
    }
    public abstract String getPermission();

    /**
     * Accepts the lore of the generated item, and
     * modifies it before returning a new list to be
     * used as the lore.
     * @param lore The lore to modify
     * @return The new lore to be shown on the item.
     */
    public List<String> updateLore(List<String> lore) {
        return lore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuSelectable)) return false;
        MenuSelectable that = (MenuSelectable) o;

        return this.id.equalsIgnoreCase(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
