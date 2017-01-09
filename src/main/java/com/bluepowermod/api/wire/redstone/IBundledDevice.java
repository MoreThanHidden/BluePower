package com.bluepowermod.api.wire.redstone;

import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.connect.IConnectionCache;
import net.minecraft.util.EnumFacing;
import uk.co.qmunity.lib.util.MinecraftColor;
import uk.co.qmunity.lib.vec.IWorldLocation;

public interface IBundledDevice extends IWorldLocation {

    /**
     * Returns whether or not the device passed as an argument can be connected to this device on the specified side. It also takes a ConnectionType,
     * which determines the type of connection to this device.
     */
    public boolean canConnect(EnumFacing side, IBundledDevice dev, ConnectionType type);

    /**
     * Returns a cache of all the connections of other devices with this one. Create an instance of this class by calling
     * {@link IRedstoneApi#createBundledConnectionCache(IBundledDevice)}
     */
    public IConnectionCache<? extends IBundledDevice> getBundledConnectionCache();

    /**
     * Gets the output of this device on the specified side.
     */
    public byte[] getBundledOutput(EnumFacing side);

    /**
     * Sets the power level on the specified side to a set power level.
     */
    public void setBundledPower(EnumFacing side, byte[] power);

    /**
     * Gets the input of this device on the specified side.
     */
    public byte[] getBundledPower(EnumFacing side);

    /**
     * Notifies the device of a power change. (Usually called after propagation)
     */
    public void onBundledUpdate();

    /**
     * Gets the color of this bundled device. Normally used to determine if other blocks should connect to it.
     */
    public MinecraftColor getBundledColor(EnumFacing side);

    /**
     * Returns whether or not this is a normal face (if face devices should be able to connect to it)
     */
    public boolean isNormalFace(EnumFacing side);

}
