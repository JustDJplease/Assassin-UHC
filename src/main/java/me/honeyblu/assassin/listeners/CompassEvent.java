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
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class CompassEvent implements Listener {

    // ------------------------------- //
    // Constructor & Private variables
    // ------------------------------- //
    private final Game game;
    private long lastCompassCheck = 0L;
    private long lastInteract = 0L;

    public CompassEvent(Game game) {
        this.game = game;
    }

    // ------------------------------- //
    // Listener
    // ------------------------------- //
    @EventHandler
    public void onAssassinClickCompass(PlayerInteractEvent event) {

        // Checking if it was a right-click.
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Checking if a game is active.
        if (!game.isGameActive) {
            return;
        }

        // Preventing NullPointerExceptions.
        Validate.notNull(game.target, "Target player cannot be null!");
        Validate.notNull(game.assassin, "Assassin player cannot be null!");

        Player assassin = event.getPlayer();

        // Checking if it was the assassin who clicked.
        if (assassin != game.assassin) {
            return;
        }

        // Checking if the assassin is holding a compass.
        EntityEquipment equipment = assassin.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack heldItem = equipment.getItemInMainHand();
        if (heldItem.getType() != Material.COMPASS) {
            return;
        }

        // Checking if the assassin is on cooldown.
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCompassCheck < 30000) {

            // Prevent spam-sending this message.
            if (currentTime - lastInteract > 500) {
                assassin.sendMessage("§4You can only use this every §c30 §4seconds.");
                lastInteract = currentTime;
            }

            // Not doing anything anyway.
            return;
        }

        // Setting the new compass location.
        Location targetLocation = game.target.getLocation();
        Location assassinLocation = game.assassin.getLocation();

        World targetWorld = targetLocation.getWorld();
        World assassinWorld = assassinLocation.getWorld();

        Validate.notNull(targetWorld, "World cannot be null!");
        Validate.notNull(assassinWorld, "World cannot be null!");

        boolean shouldGiveSpeedAnyway = false;

        if (Objects.equals(targetWorld, assassinWorld)) {
            assassin.setCompassTarget(targetLocation);
            assassin.sendMessage("§2Compass updated. Now tracking §a" + game.target.getName() + "§2.");
        } else {
            assassin.sendMessage("§cTarget is in a different dimension.");
            shouldGiveSpeedAnyway = true;
        }

        // Playing a sound-effect and updating the cooldown.
        assassin.playSound(assassinLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 1f);
        assassinWorld.playSound(assassinLocation, Sound.ENTITY_ENDER_DRAGON_GROWL, 100f, 0.4f);

        // Updating cooldown variables.
        lastCompassCheck = currentTime;
        lastInteract = currentTime;

        // Giving the assassin speed and jump-boost based on the distance (If they are very far away).
        if ((assassinLocation.distanceSquared(targetLocation) >= 90000) || shouldGiveSpeedAnyway) {

            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 280, 4);
            PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 280, 2);
            PotionEffect haste = new PotionEffect(PotionEffectType.FAST_DIGGING, 280, 1);

            assassin.addPotionEffect(speed, true);
            assassin.addPotionEffect(jump, true);
            assassin.addPotionEffect(haste, true);

            assassin.sendMessage("§2You received §aSpeed §2, §aJump §2and §aHaste§2.");
            return;
        }

        // Giving the assassin speed and jump-boost based on the distance (If they are relatively far away).
        if ((assassinLocation.distanceSquared(targetLocation) >= 15625)) {

            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 140, 4);
            PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 140, 2);
            PotionEffect haste = new PotionEffect(PotionEffectType.FAST_DIGGING, 140, 1);

            assassin.addPotionEffect(speed, true);
            assassin.addPotionEffect(jump, true);
            assassin.addPotionEffect(haste, true);

            assassin.sendMessage("§2You received §aSpeed §2, §aJump §2and §aHaste§2.");
        }
    }
}
