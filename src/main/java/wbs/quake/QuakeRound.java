package wbs.quake;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.quake.player.QuakePlayer;
import wbs.quake.powerups.ArenaPowerUp;
import wbs.utils.util.WbsScoreboard;
import wbs.utils.util.entities.state.SavedPlayerState;
import wbs.utils.util.entities.state.tracker.GameModeState;
import wbs.utils.util.entities.state.tracker.InventoryState;
import wbs.utils.util.entities.state.tracker.InvulnerableState;
import wbs.utils.util.entities.state.tracker.XPState;

import java.util.*;
import java.util.stream.Collectors;

public class QuakeRound {

    private static final String BORDER = "&r===============";

    private final Map<QuakePlayer, Integer> points = new HashMap<>();
    private final Arena arena;
    private final QuakeLobby lobby;

    private final List<QuakePlayer> initialPlayersInRound = new LinkedList<>();

    // Round info
    private int secondsRemaining;

    // Scoreboard
    private final WbsScoreboard scoreboard;
    private final int scoreboardLeaderboardLine;
    private final int timeLeftLine;

    private final WbsQuake plugin;
    private final QuakeSettings settings;
    public QuakeRound(Arena arena, List<QuakePlayer> playersInRound) {
        this.arena = arena;
        lobby = QuakeLobby.getInstance();
        plugin = WbsQuake.getInstance();
        settings = plugin.settings;

        this.initialPlayersInRound.addAll(playersInRound);

        scoreboard = new WbsScoreboard(plugin, "WbsQuake", "&c&lQuake");

        scoreboard.addLine(BORDER);
        scoreboard.addLine("");
        scoreboard.addLine("&rMap: &h" + arena.getDisplayName());
        scoreboard.addLine("&rPoints to win: &h" + arena.getKillsToWin());
        scoreboard.addLine("&rTime left: &h");
        timeLeftLine = scoreboard.size() - 1;

        String blank = "&r";
        // TODO: Find a way to not need this lol
        scoreboard.addLine(blank);

        if (settings.showLeaderboardInGame) {
            for (int i = 0; i < Math.min(playersInRound.size(), 3); i++) {
                scoreboard.addLine(getBlank(i + 2));
            }
            scoreboardLeaderboardLine = scoreboard.size() - 1;
            scoreboard.addLine(getBlank(playersInRound.size() + 2));
        } else {
            scoreboardLeaderboardLine = 0;
        }

        scoreboard.addLine(BORDER + "&r");

        scoreboard.size();
    }

    private String getBlank(int repeats) {
        StringBuilder blankLine = new StringBuilder("&r");
        for (int repeat = 0; repeat < repeats; repeat++) {
            blankLine.append("&r");
        }
        return blankLine.toString();
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
            List<QuakePlayer> leaderboard = getLeaderboard();

            int extraWidthNeeded = 0;

            char[] colours = {'a', 'e', 'c'};
            for (int i = 0; i < 3; i++) {
                if (leaderboard.size() > i) {
                    QuakePlayer player = leaderboard.get(i);
                    String playerName = player.getName();
                    scoreboard.setLine(
                            scoreboardLeaderboardLine + i,
                            "&" + colours[i] + points.get(player)
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
            scoreboard.setLine(scoreboard.size() - 1, BORDER + extraWidth + "&r");
        }
    }

    // Build a new one every time to read from settings
    private SavedPlayerState getRoundState() {
        SavedPlayerState roundState = new SavedPlayerState();

        ItemStack[] inv = new ItemStack[40];
        inv[ItemManager.getQuakeCompassSlot()] = ItemManager.getQuakeCompass();

        roundState.track(new XPState(0))
                .track(new GameModeState(GameMode.ADVENTURE))
                .track(new InventoryState(inv, ItemManager.getQuakeGunSlot()))
                .track(new InvulnerableState(true))
                .trackAll();

        return roundState;
    }

    /**
     * Start the round immediately
     * @return The runnable ID for the started timer
     */
    public int startRound() {
        secondsRemaining = arena.getSecondsInRound();

        SavedPlayerState roundState = getRoundState();

        renderScoreboard();
        for (QuakePlayer player : initialPlayersInRound) {
            scoreboard.showToPlayer(player.getPlayer());
            points.put(player, 0);

            roundState.restoreState(player.getPlayer());

            PlayerInventory inv = player.getPlayer().getInventory();
            inv.setItem(ItemManager.getQuakeGunSlot(), player.getCurrentGun().buildGun());
        }

        arena.start();

        @SuppressWarnings("inline")
        int roundRunnableId = new BukkitRunnable() {
            @Override
            public void run() {
                secondsRemaining--;

                renderScoreboard();

                for (QuakePlayer player : lobby.getPlayersInRound()) {
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
            for (QuakePlayer player : lobby.getPlayersInRound()) {
                powerUp.remove(player);
                player.getCurrentGun().clearCooldownModifier();
            }
        }

        List<QuakePlayer> winners = getLeaderboard();

        int winnerPoints = points.get(winners.get(0));
        int currentPoints = winnerPoints;
        int currentPlace = 0;

        Multimap<Integer, QuakePlayer> placeMap = HashMultimap.create();
        for (QuakePlayer player : winners) {
            int score = points.get(player);

            // Only add stats if the highest score is above 0
            if (winnerPoints > 0) {
                player.addPlayed();
                if (score == winnerPoints) {
                    player.addWin();
                }
            }

            if (score != currentPoints) {
                currentPlace++;
                currentPoints = score;
            }

            placeMap.put(currentPlace, player);
        }

        StringBuilder endMessage = new StringBuilder("&hRound over!&r"
                + "\n==============================="
                + "\n"
                + "\n");

        final String[] placeDisplay = {"&a&lWinner", "&e&l2nd", "&c&l3rd"};

        for (int i = 0; i < placeDisplay.length; i++) {
            Collection<QuakePlayer> players = placeMap.get(i);

            if (players != null && !players.isEmpty()) {
                QuakePlayer randomPlayer = players.stream().findAny().get();
                String display = placeDisplay[i];

                String playerDisplay = players.stream()
                        .map(QuakePlayer::getName)
                        .collect(Collectors.joining(" & "));

                endMessage.append("\n")
                        .append(display)
                        .append(" - ")
                        .append(playerDisplay)
                        .append(" - ")
                        .append(points.get(randomPlayer));
            }
        }

        endMessage.append("\n\n&r===============================");

        scoreboard.clear();

        arena.finish();

        QuakeDB.getPlayerManager().saveAsync(winners);

        lobby.roundOver(endMessage.toString());
    }

    private List<QuakePlayer> getLeaderboard() {
        List<QuakePlayer> winners = new LinkedList<>(points.keySet());

        winners.sort((a, b) -> Integer.compare(points.get(b), points.get(a)));

        return winners;
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

        EconomyUtil.giveMoney(attacker, moneyToGive);

        String format;
        if (headshot) {
            format = attacker.getCosmetics().killMessage.formatHeadshot(attacker, victim);
            attacker.addHeadshot();

            if (settings.headshotsGiveBonusPoints) {
                givePoint(attacker);
            }
        } else {
            format = attacker.getCosmetics().killMessage.format(attacker, victim);
        }

        lobby.getPlayersInRound().forEach(player -> plugin.sendMessageNoPrefix(format, player.getPlayer()));

        givePoint(attacker);
        arena.respawn(victim);

        // Run kill perk after respawning to ensure "closest" target can work
        if (attacker.killPerk != null)
            attacker.killPerk.apply(attacker);
    }
}
