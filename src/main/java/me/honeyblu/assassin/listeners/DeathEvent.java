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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {

    // ------------------------------- //
    // Constructor
    // ------------------------------- //
    private final Game game;

    public DeathEvent(Game game) {
        this.game = game;
    }

    // ------------------------------- //
    // Listener
    // ------------------------------- //
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        // Checking if a game is active.
        if (!game.isGameActive) {
            return;
        }

        // Preventing NullPointerExceptions.
        Validate.notNull(game.target, "Target player cannot be null!");
        Validate.notNull(game.assassin, "Assassin player cannot be null!");

        Player succumbedPlayer = event.getEntity();

        // Checking if it was the assassin who perished.
        if (succumbedPlayer == game.assassin) {
            event.setKeepInventory(true);
            return;
        }

        if (succumbedPlayer != game.target) {
            return;
        }

        // Ending the game if it had not already ended.
        game.endGame();
    }
}
