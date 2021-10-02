package wbs.quake.cosmetics.trails;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticType;
import wbs.quake.cosmetics.SelectableCosmetic;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.QuakePlayer;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsColours;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.particles.WbsParticleEffect;

import java.util.List;

public abstract class Trail extends SelectableCosmetic {

    public enum TrailType {
        STANDARD, SPIRAL
    }

    @Nullable
    public static Trail buildTrail(ConfigurationSection section, String directory) throws InvalidConfigurationException {
        String typeString = section.getString("type", "STANDARD");

        TrailType type = WbsEnums.getEnumFromString(TrailType.class, typeString);
        if (type == null) {
            WbsQuake.getInstance().settings.logError("Invalid trail type: " + typeString, directory + "/type");
            return null;
        }

        switch (type) {
            case STANDARD:
                return new StandardTrail(section, directory);
            case SPIRAL:
                return new SpiralTrail(section, directory);
        }
        return null;
    }

    protected final Particle particle;
    protected int amountPerBlock = 5;
    protected Object data;

    protected WbsParticleEffect effect;

    public Trail(String id, Particle particle, Material material, String display, String permission, List<String> description, double price) {
        super(id, material, display, permission, description, price);
        this.particle = particle;
    }

    public Trail(ConfigurationSection section, String directory) {
        super(section, directory);

        WbsConfigReader.requireNotNull(section, "particle", settings, directory);

        String particleString = section.getString("particle", Particle.FIREWORKS_SPARK.name());
        particle = WbsEnums.getEnumFromString(Particle.class, particleString);
        if (particle == null) throw new InvalidConfigurationException("Invalid particle: " + particleString);
    }

    protected void configureOptions() {
        if (particle.getDataType() == Particle.DustOptions.class) {
            setOption(new Particle.DustOptions(Color.RED, 1f));
        }
    }

    protected void configure(ConfigurationSection section, String directory) {
        setAmountPerBlock(section.getInt("amount-per-block", amountPerBlock));

        if (particle.getDataType() == Particle.DustOptions.class) {
            String colourString = section.getString("colour", "red");

            Color colour = WbsColours.fromHexOrDyeString(colourString, Color.RED);

            float size = (float) section.getDouble("size", 1);

            Particle.DustOptions options = new Particle.DustOptions(colour, size);
            setOption(options);
        }
    }

    public abstract void playShot(Location pos1, Location pos2);

    public int getAmountPerBlock() {
        return amountPerBlock;
    }

    public void setAmountPerBlock(int amountPerBlock) {
        this.amountPerBlock = amountPerBlock;
    }

    public void setOption(Object data) {
        if (!particle.getDataType().isAssignableFrom(data.getClass())) {
            throw new IllegalArgumentException("Invalid data type for particle " + particle);
        }

        this.data = data;
        effect.setOptions(data);
    }

    @Override
    public CosmeticType getCosmeticType() {
        return CosmeticType.TRAIL;
    }

    @Override
    public void onSelect(QuakePlayer player, PlayerCosmetics cosmetics) {
        cosmetics.trail = this;
        player.getCurrentGun().setTrail(this);
    }
}
