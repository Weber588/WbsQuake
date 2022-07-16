package wbs.quake.cosmetics.trails;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.LineParticleEffect;

public class ZigZagTrail extends Trail {

    double oscillationsPerBlock = 1;
    double radius = 0.25;

    public ZigZagTrail(ConfigurationSection section, String directory) {
        super(section, directory);

        oscillationsPerBlock = section.getDouble("oscillations-per-block", oscillationsPerBlock);
        radius = section.getDouble("radius", radius);

        effect = new LineParticleEffect()
                .setScaleAmount(true);

        configure(section, directory);

        effect.setAmount(amountPerBlock);
    }

    @Override
    public void playShot(Location pos1, Location pos2, boolean isBounce, QuakePlayer shooter) {
        LineParticleEffect lineEffect = (LineParticleEffect) effect;

        Vector shootVec = pos2.toVector().subtract(pos1.toVector());

        Vector offset = shootVec.getCrossProduct(WbsMath.randomVector())
                .normalize()
                .multiply(radius);

        double distance = pos1.distance(pos2);
        double distancePerOscillation = 1 / oscillationsPerBlock;

        Vector step = shootVec.clone().normalize().multiply(distancePerOscillation);

        double distanceTravelled = 0;
        Location currentPos = pos1.clone().add(offset.clone().multiply(0.5));
        while (distanceTravelled < distance - distancePerOscillation) {
            distanceTravelled += distancePerOscillation;

            // Oscillate back and forth
            offset.multiply(-1);

            Location targetPos = currentPos.clone().add(step).add(offset);

            lineEffect.play(particle, currentPos, targetPos);

            currentPos = targetPos;
        }

        lineEffect.play(particle, currentPos, pos2);
    }
}
