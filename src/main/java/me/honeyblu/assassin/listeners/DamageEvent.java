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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageEvent implements Listener {

    // ------------------------------- //
    // Constructor
    // ------------------------------- //
    private final Game game;

    public DamageEvent(Game game) {
        this.game = game;
    }

    // ------------------------------- //
    // Listener
    // ------------------------------- //
    @EventHandler
    public void onAssassinHitPlayer(EntityDamageByEntityEvent event) {

        // Checking if a game is active.
        if (!game.isGameActive) {
            return;
        }

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        // Checking if the entities are both players.
        if (!(damager instanceof Player)) {
            return;
        }

        if (!(entity instanceof Player)) {
            return;
        }

        // Preventing NullPointerExceptions.
        Validate.notNull(game.target, "Target player cannot be null!");
        Validate.notNull(game.assassin, "Assassin player cannot be null!");

        Player assassin = (Player) damager;
        Player target = (Player) entity;

        // Checking if the assassin damages the target.
        if (assassin != game.assassin) {
            return;
        }

        if (target != game.target) {
            return;
        }

        // Cancelling damage whenever the assassin is frozen.
        if (game.isFrozen) {
            game.assassin.sendMessage("§cYou cannot damage the target whilst frozen.");
            event.setCancelled(true);
            return;
        }

        // Damaging the target.
        event.setDamage(9000);

        // Ending the game.
        game.endGame();
    }
}
