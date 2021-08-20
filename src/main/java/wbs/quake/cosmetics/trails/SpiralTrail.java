package wbs.quake.cosmetics.trails;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import wbs.utils.util.particles.RingParticleEffect;

import java.util.Collections;

public class SpiralTrail extends Trail {

    private double rotationsPerBlock = 2;
    private double radius = 0.35;
    private int amount = 1;

    public SpiralTrail(Particle particle) {
        super("default",
                particle,
                Material.STICK,
                "&c&lDefault",
                "",
                Collections.singletonList("The default trail."),
                0);

        effect = new RingParticleEffect();
        configureOptions();
    }

    public SpiralTrail(ConfigurationSection section, String directory) {
        super(section, directory);

        rotationsPerBlock = section.getDouble("rotations-per-block", rotationsPerBlock);
        radius = section.getDouble("radius", radius);
        amount = section.getInt("amount", amount);

        effect = new RingParticleEffect()
                .setRadius(radius)
                .setAmount(amount);
        configure(section, directory);
    }

    @Override
    public void playShot(Location pos1, Location pos2) {
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

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }


    public double getRotationsPerBlock() {
        return rotationsPerBlock;
    }
    public void setRotationsPerBlock(int rotationsPerBlock) {
        this.rotationsPerBlock = rotationsPerBlock;
    }

    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
        effect.setAmount(amount);
    }
}
