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

package me.honeyblu.assassin.listeners;

import me.honeyblu.assassin.Game;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveEvent implements Listener {

    // ------------------------------- //
    // Constructor
    // ------------------------------- //
    private final Game game;

    public MoveEvent(Game game) {
        this.game = game;
    }

    // ------------------------------- //
    // Listener
    // ------------------------------- //
    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        // Checking if the game is active.
        if (!game.isGameActive) {
            return;
        }

        // Preventing NullPointerExceptions.
        Validate.notNull(game.target, "Target player cannot be null!");
        Validate.notNull(game.assassin, "Assassin player cannot be null!");

        Player assassin = event.getPlayer();

        // Checking if it was the assassin who moved.
        if (game.assassin != assassin) {
            return;
        }

        // Checking if the assassin should be frozen.
        if (!game.isFrozen) {
            return;
        }

        // Checking if the assassin moved from block to block and not just rotated.
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        if ((from.getBlockX() == to.getBlockX()) && (from.getBlockY() == to.getBlockY()) && (from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        // Freezing the assassin.
        event.setCancelled(true);
    }
}
