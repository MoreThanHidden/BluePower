package com.bluepowermod.network.message;

import com.bluepowermod.api.misc.Accessibility;
import com.bluepowermod.network.BPNetworkHandler;
import com.bluepowermod.network.LocatedPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class MessageWirelessNewFreq extends LocatedPacket<MessageWirelessNewFreq> {

    private Accessibility acc;
    private String name;
    private boolean bundled;
    private EnumFacing face;

   // public MessageWirelessNewFreq(IWirelessGate gate, Accessibility newAccessibility, String newName, boolean bundled) {

       // super(gate);

       // acc = newAccessibility;
       // name = newName;
       // this.bundled = bundled;
       // face = gate.getFace();
   // }

    public MessageWirelessNewFreq() {

    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {

        //Frequency freq = (Frequency) WirelessManager.COMMON_INSTANCE.registerFrequency(player, name, acc, bundled);
       // ITilePartHolder h = MultipartCompatibility.getPartHolder(player.world, pos);

       // if (h != null) {
        //    IWirelessGate p = null;
           // for (IPart pa : h.getParts())
          //      if (pa instanceof IWirelessGate && ((IWirelessGate) pa).getFace() == face)
          //          p = (IWirelessGate) pa;
          //  if (p == null)
          //      return;

        //    p.setFrequency(freq);

        //    BPNetworkHandler.INSTANCE.sendTo(new MessageWirelessFrequencySync(player), (EntityPlayerMP) player);
       // }
    }

    @Override
    public void write(DataOutput buffer) throws IOException {

        super.write(buffer);

        buffer.writeInt(acc.ordinal());
        buffer.writeUTF(name);
        buffer.writeBoolean(bundled);
        buffer.writeInt(face.ordinal());
    }

    @Override
    public void read(DataInput buffer) throws IOException {

        super.read(buffer);

        acc = Accessibility.values()[buffer.readInt()];
        name = buffer.readUTF();
        bundled = buffer.readBoolean();
        face = EnumFacing.byIndex(buffer.readInt());
    }
}
