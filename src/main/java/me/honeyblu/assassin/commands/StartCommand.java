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

package me.honeyblu.assassin.commands;

import me.honeyblu.assassin.Game;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    // ------------------------------- //
    // Constructor
    // ------------------------------- //
    private final Game game;

    public StartCommand(Game game) {
        this.game = game;
    }

    // ------------------------------- //
    // Command Executor
    // ------------------------------- //
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Checking if the commandExecutor has operating permissions.
        if (!sender.isOp() && !sender.hasPermission("assassin.command.start")) {
            sender.sendMessage("§4You do not have permission.");
            return true;
        }

        // Checking if the correct amount of arguments have been supplied.
        if (args.length != 2) {
            sender.sendMessage("§4Command usage: §c/start <assassin> <target>");
            return true;
        }

        String assassinName = args[0];
        String targetName = args[1];

        // Checking if the arguments are correct and the players are online.
        Player assassin = Bukkit.getPlayer(assassinName);
        if (assassin == null) {
            sender.sendMessage("§4Error: §c" + assassinName + "§4 is not a player.");
            sender.sendMessage("§4Command usage: §c/start <assassin> <target>");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage("§4Error: §c" + targetName + "§4 is not a player.");
            sender.sendMessage("§4Command usage: §c/start <assassin> <target>");
            return true;
        }

        // Checking if there are two different players.
        if (assassin == target) {
            sender.sendMessage("§4Error: The assassin and target cannot be the same user.");
            sender.sendMessage("§4Command usage: §c/start <assassin> <target>");
            return true;
        }

        // Starting the game.
        sender.sendMessage("§2Command successful: §aStarting the game.");
        game.startGame(assassin, target);
        return true;
    }
}
