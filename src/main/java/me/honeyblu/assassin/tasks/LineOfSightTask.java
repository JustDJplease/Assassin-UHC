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

package me.honeyblu.assassin.tasks;

import me.honeyblu.assassin.Game;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Objects;

public class LineOfSightTask implements Runnable {

    // ------------------------------- //
    // Constructor & Private variables
    // ------------------------------- //
    private final Game game;
    private int heartbeat = 20;
    private boolean isWorldBorderWarningActive = false;
    private boolean shouldBeDifferentPitch = true;

    public LineOfSightTask(Game game) {
        this.game = game;
    }

    // ------------------------------- //
    // Runnable
    // ------------------------------- //
    @Override
    public void run() {

        // Resetting the frozen status.
        game.isFrozen = false;

        // Checking if a game is active.
        if (!game.isGameActive) {
            return;
        }

        // Preventing NullPointerExceptions.
        if (game.assassin == null || game.target == null) {
            game.logger.severe("Player cannot be null.");
            return;
        }

        Location targetLocation = game.target.getLocation();
        Location assassinLocation = game.assassin.getLocation();

        // Managing the heartbeat effect
        heartbeat = heartbeat - 1;
        if (heartbeat < 0) {
            heartbeat = 20;

            World targetIsInWorld = targetLocation.getWorld();
            World assassinIsInWorld = assassinLocation.getWorld();

            if ((targetIsInWorld == assassinIsInWorld) && targetLocation.distanceSquared(assassinLocation) < 1000) {
                game.target.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§7* heart beating *"));

                // Displaying a the world border glowing effect.
                if (!isWorldBorderWarningActive) {
                    game.worldBorderUtil.shrink();
                    isWorldBorderWarningActive = true;
                }
            } else {
                if (isWorldBorderWarningActive) {
                    game.worldBorderUtil.enlarge();
                    isWorldBorderWarningActive = false;
                }
            }
        }

        // Checking if the target is looking at the assassin.
        if (!isTargetLookingAtPlayer(game.target, game.assassin)) {
            return;
        }

        // Checking if the target is not looking through blocks.
        if (!canActuallySeeAssassin(game.target, game.assassin)) {
            return;
        }

        // Assassin should be frozen.
        drawParticles();

        // Letting the assassin know.
        game.assassin.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§4You are frozen"));

        // Blinding the assassin.
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 20, 0);
        // Commented out since we're now forcing this effect instead of removing and then re-adding it.
        // game.assassin.removePotionEffect(PotionEffectType.BLINDNESS);
        game.assassin.addPotionEffect(blindness, true);

        // Playing the 'line of sight' sound-effect.
        if (shouldBeDifferentPitch) {
            game.assassin.playSound(assassinLocation, Sound.BLOCK_NOTE_BLOCK_BASS, 0.4f, 0.6f);
            game.target.playSound(targetLocation, Sound.BLOCK_NOTE_BLOCK_BASS, 0.4f, 0.6f);
            shouldBeDifferentPitch = false;
        } else {
            game.assassin.playSound(assassinLocation, Sound.BLOCK_NOTE_BLOCK_BASS, 0.4f, 0.7f);
            game.target.playSound(targetLocation, Sound.BLOCK_NOTE_BLOCK_BASS, 0.4f, 0.7f);
            shouldBeDifferentPitch = true;
        }

        // Freezing the assassin upon moving.
        game.isFrozen = true;
    }

    // ------------------------------- //
    // Private methods
    // ------------------------------- //
    private boolean isTargetLookingAtPlayer(Player target, Player assassin) {
        World targetIsInWorld = target.getLocation().getWorld();
        World assassinIsInWorld = assassin.getLocation().getWorld();

        if (targetIsInWorld != assassinIsInWorld) {
            return false;
        }

        // Magic code stolen from the internet to calculate if the target is aiming at the assassin.
        Location targetEyeLocation = target.getEyeLocation();
        Vector toAssassin = assassin.getEyeLocation().toVector().subtract(targetEyeLocation.toVector());
        double dot = toAssassin.normalize().dot(targetEyeLocation.getDirection());

        if (targetEyeLocation.distanceSquared(assassin.getLocation()) < 9) {
            return dot > 0.96D;
        }

        if (targetEyeLocation.distanceSquared(assassin.getLocation()) < 25) {
            return dot > 0.975D;
        }

        return dot > 0.99D;
    }

    private boolean canActuallySeeAssassin(Player target, Player assassin) {
        // Checking if the target is not looking through solid blocks.
        return target.hasLineOfSight(assassin);
    }

    private void drawParticles() {
        Location from = game.target.getEyeLocation().clone().add(0, -0.3, 0);
        Location to = game.assassin.getEyeLocation().clone().add(0, -0.3, 0);

        drawLine(from, to);
    }

    private void drawLine(Location start, Location finish) {
        // Magic code stolen from the internet to draw a line of particles.
        double space = 0.3;

        // Validating if both locations are in the same world.
        World world = start.getWorld();
        Validate.notNull(world, "World cannot be null!");
        Validate.isTrue(Objects.equals(finish.getWorld(), world), "Lines cannot be in different worlds!");

        // Calculating the distance between the points.
        double distance = start.distance(finish);

        // Getting the points as vectors.
        Vector startVector = start.toVector();
        Vector finishVector = finish.toVector();

        // Subtract gives you a vector between the points, which we multiply by the space
        Vector vector = finishVector.clone().subtract(startVector).normalize().multiply(space);

        // The distance we covered so far.
        double covered = 0;

        // For aesthetic effect, every other particle gets a different colour.
        boolean shouldBeDifferentColour = false;

        // Looping until we reach the distance.
        for (; covered < distance; startVector.add(vector)) {
            Particle.DustOptions dustOptions;

            if (shouldBeDifferentColour) {
                // Green-ish
                dustOptions = new Particle.DustOptions(Color.fromRGB(50, 191, 90), 1);
                shouldBeDifferentColour = false;
            } else {
                // Blue-ish
                dustOptions = new Particle.DustOptions(Color.fromRGB(50, 191, 186), 1);
                shouldBeDifferentColour = true;
            }

            world.spawnParticle(Particle.REDSTONE, startVector.getX(), startVector.getY(), startVector.getZ(), 1, 0.1, 0.1, 0.1, 0.1, dustOptions);
            covered += space;
        }
    }
}
