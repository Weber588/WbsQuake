package wbs.quake.cosmetics.trails;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.RingParticleEffect;

public class RingTrail extends Trail {

    private final double startRadius;
    private final double endRadius;
    private final int startAmount;

    public RingTrail(ConfigurationSection section, String directory) {
        super(section, directory);

        startRadius = section.getDouble("start-radius", 0.35);
        endRadius = section.getDouble("end-radius", 0.35);
        startAmount = section.getInt("amount", 7);

        effect = new RingParticleEffect()
                .setRadius(startRadius)
                .setAmount(startAmount);

        configure(section, directory);
    }

    @Override
    public void playShot(Location pos1, Location pos2, boolean isBounce) {
        RingParticleEffect ringEffect = (RingParticleEffect) effect;

        double distance = pos1.distance(pos2);
        int steps = (int) distance * amountPerBlock;
        Vector direction = pos2.toVector()
                .subtract(pos1.toVector())
                .normalize()
                .multiply(distance / steps);

        ringEffect.setAbout(direction);

        double currentRadius = startRadius;
        Location current = pos1.clone();
        for (int i = 0; i < steps; i++) {
            int amount = (int) (startAmount * currentRadius / startRadius);
            ringEffect.setAmount(amount);
            ringEffect.setRadius(currentRadius);
            ringEffect.buildAndPlay(particle, current);

            if (!isBounce) {
                currentRadius = WbsMath.lerp(startRadius, endRadius, (double) i / steps);
            } else {
                currentRadius = WbsMath.lerp(endRadius, startRadius, (double) i / steps);
            }
            current.add(direction);
        }
    }
}
