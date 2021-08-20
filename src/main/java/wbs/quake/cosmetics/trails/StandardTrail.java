package wbs.quake.cosmetics.trails;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import wbs.utils.util.particles.LineParticleEffect;

import java.util.List;

public class StandardTrail extends Trail {

    public StandardTrail(String id, Particle particle, Material material, String display, String permission, List<String> description, double price) {
        super(id, particle, material, display, permission, description, price);

        effect = new LineParticleEffect().setScaleAmount(true);
        configureOptions();
    }

    public StandardTrail(ConfigurationSection section, String directory) {
        super(section, directory);

        effect = new LineParticleEffect().setScaleAmount(true);
        configure(section, directory);
    }

    @Override
    public void setAmountPerBlock(int amountPerBlock) {
        super.setAmountPerBlock(amountPerBlock);
        effect.setAmount(amountPerBlock);
    }

    @Override
    public void playShot(Location pos1, Location pos2) {
        ((LineParticleEffect) effect).play(particle, pos1, pos2);
    }
}
