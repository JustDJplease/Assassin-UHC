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

package me.honeyblu.assassin.util;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.honeyblu.assassin.Game;
import me.honeyblu.assassin.util.packet.WrapperPlayServerWorldBorder;

public class WorldBorderUtil {

    // ------------------------------- //
    // Constructor
    // ------------------------------- //
    private final Game game;

    public WorldBorderUtil(Game game) {
        this.game = game;
    }

    // ------------------------------- //
    // Public Utility Methods
    // ------------------------------- //
    public void shrink() {
        // Get the size of the world border.
        int worldBorderSize = (int) game.assassin.getWorld().getWorldBorder().getSize();

        // Creating the packet.
        WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();
        packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);
        packet.setWarningDistance(worldBorderSize);

        // Sending the packet.
        packet.sendPacket(game.assassin);
    }

    public void enlarge() {
        // Creating the packet.
        WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();
        packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);
        packet.setWarningDistance(5);

        // Sending the packet.
        packet.sendPacket(game.assassin);
    }
}
