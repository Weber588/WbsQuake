package wbs.quake;

import wbs.quake.player.QuakePlayer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PlayerTargeter {

    public enum TargetType {
        PLAYER, NOT_PLAYER, ALL, RANDOM, RANDOM_NOT_PLAYER, CLOSEST
    }

    public static Collection<QuakePlayer> getTargets(QuakePlayer player, TargetType type) {
        List<QuakePlayer> targets = new LinkedList<>();

        List<QuakePlayer> playersInArena = QuakeLobby.getInstance().getPlayers();
        switch (type) {
            case PLAYER:
                targets.add(player);
                break;
            case NOT_PLAYER:
                for (QuakePlayer otherPlayer : playersInArena) {
                    if (!otherPlayer.equals(player)) {
                        targets.add(otherPlayer);
                    }
                }
                break;
            case ALL:
                targets.addAll(playersInArena);
                break;
            case RANDOM:
                targets.add(
                        playersInArena.get(new Random().nextInt(playersInArena.size()))
                );
                break;
            case RANDOM_NOT_PLAYER:
                playersInArena.remove(player);
                targets.add(
                        playersInArena.get(new Random().nextInt(playersInArena.size()))
                );
                break;
            case CLOSEST:
                double closestDistance = Double.MAX_VALUE;
                QuakePlayer closest = null;
                for (QuakePlayer otherPlayer : playersInArena) {
                    if (otherPlayer.equals(player)) continue;
                    double distanceSquared = otherPlayer.getPlayer().getLocation().distanceSquared(player.getPlayer().getLocation());
                    if (distanceSquared < closestDistance) {
                        closest = otherPlayer;
                        closestDistance = distanceSquared;
                    }
                }

                if (closest == null) {
                    return targets;
                }

                targets.add(closest);
                break;
        }

        return targets;
    }

}
