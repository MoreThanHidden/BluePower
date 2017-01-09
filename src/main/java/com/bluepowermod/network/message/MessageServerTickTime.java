package com.bluepowermod.network.message;

import com.bluepowermod.part.tube.TubeStack;
import net.minecraft.entity.player.EntityPlayer;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.network.Packet;

public class MessageServerTickTime extends Packet<MessageServerTickTime> {
    private double tickTime;

    public MessageServerTickTime() {
    }

    public MessageServerTickTime(double tickTime) {
        this.tickTime = tickTime;
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        TubeStack.tickTimeMultiplier = Math.min(1, 50D / Math.max(tickTime - 5, 0.01));//Let the client stack go a _little_ bit faster than the real value (50 / tickTime), as else if the server stacks arrive first, glitches happen.
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }

    @Override
    public void fromBytes(MCByteBuf buffer) {
        tickTime = buffer.readDouble();
    }

    @Override
    public void toBytes(MCByteBuf buffer) {
        buffer.writeDouble(tickTime);
    }

}
