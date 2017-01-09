package com.bluepowermod.api.power;

import com.bluepowermod.api.connect.IConnectionCache;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import uk.co.qmunity.lib.vec.IWorldLocation;

/**
 * @author MineMaarten, Koen Beckers (K4Unl)
 */
public interface IPowerBase extends IWorldLocation {

    /*
     * Forward these functions to the handler
     */

    public void readFromNBT(NBTTagCompound tagCompound);

    public void writeToNBT(NBTTagCompound tagCompound);

    public void readUpdateFromNBT(NBTTagCompound tagCompound);

    public void writeUpdateToNBT(NBTTagCompound tagCompound);

    public void update();

    public void onNeighborUpdate();

    /**
     * Negative energy for removal
     * @param energy
     * @param simulate when true, no power will be added, but the return value can be used to determine if adding power is possible.
     * @return the added power.
     */
    public double addEnergy(double energy, boolean simulate);

    public double getVoltage();

    public double getMaxVoltage();

    /*
     * Connections
     */

    public IConnectionCache<IPowerBase> getConnectionCache();

    public boolean isConnected(EnumFacing side);

    public IPowered getDevice();

    public void disconnect();
}
