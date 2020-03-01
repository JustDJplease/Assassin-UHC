/*
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets
 * Copyright (C) dmulloy2 <http://dmulloy2.net>
 * Copyright (C) Kristian S. Strangeland
 */

package me.honeyblu.assassin.util.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.WorldBorderAction;

public class WrapperPlayServerWorldBorder extends AbstractPacket {
    private static final PacketType TYPE = PacketType.Play.Server.WORLD_BORDER;

    public WrapperPlayServerWorldBorder() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public void setAction(WorldBorderAction value) {
        handle.getWorldBorderActions().write(0, value);
    }

    public void setWarningDistance(int value) {
        handle.getIntegers().write(2, value);
    }
}
