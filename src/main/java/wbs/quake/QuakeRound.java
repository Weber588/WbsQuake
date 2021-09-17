package wbs.quake;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.quake.player.QuakePlayer;
import wbs.quake.powerups.ArenaPowerUp;
import wbs.quake.powerups.PowerUp;
import wbs.utils.util.WbsScoreboard;
import wbs.utils.util.pluginhooks.VaultWrapper;

import java.util.*;
import java.util.stream.Collectors;

public class QuakeRound {

    private static final String BORDER = "&r===============";

    private final Map<QuakePlayer, Integer> points = new HashMap<>();
    private final Arena arena;
    private final QuakeLobby lobby;

    // Round info
    private int secondsRemaining;

    // Scoreboard
    private final WbsScoreboard scoreboard;
    private final int scoreboardLeaderboardLine = 5;
    private final int timeLeftLine = 2;
    private int scoreboardSize = 8;

    private final WbsQuake plugin;
    private final QuakeSettings settings;
    public QuakeRound(Arena arena) {
        this.arena = arena;
        lobby = QuakeLobby.getInstance();
        plugin = WbsQuake.getInstance();
        settings = plugin.settings;

        scoreboard = new WbsScoreboard(plugin, "WbsQuake", "&c&lQuake");

        scoreboard.addLine(BORDER);
        scoreboard.addLine("");
        scoreboard.addLine("&rTime left: &h");
        scoreboard.addLine("&rMap: &h" + arena.getName());

        // TODO: Find a way to not need this lol
        scoreboard.addLine("&r");

        if (settings.showLeaderboardInGame) {
            scoreboard.addLine("&r1: ");
            scoreboard.addLine("&r2: ");
            if (lobby.getPlayers().size() >= 3) {
                scoreboard.addLine("&r3: ");
                scoreboardSize++;
            }
        }

        scoreboard.addLine("&r&r");

        scoreboard.addLine(BORDER + "&r");
    }

    public void givePoint(QuakePlayer attacker) {
        int current = points.getOrDefault(attacker, 0);

        current++;

        points.put(attacker, current);

        if (current >= arena.getKillsToWin()) {
            roundOver();
        }
    }

    private static final int CHARS_NEEDED_TO_EXTEND = 6;

    private void renderScoreboard() {
        String timeLeftString = "&rTime left: &h" + (secondsRemaining / 60) + ":"
                + String.format("%02d", secondsRemaining % 60);

        scoreboard.setLine(timeLeftLine, timeLeftString);

        if (settings.showLeaderboardInGame) {
            List<Map.Entry<QuakePlayer, Integer>> leaderboard = getLeaderboard();

            int extraWidthNeeded = 0;

            char[] colours = {'a', 'e', 'c'};
            for (int i = 0; i < 3; i++) {
                if (leaderboard.size() > i) {
                    Map.Entry<QuakePlayer, Integer> entry = leaderboard.get(i);
                    String playerName = entry.getKey().getName();
                    scoreboard.setLine(
                            scoreboardLeaderboardLine + i,
                            "&" + colours[i] + entry.getValue()
                                    + " - " + playerName);

                    if (playerName.length() > CHARS_NEEDED_TO_EXTEND) {
                        extraWidthNeeded = playerName.length() - CHARS_NEEDED_TO_EXTEND;
                    }
                }
            }

            StringBuilder extraWidth = new StringBuilder();
            for (int i = 0; i < extraWidthNeeded; i++) {
                extraWidth.append('=');
            }

            scoreboard.setLine(0, BORDER + extraWidth);
            scoreboard.setLine(scoreboardSize, BORDER + extraWidth + "&r");
        }
    }

    /**
     * Start the round immediately
     * @return The runnable ID for the started timer
     */
    public int startRound() {
        secondsRemaining = arena.getSecondsInRound();

        renderScoreboard();
        for (QuakePlayer player : lobby.getPlayers()) {
            scoreboard.showToPlayer(player.getPlayer());
        }

        for (QuakePlayer player : lobby.getPlayers()) {
            points.put(player, 0);

            Inventory inv = player.getPlayer().getInventory();
            inv.clear();
            inv.addItem(player.getCurrentGun().buildGun());
            inv.addItem(new ItemStack(Material.COMPASS));
        }

        arena.start();

        @SuppressWarnings("inline")
        int roundRunnableId = new BukkitRunnable() {
            @Override
            public void run() {
                secondsRemaining--;

                renderScoreboard();

                for (QuakePlayer player : lobby.getPlayers()) {
                    Player bukkitPlayer = player.getPlayer();
                    bukkitPlayer.setSaturation(1);
                    QuakePlayer closest = arena.getClosestPlayer(player);
                    if (closest != null) {
                        bukkitPlayer.setCompassTarget(closest.getPlayer().getLocation());
                    }
                }

                if (secondsRemaining == 0) {
                    roundOver();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20).getTaskId();


        return roundRunnableId;
    }

    public Arena getArena() {
        return arena;
    }

    public void roundOver() {
        for (ArenaPowerUp powerUp : arena.getPowerUps().values()) {
            for (QuakePlayer player : lobby.getPlayers()) {
                powerUp.remove(player);
            }
        }

        List<Map.Entry<QuakePlayer, Integer>> winners = getLeaderboard();

        int winnerPoints = winners.get(0).getValue();

        points.forEach((player, points) -> {
            player.addPlayed();
            if (points == winnerPoints) {
                player.addWin();
            }
        });

        String endMessage =  "&hRound over!&r"
                + "\n==============================="
                + "\n"
                + "\n&a&lWinner - " + winners.get(0).getKey().getName() + " - " + winners.get(0).getValue();

        if (winners.size() >= 2) {
            endMessage += "\n&e&l2nd - " + winners.get(1).getKey().getName() + " - " + winners.get(1).getValue();
        }

        if (winners.size() >= 3) {
            endMessage += "\n&c&l3rd - " + winners.get(2).getKey().getName() + " - " + winners.get(2).getValue();
        }

        endMessage += "\n\n&r===============================";

        scoreboard.clear();

        arena.finish();

        lobby.roundOver(endMessage);
    }

    private List<Map.Entry<QuakePlayer, Integer>> getLeaderboard() {
        List<Map.Entry<QuakePlayer, Integer>> winners = new ArrayList<>(points.entrySet());

        winners.sort(Comparator.comparingInt(Map.Entry::getValue));

        Collections.reverse(winners);

        return winners;
    }

    private List<QuakePlayer> getPlayerLeaderboard() {
        return getLeaderboard().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public void registerKill(QuakePlayer victim, QuakePlayer attacker, boolean headshot) {
        victim.addDeath();
        attacker.addKill();

        victim.getCosmetics().deathSound.play(victim.getPlayer().getLocation());

        for (ArenaPowerUp powerUp : arena.getPowerUps().values()) {
            powerUp.remove(victim);
        }

        double moneyToGive = settings.moneyPerKill;
        if (headshot) moneyToGive += settings.headshotBonus;

        VaultWrapper.giveMoney(attacker.getPlayer(), moneyToGive);

        String format;
        if (headshot) {
            format = settings.headshotFormat;
            attacker.addHeadshot();

            if (settings.headshotsGiveBonusPoints) {
                givePoint(attacker);
            }
        } else {
            format = settings.killFormat;
        }

        format = format.replace("%attacker%", attacker.getName());
        format = format.replace("%victim%", victim.getName());

        lobby.messagePlayersNoPrefix(format);

        givePoint(attacker);
        arena.respawn(victim);

        // Run kill perk after respawning to ensure "closest" target can work
        if (attacker.killPerk != null)
            attacker.killPerk.apply(attacker);
    }
}
