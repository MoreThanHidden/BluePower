package com.bluepowermod.power;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import uk.co.qmunity.lib.network.annotation.GuiSynced;

import com.bluepowermod.api.connect.IConnection;
import com.bluepowermod.api.connect.IConnectionCache;
import com.bluepowermod.api.misc.IFace;
import com.bluepowermod.api.power.IPowerBase;
import com.bluepowermod.api.power.IPowered;

/**
 * @author Koen Beckers (K4Unl)
 */
public class PowerHandler implements IPowerBase, IFace {

    private final IPowered device;
    private final PowerConnectionCache cache = new PowerConnectionCache(this);
    private final boolean[] connections = new boolean[6];

    @GuiSynced
    private double voltage;

    public PowerHandler(IPowered device) {

        this.device = device;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {

        voltage = tagCompound.getDouble("voltage");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {

        tagCompound.setDouble("voltage", voltage);
    }

    @Override
    public void readUpdateFromNBT(NBTTagCompound tagCompound) {

        NBTTagCompound tag = tagCompound.getCompoundTag("powerHandler");
        for (int i = 0; i < 6; i++)
            connections[i] = tag.getBoolean("connected_" + i);

        // TODO
    }

    @Override
    public void writeUpdateToNBT(NBTTagCompound tagCompound) {

        NBTTagCompound tag = new NBTTagCompound();
        for (int i = 0; i < 6; i++)
            tag.setBoolean("connected_" + i, cache.getConnectionOnSide(ForgeDirection.getOrientation(i)) != null);
        tagCompound.setTag("powerHandler", tag);

        // TODO
    }

    @Override
    public void update() {

        double avgVoltage = getVoltage();
        int neighborCount = 1;
        for (ForgeDirection dir : ForgeDirection.values()) {// Loop through the cache (including UNKNOWN)
            IConnection<IPowerBase> neighbor = cache.getConnectionOnSide(dir);
            if (neighbor != null) {
                avgVoltage += neighbor.getB().getVoltage();
                neighborCount++;
            }
        }
        avgVoltage /= neighborCount;

        addEnergy(avgVoltage - getVoltage(), false);
        for (ForgeDirection dir : ForgeDirection.values()) {
            IConnection<IPowerBase> neighbor = cache.getConnectionOnSide(dir);
            if (neighbor != null) {
                neighbor.getB().addEnergy(avgVoltage - neighbor.getB().getVoltage(), false);
            }
        }
    }

    @Override
    public void onNeighborUpdate() {

        cache.recalculateConnections();
    }

    @Override
    public double getVoltage() {

        return voltage;
    }

    @Override
    public double addEnergy(double energy, boolean simulate) {

        double actualEnergy;
        if (energy > 0) {
            actualEnergy = Math.min(getMaxVoltage() - getVoltage(), energy);
        } else {
            actualEnergy = Math.max(energy, -getVoltage());
        }
        if (!simulate)
            voltage += actualEnergy;
        return actualEnergy;
    }

    @Override
    public double getMaxVoltage() {

        return 12;
    }

    @Override
    public IConnectionCache<IPowerBase> getConnectionCache() {

        return cache;
    }

    @Override
    public boolean isConnected(ForgeDirection side) {

        if (device.getWorld() != null && !device.getWorld().isRemote)
            return cache.getConnectionOnSide(side) != null;

        return connections[side.ordinal()];
    }

    @Override
    public IPowered getDevice() {

        return device;
    }

    @Override
    public void disconnect() {

        cache.disconnectAll();
    }

    @Override
    public ForgeDirection getFace() {

        if (device instanceof IFace)
            return ((IFace) device).getFace();

        return ForgeDirection.UNKNOWN;
    }

}
