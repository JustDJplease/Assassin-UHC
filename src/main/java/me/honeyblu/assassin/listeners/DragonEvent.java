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
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;

public class DragonEvent {

    // ------------------------------- //
    // Constructor
    // ------------------------------- //
    private final Game game;

    public DragonEvent(Game game) {
        this.game = game;
    }

    // ------------------------------- //
    // Listener
    // ------------------------------- //
    @EventHandler
    public void onDragonChangePhase(EnderDragonChangePhaseEvent event) {

        // Checking if a game is active.
        if (!game.isGameActive) {
            return;
        }

        // Checking if the dragon has been defeated.
        if (event.getNewPhase() != EnderDragon.Phase.DYING) {
            return;
        }

        // Preventing NullPointerExceptions.
        Validate.notNull(game.target, "Target player cannot be null!");
        Validate.notNull(game.assassin, "Assassin player cannot be null!");

        game.endGame(true);
    }
}
