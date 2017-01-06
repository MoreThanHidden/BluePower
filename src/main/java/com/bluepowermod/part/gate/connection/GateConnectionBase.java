package com.bluepowermod.part.gate.connection;

import com.bluepowermod.api.connect.IConnection;
import com.bluepowermod.api.gate.IGateConnection;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import com.bluepowermod.part.gate.GateBase;
import com.bluepowermod.redstone.DummyRedstoneDevice;
import com.bluepowermod.redstone.RedstoneApi;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import uk.co.qmunity.lib.helper.RedstoneHelper;
import uk.co.qmunity.lib.util.Dir;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

;

public abstract class GateConnectionBase implements IGateConnection {

    protected GateBase gate;
    protected Dir direction;
    protected boolean enabled = false;
    protected boolean outputOnly;

    private boolean needsSyncing = false;

    public GateConnectionBase(GateBase gate, Dir direction) {

        this.gate = gate;
        this.direction = direction;
    }

    @Override
    public GateBase getGate() {

        return gate;
    }

    @Override
    public Dir getDirection() {

        return direction;
    }

    public EnumFacing getEnumFacing() {

        return getDirection().toEnumFacing(gate.getFace(), gate.getRotation());
    }

    @Override
    public void notifyUpdate() {

        if (gate.getParent() == null || gate.getWorld() == null)
            return;

        EnumFacing d = getEnumFacing();
        IConnection<? extends IRedstoneDevice> c = gate.getRedstoneConnectionCache().getConnectionOnSide(d);

        if (c == null || c.getB() instanceof DummyRedstoneDevice) {
            World world = gate.getWorld();
            int x = gate.getPos().getX(), y = gate.getPos().getY(), z = gate.getPos().getZ();

            RedstoneHelper.notifyRedstoneUpdate(world, x, y, z, d, true);
        } else {
            RedstoneApi.getInstance().getRedstonePropagator(getGate(), d).propagate();
        }
    }

    public abstract void notifyUpdateIfNeeded();

    @Override
    public boolean isEnabled() {

        return enabled;
    }

    @Override
    public IGateConnection setEnabled(boolean enabled) {

        this.enabled = enabled;

        setNeedsSyncing(true);

        return this;
    }

    @Override
    public IGateConnection enable() {

        return setEnabled(true);
    }

    @Override
    public IGateConnection disable() {

        return setEnabled(false);
    }

    @Override
    public IGateConnection setOutputOnly() {

        outputOnly = true;
        return this;
    }

    @Override
    public IGateConnection setBidirectional() {

        outputOnly = false;
        return this;
    }

    @Override
    public boolean isOutputOnly() {

        return outputOnly;
    }

    /**
     * Gets the output (and input if !outputOnly) signal in this connection as a double (0-1)
     */
    public abstract double getSignal();

    @Override
    public void writeToNBT(NBTTagCompound tag) {

        tag.setBoolean("enabled", enabled);
        tag.setInteger("direction", direction.ordinal());
        tag.setBoolean("outputOnly", outputOnly);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        enabled = tag.getBoolean("enabled");
        direction = Dir.values()[tag.getInteger("direction")];
        outputOnly = tag.getBoolean("outputOnly");
    }

    @Override
    public void writeData(DataOutput buffer) throws IOException {

        setNeedsSyncing(false);

        buffer.writeBoolean(enabled);
    }

    @Override
    public void readData(DataInput buffer) throws IOException {

        enabled = buffer.readBoolean();
    }

    @Override
    public boolean canConnectRedstone() {

        return !isBundled();
    }

    public boolean needsSyncing() {

        return needsSyncing;
    }

    protected void setNeedsSyncing(boolean needsSyncing) {

        this.needsSyncing = needsSyncing;
    }

    public GateConnectionBase reset() {

        enabled = false;

        return this;
    }

}
