package wbs.quake.cosmetics.trails;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.LineParticleEffect;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class StandardTrail extends Trail {

    private final List<Vector> offsets = new LinkedList<>();

    public StandardTrail(String id, Particle particle, Material material, String display, String permission, List<String> description, double price) {
        super(id, particle, material, display, permission, description, price);

        offsets.add(new Vector(0, 0, 0));

        effect = new LineParticleEffect().setScaleAmount(true);
        configureOptions();
    }

    public StandardTrail(ConfigurationSection section, String directory) {
        super(section, directory);

        List<String> offsetStrings = section.getStringList("offsets");
        for (String offsetString : offsetStrings) {
            String[] args = offsetString.replaceAll("\\s", "").split(",");

            if (args.length != 2) {
                settings.logError("Invalid offset string: " + offsetString + ". Must be in the form \"x,y\"", directory + "/offsets");
                continue;
            }

            double x, y;
            try {
                x = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                settings.logError("Invalid offset string: " + offsetString + ". x must be a decimal number.", directory + "/offsets");
                continue;
            }
            try {
                y = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                settings.logError("Invalid offset string: " + offsetString + ". y must be a decimal number.", directory + "/offsets");
                continue;
            }

            offsets.add(new Vector(x, y, 0));
        }

        if (offsets.isEmpty()) {
            offsets.add(new Vector(0, 0, 0));
        }

        effect = new LineParticleEffect().setScaleAmount(true);
        configure(section, directory);
    }

    @Override
    public void setAmountPerBlock(int amountPerBlock) {
        super.setAmountPerBlock(amountPerBlock);
        effect.setAmount(amountPerBlock);
    }

    private static final Vector UP_VECTOR = new Vector(0, 1, 0);

    @Override
    public void playShot(Location pos1, Location pos2, boolean isBounce) {
        Vector direction = pos2.toVector().subtract(pos1.toVector());
        Vector perp = direction.getCrossProduct(UP_VECTOR);
        Vector offsetDir = perp.getCrossProduct(direction);
        for (Vector offset : offsets) {
            Vector thisPerp = perp.clone().normalize().multiply(offset.getX());
            Vector thisOffsetDir = offsetDir.clone().normalize().multiply(offset.getY());

            Location thisPos1 = pos1.clone().add(thisPerp).add(thisOffsetDir);
            Location thisPos2 = pos2.clone().add(thisPerp).add(thisOffsetDir);

            ((LineParticleEffect) effect).play(particle, thisPos1, thisPos2);
        }
    }
}
