/*
 * AssassinUHC - Copyright (c) 2020 - All Rights Reserved.
 *
 * You are allowed to:
 * - Modify this code, and use it for personal projects. (Private servers, small networks)
 * - Take ideas and / or formats of this plugin and use it for personal projects. (Private servers, small networks)
 *
 * You are NOT allowed to:
 * - Resell the original plugin or a modification of it.
 * - Claim this plugin as your own.
 * - Distribute the source-code or a modification of it without prior consent of the original author.
 */

package me.honeyblu.assassin;

import me.honeyblu.assassin.commands.StartCommand;
import me.honeyblu.assassin.listeners.*;
import me.honeyblu.assassin.tasks.LineOfSightTask;
import me.honeyblu.assassin.util.WorldBorderUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class Game extends JavaPlugin {

    // ------------------------------- //
    // Public variables
    // ------------------------------- //
    public WorldBorderUtil worldBorderUtil;

    public Player assassin;
    public Player target;

    public boolean isFrozen = false;
    public boolean isGameActive = false;

    // ------------------------------- //
    // Private variables
    // ------------------------------- //
    private Team team;
    private long timeStartGame = 0L;

    // ------------------------------- //
    // onEnable method
    // ------------------------------- //
    @Override
    public void onEnable() {

        // Registering world border utility (and its dependency).
        worldBorderUtil = new WorldBorderUtil(this);

        // Registering damage event.
        PluginManager pluginManager = Bukkit.getPluginManager();
        DamageEvent damageEvent = new DamageEvent(this);
        pluginManager.registerEvents(damageEvent, this);

        // Registering move event.
        MoveEvent moveEvent = new MoveEvent(this);
        pluginManager.registerEvents(moveEvent, this);

        // Registering compass event.
        CompassEvent compassEvent = new CompassEvent(this);
        pluginManager.registerEvents(compassEvent, this);

        // Registering death event.
        DeathEvent deathEvent = new DeathEvent(this);
        pluginManager.registerEvents(deathEvent, this);

        // Registering dragon event.
        DragonEvent dragonEvent = new DragonEvent(this);
        pluginManager.registerEvents(dragonEvent, this);

        // Registering start command.
        StartCommand startCommand = new StartCommand(this);
        PluginCommand internalStartCommand = getCommand("start");
        Validate.notNull(internalStartCommand, "The internal start command cannot be null!");
        internalStartCommand.setExecutor(startCommand);

        // Registers the repeating task that freezes the assassin.
        BukkitScheduler scheduler = getServer().getScheduler();
        LineOfSightTask task = new LineOfSightTask(this);
        scheduler.runTaskTimer(this, task, 0L, 0L);

        // Registering teams if they do not already exist.
        ScoreboardManager scoreboardManager = getServer().getScoreboardManager();
        Validate.notNull(scoreboardManager, "The scoreboard manager cannot be null!");
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

        if (scoreboard.getTeam("_do_not_edit_") == null) {
            scoreboard.registerNewTeam("_do_not_edit_");
        }

        team = scoreboard.getTeam("_do_not_edit_");
        Validate.notNull(team, "The team cannot be null!");

        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.allowFriendlyFire();
    }

    // ------------------------------- //
    // onDisable method
    // ------------------------------- //
    @Override
    public void onDisable() {
        endGame(false);
    }

    // ------------------------------- //
    // Public methods
    // ------------------------------- //
    public void startGame(Player assassin, Player target) {

        // Announcing the game.
        Bukkit.broadcastMessage("§a" + assassin.getName() + "§2 has started hunting §a" + target.getName() + "§2.");
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100f, 0.1f));

        // Updating public variables.
        isGameActive = true;
        timeStartGame = System.currentTimeMillis();
        this.assassin = assassin;
        this.target = target;

        // Preparing the players.
        team.addEntry(assassin.getName());
        assassin.getInventory().clear();
        assassin.setExp(0);

        team.addEntry(target.getName());
        target.getInventory().clear();
        target.setExp(0);

        // Giving the assassin a compass.
        ItemStack compass = new ItemStack(Material.COMPASS, 1);

        assassin.getInventory().setItem(0, compass);
        assassin.sendMessage("§2You have received a compass.");

        // Blinding the assassin.
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 200, 0);
        assassin.addPotionEffect(blindness);
    }

    public void endGame(boolean wonByTarget) {

        // Checking if a game is active.
        if (isGameActive) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100f, 0.1f));
            if (wonByTarget) {
                Bukkit.broadcastMessage("§2The hunt has ended! The§a dragon§2 was defeated!");
            } else {
                if (target == null) {
                    Bukkit.broadcastMessage("§2The hunt has ended! The§a target§2 was slain!");
                } else {
                    Bukkit.broadcastMessage("§2The hunt has ended! §a" + target.getName() + "§2 was slain!");
                }
            }
            Bukkit.broadcastMessage("§aThe game has lasted " + getReadableTimeLasted() + ".");
        }

        // Resetting the players.
        if (assassin != null) {
            team.removeEntry(assassin.getName());
        }

        if (target != null) {
            team.removeEntry(target.getName());
        }

        worldBorderUtil.enlarge();

        // Clearing public variables.
        assassin = null;
        target = null;
        timeStartGame = 0L;
        isFrozen = false;
        isGameActive = false;

        // Clearing the teams.
        ScoreboardManager scoreboardManager = getServer().getScoreboardManager();
        assert scoreboardManager != null;
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

        if (scoreboard.getTeam("_do_not_edit_") != null) {
            team.unregister();
        }
    }

    private String getReadableTimeLasted() {
        long milliseconds = System.currentTimeMillis() - timeStartGame;

        // If the time somehow lasted a negative amount.
        if (milliseconds < 0) {
            return "forever";
        }

        // Calculate the time.
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        // Display the time in a readable format.
        if (hours < 1) {
            return minutes + " Minutes and " + seconds + " Seconds";
        } else {
            return hours + " Hour, " + minutes + " Minutes and " + seconds + " Seconds";
        }
    }
}
