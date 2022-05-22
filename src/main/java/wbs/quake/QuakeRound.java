package wbs.quake;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.quake.player.PlayerManager;
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
    private final int scoreboardLeaderboardLine = 5;
    private final int timeLeftLine = 2;
    private int scoreboardSize = 8;

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

        // TODO: Find a way to not need this lol
        scoreboard.addLine("&r");

        if (settings.showLeaderboardInGame) {
            scoreboard.addLine("&r1: ");
            scoreboard.addLine("&r2: ");
            if (playersInRound.size() >= 3) {
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

        QuakeDB.getPlayerManager().saveAsync(initialPlayersInRound);

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
