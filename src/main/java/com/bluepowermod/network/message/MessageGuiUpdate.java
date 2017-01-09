/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.network.message;

import com.bluepowermod.BluePower;
import com.bluepowermod.part.IGuiButtonSensitive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import uk.co.qmunity.lib.network.LocatedPacket;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.part.IPartHolder;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.part.MultipartCompat;

/**
 *
 * @author MineMaarten
 */

public class MessageGuiUpdate extends LocatedPacket<MessageGuiUpdate> {

    private String partId;
    private int icId; // only used with the Integrated Circuit
    private int messageId;
    private int value;

    public MessageGuiUpdate() {

    }

    /**
     *
     * @param part
     *            should also implement IGuiButtonSensitive to be able to receive this packet.
     * @param messageId
     * @param value
     */
    public MessageGuiUpdate(IQLPart part, int messageId, int value) {

        super(part.getPos());

        // if (part instanceof GateBase && ((GateBase) part).parentCircuit != null) {
        // icId = ((GateBase) part).parentCircuit.getGateIndex((GateBase) part);
        // part = ((GateBase) part).parentCircuit;
        // }
        partId = getPartId(part);
        if (partId.equals("-1"))
            BluePower.log.warn("[MessageGuiUpdate] BPPart couldn't be found");

        this.messageId = messageId;
        this.value = value;
    }

    public MessageGuiUpdate(TileEntity tile, int messageId, int value) {

        super(tile.getPos());
        partId = "-1";
        this.messageId = messageId;
        this.value = value;
    }

    private String getPartId(IQLPart part) {
        return (MultipartCompat.getHolder(part.getWorld(), part.getPos()).getPartID(part));
    }

    @Override
    public void toBytes(MCByteBuf buf) {

        super.toBytes(buf);
        buf.writeInt(messageId);
        buf.writeString(partId);
        buf.writeInt(value);
        buf.writeInt(icId);
    }

    @Override
    public void fromBytes(MCByteBuf  buf) {

        super.fromBytes(buf);
        messageId = buf.readInt();
        partId = buf.readString();
        value = buf.readInt();
        icId = buf.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {

        IPartHolder partHolder = MultipartCompat.getHolder(player.world, pos);
        if (partHolder != null) {
            messagePart(player, partHolder);
        } else {
            TileEntity te = player.world.getTileEntity(pos);
            if (te instanceof IGuiButtonSensitive) {
                ((IGuiButtonSensitive) te).onButtonPress(player, messageId, value);
            }
        }
    }

    private void messagePart(EntityPlayer player, IPartHolder partHolder) {

            IQLPart part = partHolder.findPart(partId);
            // IntegratedCircuit circuit = null;
            // if (part instanceof IntegratedCircuit) {
            // circuit = (IntegratedCircuit) part;
            // part = ((IntegratedCircuit) part).getPartForIndex(message.icId);
            // }
            if (part instanceof IGuiButtonSensitive) {
                ((IGuiButtonSensitive) part).onButtonPress(player, messageId, value);
                // if (circuit != null)
                // circuit.sendUpdatePacket();
            } else {
                BluePower.log.error("[BluePower][MessageGuiPacket] Part doesn't implement IGuiButtonSensitive");
            }
    }
}
