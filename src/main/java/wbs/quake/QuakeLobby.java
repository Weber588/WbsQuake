package wbs.quake;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsScoreboard;
import wbs.utils.util.plugin.WbsMessenger;

import java.util.*;

public class QuakeLobby extends WbsMessenger {

    // Constants
    private static final int VOTING_DURATION = 30;
    private static final int VOTING_DELAY = 5;
    private static final int COUNTDOWN_DURATION = 5;
    private static final int END_DURATION = 5;

    private static final int PLAYERS_TO_START = 2;

    // Singleton
    private static QuakeLobby instance;
    public static QuakeLobby getInstance() {
        if (instance == null) instance = new QuakeLobby(WbsQuake.getInstance());
        return instance;
    }

    public static void reload() {
        getInstance().kickAll();

        instance = null;
    }

    private QuakeLobby(WbsQuake plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    private final WbsQuake plugin;
    private Location lobbySpawn = null;

    private final List<QuakePlayer> players = new LinkedList<>();

    // Misc

    public List<QuakePlayer> getPlayers() {
        return players;
    }

    private final ItemStack[] lobbyInventory = {
            new ItemStack(Material.CLOCK)
    };

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public void teleportToLobby(QuakePlayer player) {
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }
    }

    // Join/leave listeners to trigger the start of voting, or revert to prior state on leave

    private final Map<QuakePlayer, WbsScoreboard> scoreboards = new HashMap<>();

    /**
     * Make a player join this arena.
     * @param player The player to join
     * @return True if the player joined, false otherwise
     */
    public boolean join(QuakePlayer player) {
        if (players.contains(player)) return false;
        Player bukkitPlayer = player.getPlayer();

        savePlayerState(player);
        teleportToLobby(player);
        bukkitPlayer.setInvulnerable(true);
        bukkitPlayer.getInventory().setContents(lobbyInventory);

        messagePlayers("&h" + player.getName() + "&r joined the lobby! &h(" + (players.size() + 1) + ")");

        players.add(player);
        WbsScoreboard scoreboard = getScoreboardFor(player);
        scoreboard.showToPlayer(player.getPlayer());
        scoreboards.put(player, scoreboard);

        sendMessage("Joined the lobby!", player.getPlayer());

        if (players.size() >= PLAYERS_TO_START) {
            if (state == GameState.WAITING_FOR_PLAYERS) {
                startVotingLater();
            }
        }

        return true;
    }

    /**
     * Make the given player leave the arena.
     * @param player The player to leave
     * @return True if the player was in the lobby and was removed,
     * false if the player was not in the lobby (has no effect)
     */
    public boolean leave(QuakePlayer player) {
        if (!players.contains(player)) return false;

        player.getCurrentGun().clearCooldownModifier();

        plugin.sendMessage("Left the lobby!", player.getPlayer());
        players.remove(player);
        scoreboards.get(player).clear();

        messagePlayers(player.getPlayer().getName() + "&r left. &w(" + players.size() + ")");

        restorePlayerState(player);
        player.getPlayer().setInvulnerable(false);

        if (players.size() < PLAYERS_TO_START) {
            switch (state) {
                case WAITING_FOR_PLAYERS:
                    // Do nothing; already here
                    break;
                case PAUSED_BEFORE_VOTING:
                case VOTING:
                    cancelVoting();
                    break;
                case COUNTDOWN:
                    cancelCountdown();
                    break;
                case GAMEPLAY:
                    assert round != null;
                    round.roundOver();
                    break;
                case ROUND_OVER:
                    break;
            }

            state = GameState.WAITING_FOR_PLAYERS;
        }

        return true;
    }

    private static final String BORDER = "&r===============";

    private WbsScoreboard getScoreboardFor(QuakePlayer player) {
        String namespace = player.getPlayer().getUniqueId().toString();
        namespace = namespace.substring(namespace.length() - 16);

        WbsScoreboard playersScoreboard = new WbsScoreboard(plugin, namespace, "&c&lQuake");

        configureScoreboard(playersScoreboard, player);

        return playersScoreboard;
    }

    private WbsScoreboard configureScoreboard(WbsScoreboard scoreboard, QuakePlayer player) {
        scoreboard.addLine(BORDER);
        scoreboard.addLine("");
        for (String line : player.getStatsDisplay()) {
            scoreboard.addLine("&r" + line);
        }
        scoreboard.addLine("&r");
        scoreboard.addLine(BORDER + "&r");

        return scoreboard;
    }

    public void kickAll() {
        waitForPlayers(false);

        List<QuakePlayer> copiedPlayers = new LinkedList<>(players);
        for (QuakePlayer player : copiedPlayers) {
            leave(player);
        }
    }

    @NotNull
    public QuakeRound getCurrentRound() {
        if (round == null) {
            throw new IllegalStateException("Round queried outside of gameplay");
        }
        return round;
    }

    private void registerRunnable(int id) {
        if (runnableId != -1) {
            throw new IllegalStateException("Runnable was scheduled before previous was cancelled!");
        }

        runnableId = id;
    }

    public void forceStart() {
        switch (state) {
            case WAITING_FOR_PLAYERS:
            case PAUSED_BEFORE_VOTING:
                cancelRunnable();
                startVoting();
                break;
            case VOTING:
                cancelRunnable();

                Arena chosenArena = getChosenArena();
                messagePlayers("&h" + chosenArena.getName() + "&r won!");
                startCountdown(chosenArena);
                break;
        }
    }

    public GameState getState() {
        return state;
    }

    // ============================= //
    //          State Machine        //
    // ============================= //

    public enum GameState {
        WAITING_FOR_PLAYERS, PAUSED_BEFORE_VOTING, VOTING, COUNTDOWN, GAMEPLAY, ROUND_OVER
    }

    private GameState state = GameState.WAITING_FOR_PLAYERS;
    private int runnableId = -1;

    private void cancelRunnable() {
        if (runnableId == -1) return;

        Bukkit.getScheduler().cancelTask(runnableId);
        runnableId = -1;
    }

    // Voting

    // The IDs for each arena available in the given round
    private final Map<Integer, Arena> allowedVotes = new HashMap<>();
    // The players who have voted, and the ID they voted for
    private final Map<QuakePlayer, Integer> voted = new HashMap<>();

    /**
     * Add a player's vote for a given arena, by ID shown in chat from voting
     * @param player The player voting
     * @param voteNumber The ID of the arena they voted for
     * @return The chosen arena, null otherwise
     */
    public Arena playerVote(QuakePlayer player, int voteNumber) {
        Arena votedArena = allowedVotes.get(voteNumber);
        if (votedArena == null) {
            return null;
        }

        voted.put(player, voteNumber);

        return votedArena;
    }

    private final Random random = new Random();
    @NotNull
    private Arena getChosenArena() {
        if (allowedVotes.size() == 0) {
            plugin.logger.severe("getChosenArena was called when allowedVotes was empty");
            throw new IllegalStateException("No arenas configured.");
        }

        Map<Arena, Integer> votes = new HashMap<>();
        for (QuakePlayer voter : voted.keySet()) {
            int votedFor = voted.get(voter);

            Arena chosenArena = allowedVotes.get(votedFor);
            votes.put(chosenArena, votes.getOrDefault(chosenArena, 0) + 1);
        }

        int mostVotes = 0;
        List<Arena> topVotes = new ArrayList<>();
        for (Arena votedFor : votes.keySet()) {
            int votesForArena = votes.get(votedFor);

            if (votesForArena > mostVotes) {
                mostVotes = votesForArena;
                topVotes.clear();
                topVotes.add(votedFor);
            } else if (votesForArena == mostVotes) {
                topVotes.add(votedFor);
            }
        }

        int totalVotes = topVotes.size();

        Arena chosen;
        int index;
        if (totalVotes > 0) {
            index = random.nextInt(totalVotes);
            chosen = topVotes.get(index);
        } else {
            index = random.nextInt(allowedVotes.size());
            chosen = allowedVotes.get(index + 1);
        }

        if (chosen == null) {
            plugin.logger.warning("Chosen was null. Please report this error. " +
                    "(chosen=null;index=" + index + ";totalVotes=" + totalVotes + ")");
            throw new IllegalStateException("Chosen arena is only valid during voting.");
        }

        allowedVotes.clear();
        voted.clear();

        return chosen;
    }

    private void sendArenaPrompt(Arena arena, int id) {
        String hoverString = "&rClick to vote for &h" + arena.getName() + "&r!";

        Text hoverText = new Text(plugin.dynamicColourise(hoverString));

        String prompt = plugin.dynamicColourise("&h" + id + ") &r" + arena.getName());

        TextComponent fullMessage = new TextComponent(prompt);
        fullMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

        fullMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wbsquake vote " + id));

        for (QuakePlayer player : players) {
            player.getPlayer().spigot().sendMessage(fullMessage);
        }
    }

    private void startVotingLater() {
        state = GameState.PAUSED_BEFORE_VOTING;

        registerRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                runnableId = -1;
                startVoting();
            }
        }.runTaskLater(plugin, VOTING_DELAY * 20).getTaskId());
    }

    private void startVoting() {
        state = GameState.VOTING;

        messagePlayers("Time to vote! Click or use &h/q vote [id]&r to vote!");

        // TODO: Pick arenas based on amount of players in lobby
        // For now, just showing all arenas
        int arenaID = 0;
        for (Arena arena : ArenaManager.getAllArenas()) {
            arenaID++;
            allowedVotes.put(arenaID, arena);

            sendArenaPrompt(arena, arenaID);
        }
        // TODO: Make arenas votable via command and by clicking in chat

        registerRunnable(new BukkitRunnable() {
            int timeLeft = VOTING_DURATION;

            @Override
            public void run() {
                if (timeLeft == 0) {
                    runnableId = -1;
                    cancel();

                    Arena chosenArena = getChosenArena();
                    messagePlayers("&h" + chosenArena.getName() + "&r won!");
                    startCountdown(chosenArena);
                    return;
                }

                if (timeLeft % 10 == 0) {
                    messagePlayers("&h" + timeLeft + " &rseconds left to vote!");
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 20, 20).getTaskId());
    }

    private void cancelVoting() {
        cancelRunnable();
        messagePlayers("Not enough players! Voting cancelled.");
        state = GameState.WAITING_FOR_PLAYERS;
    }

    // Countdown
    @Nullable
    private QuakeRound round;

    private void startCountdown(Arena chosenArena) {
        state = GameState.COUNTDOWN;

        round = new QuakeRound(chosenArena);
        for (QuakePlayer player : players) {
            chosenArena.respawn(player);
        }

        registerRunnable(new BukkitRunnable() {
            int timeLeft = COUNTDOWN_DURATION;

            @Override
            public void run() {
                if (timeLeft == 0) {
                    cancel();
                    runnableId = -1; 

                    startRound(round);
                    return;
                }

                if ((timeLeft % 5 == 0) || timeLeft <= 5) {
                    messagePlayers("Starting in &h" + timeLeft + "...");
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 20, 20).getTaskId());
    }

    private void cancelCountdown() {
        cancelRunnable();

        messagePlayers("Not enough players! Returning to lobby.");
        waitForPlayers();
        round = null;
    }

    // Gameplay

    private void startRound(@NotNull QuakeRound round) {
        state = GameState.GAMEPLAY;

        registerRunnable(round.startRound());
    }

    // End of gameplay

    public void roundOver(String endMessage) {
        cancelRunnable();
        state = GameState.ROUND_OVER;
        round = null;
        messagePlayers(endMessage);
        registerRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                runnableId = -1;
                waitForPlayers();
            }
        }.runTaskLater(plugin, END_DURATION * 20).getTaskId());
    }

    // Return to waiting
    private void waitForPlayers() {
        waitForPlayers(true);
    }

    private void waitForPlayers(boolean autoStart) {
        state = GameState.WAITING_FOR_PLAYERS;

        for (QuakePlayer lobbyPlayer : players) {
            teleportToLobby(lobbyPlayer);
            lobbyPlayer.getPlayer().getInventory().setContents(lobbyInventory);
            WbsScoreboard scoreboard = scoreboards.get(lobbyPlayer);
            scoreboard.clear();
            configureScoreboard(scoreboard, lobbyPlayer);
            scoreboard.showToPlayer(lobbyPlayer.getPlayer());
        }

        if (autoStart && players.size() >= PLAYERS_TO_START) {
            startVotingLater();
        }
    }

    // ======================================== //
    //          Messages & player states        //
    // ======================================== //


    private final Map<QuakePlayer, ItemStack[]> inventories = new HashMap<>();
    private final Map<QuakePlayer, Location> oldLocations = new HashMap<>();
    private final Map<QuakePlayer, GameMode> gamemodes = new HashMap<>();
    private final Map<QuakePlayer, Scoreboard> vanillaScoreboards = new HashMap<>();

    private void savePlayerState(QuakePlayer player) {
        Player bukkitPlayer = player.getPlayer();

        inventories.put(player, bukkitPlayer.getInventory().getStorageContents());
        bukkitPlayer.getInventory().clear();

        gamemodes.put(player, bukkitPlayer.getGameMode());
        bukkitPlayer.setGameMode(GameMode.ADVENTURE);

        oldLocations.put(player, bukkitPlayer.getLocation());

        vanillaScoreboards.put(player, player.getPlayer().getScoreboard());
    }

    private void restorePlayerState(QuakePlayer player) {
        Player bukkitPlayer = player.getPlayer();

        bukkitPlayer.getInventory().clear();
        bukkitPlayer.getInventory().setContents(inventories.get(player));

        bukkitPlayer.setGameMode(gamemodes.get(player));

        bukkitPlayer.teleport(oldLocations.get(player));

        player.getPlayer().setScoreboard(vanillaScoreboards.get(player));
    }

    public void messagePlayers(String message) {
        for (QuakePlayer player : players) {
            sendMessage(message, player.getPlayer());
        }
    }

    public void messagePlayersNoPrefix(String message) {
        for (QuakePlayer player : players) {
            sendMessageNoPrefix(message, player.getPlayer());
        }
    }

    public void sendActionBars(String message) {
        for (QuakePlayer player : players) {
            sendActionBar(message, player.getPlayer());
        }
    }
}
