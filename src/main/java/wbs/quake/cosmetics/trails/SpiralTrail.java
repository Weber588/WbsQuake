package wbs.quake.cosmetics.trails;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.particles.RingParticleEffect;

public class SpiralTrail extends Trail {

    private double rotationsPerBlock = 2;

    public SpiralTrail(ConfigurationSection section, String directory) {
        super(section, directory);

        rotationsPerBlock = section.getDouble("rotations-per-block", rotationsPerBlock);
        double radius = section.getDouble("radius", 0.35);
        int amount = section.getInt("amount", 1);

        effect = new RingParticleEffect()
                .setRadius(radius)
                .setAmount(amount);
        configure(section, directory);
    }

    @Override
    public void playShot(Location pos1, Location pos2, boolean isBounce, QuakePlayer shooter) {
        RingParticleEffect ringEffect = (RingParticleEffect) effect;

        double distance = pos1.distance(pos2);
        int steps = (int) distance * amountPerBlock;
        Vector direction = pos2.toVector()
                .subtract(pos1.toVector())
                .normalize()
                .multiply(distance / steps);

        double rotationsPerStep = rotationsPerBlock / amountPerBlock;
        double rotationPerStep = 360 * rotationsPerStep;
        ringEffect.setAbout(direction);

        double currentRotation = Math.random() * 360;
        Location current = pos1.clone();
        for (int i = 0; i < steps; i++) {
            ringEffect.buildAndPlay(particle, current);
            ringEffect.setRotation(currentRotation);
            currentRotation += rotationPerStep;

         //   plugin.logger.info("currentRotation = " + currentRotation);

            current.add(direction);
        }
    }
}
