package com.bluepowermod.container;

import com.bluepowermod.network.BPNetworkHandler;
import com.bluepowermod.network.message.MessageSyncMachineBacklog;
import com.bluepowermod.tile.TileMachineBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;

public abstract class ContainerMachineBase extends ContainerGhosts<TileMachineBase> {

    private int backlogSize = -1;

    public ContainerMachineBase(TileMachineBase machine) {
        super(machine);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    @Override
    public void detectAndSendChanges() {

        super.detectAndSendChanges();

        for (Object crafter : listeners) {
            IContainerListener icrafting = (IContainerListener) crafter;

            if (backlogSize != te.getBacklog().size() && icrafting instanceof EntityPlayerMP) {
                BPNetworkHandler.INSTANCE.sendTo(new MessageSyncMachineBacklog(te, te.getBacklog()), (EntityPlayerMP) icrafting);
            }
        }
        backlogSize = te.getBacklog().size();
    }
}
