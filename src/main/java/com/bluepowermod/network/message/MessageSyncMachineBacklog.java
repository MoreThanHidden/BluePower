package com.bluepowermod.network.message;

import com.bluepowermod.ClientProxy;
import com.bluepowermod.part.tube.TubeStack;
import com.bluepowermod.tile.TileMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import uk.co.qmunity.lib.client.gui.QLGuiContainerBase;
import uk.co.qmunity.lib.network.LocatedPacket;
import uk.co.qmunity.lib.network.MCByteBuf;

import java.util.ArrayList;
import java.util.List;

public class MessageSyncMachineBacklog extends LocatedPacket<MessageSyncMachineBacklog> {

    private List<TubeStack> stacks = new ArrayList<TubeStack>();

    public MessageSyncMachineBacklog() {

    }

    public MessageSyncMachineBacklog(TileMachineBase tile, List<TubeStack> stacks) {

        super(tile.getPos());
        this.stacks = stacks;
    }

    @Override
    public void toBytes(MCByteBuf buf) {

        super.toBytes(buf);
        buf.writeInt(stacks.size());
        for (TubeStack stack : stacks) {
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            ByteBufUtils.writeTag(buf, tag);
        }
    }

    @Override
    public void fromBytes(MCByteBuf buf) {
        super.fromBytes(buf);
        int amount = buf.readInt();
        for (int i = 0; i < amount; i++) {
            stacks.add(TubeStack.loadFromNBT(ByteBufUtils.readTag(buf)));
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

        TileEntity te = player.world.getTileEntity(pos);
        if (te instanceof TileMachineBase) {
            ((TileMachineBase) te).setBacklog(stacks);
            QLGuiContainerBase gui = (QLGuiContainerBase) ClientProxy.getOpenedGui();
            if (gui != null)
                gui.updateScreen();
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }

}
