package com.bluepowermod.tile;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.power.IPowerBase;
import com.bluepowermod.api.power.IPowered;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import uk.co.qmunity.lib.network.annotation.DescSynced;

public class TileBluePowerBase extends TileBase implements IRotatable {
    private IPowerBase handler;
    @DescSynced
    private boolean isPowered;

    public TileBluePowerBase() {
        if (this instanceof IPowered)
            handler = BPApi.getInstance().getPowerApi().createPowerHandler((IPowered) this);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && handler != null) {

            handler.update();
            if (world.getWorldTime() % 20 == 0) {
                isPowered = handler.getVoltage() >= handler.getMaxVoltage() * 0.8;
            }
        }
    }

    public boolean isPowered() {
        return isPowered;
    }

    public IPowerBase getPowerHandler(EnumFacing side) {

        return handler;
    }

    public boolean canConnectPower(EnumFacing side, IPowered dev, ConnectionType type) {

        return true;
    }

    public boolean isNormalFace(EnumFacing side) {

        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {

        super.readFromNBT(tagCompound);
        if (handler != null)
            handler.readFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {

        super.writeToNBT(tagCompound);
        if (handler != null)
            handler.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void invalidate() {

        super.invalidate();
        if (handler != null)
            handler.disconnect();
    }

    @Override
    public void onBlockNeighbourChanged() {
        super.onBlockNeighbourChanged();
        if (handler != null)
            handler.onNeighborUpdate();
    }

    @Override
    protected void onTileLoaded() {

        super.onTileLoaded();
        if (handler != null)
            handler.onNeighborUpdate();
    }
}
