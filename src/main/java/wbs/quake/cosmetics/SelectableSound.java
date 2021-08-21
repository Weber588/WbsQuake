package wbs.quake.cosmetics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import wbs.quake.WbsQuake;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsSound;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.List;

public abstract class SelectableSound extends SelectableCosmetic<SelectableSound> {

    private final WbsSound sound;

    public SelectableSound(String id, Material material, String display, String permission, List<String> description, double price, Sound sound, float volume, float pitch) {
        super(id, material, display, permission, description, price);
        this.sound = new WbsSound(sound, volume, pitch);
    }

    public SelectableSound(ConfigurationSection section, String directory) {
        super(section, directory);

        WbsConfigReader.requireNotNull(section, "sound", settings, directory);

        String soundString = section.getString("sound");
        assert soundString != null;

        Sound sound = WbsEnums.getEnumFromString(Sound.class, soundString);
        float pitch = (float) section.getDouble("pitch");
        float volume = (float) section.getDouble("volume");

        this.sound = new WbsSound(sound, pitch, volume);
    }

    public void play(Location location) {
        sound.play(location);
    }
}
