package com.bluepowermod.redstone;

import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.connect.IConnection;
import com.bluepowermod.api.connect.IConnectionCache;
import com.bluepowermod.api.misc.IFace;
import com.bluepowermod.api.wire.redstone.IRedConductor;
import com.bluepowermod.api.wire.redstone.IRedstoneConductor;
import com.bluepowermod.api.wire.redstone.IRedstoneConductor.IAdvancedRedstoneConductor;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Map;

public class RedstoneWrappers {

    public static IRedstoneDevice wrap(IRedstoneDevice dev, EnumFacing face) {

        if (dev instanceof IAdvancedRedstoneConductor)
            return new AdvancedRedstoneConductorWrapper((IAdvancedRedstoneConductor) dev, face);
        else if (dev instanceof IAdvancedRedstoneConductor)
            return new RedstoneConductorWrapper((IRedstoneConductor) dev, face);
        else
            return new RedstoneDeviceWrapper(dev, face);
    }

    private static EnumFacing computeDirection(Object obj, EnumFacing dir) {

        EnumFacing face = obj instanceof IFace ? ((IFace) obj).getFace() : null;

        if (face == null || face == EnumFacing.DOWN)
            return dir;

        return dir;// EnumFacing.UNKNOWN;
    }

    private static class RedstoneDeviceWrapper implements IRedstoneDevice, IFace {

        protected IRedstoneDevice dev;
        protected EnumFacing face;

        public RedstoneDeviceWrapper(IRedstoneDevice dev, EnumFacing face) {

            this.dev = dev;
            this.face = face;
        }

        @Override
        public World getWorld() {

            return dev.getWorld();
        }

        @Override
        public BlockPos getPos() {

            return dev.getPos();
        }

        @Override
        public EnumFacing getFace() {

            return face;
        }

        @Override
        public boolean canConnect(EnumFacing side, IRedstoneDevice dev, ConnectionType type) {

            return dev.canConnect(computeDirection(dev, side), dev, type);
        }

        @Override
        public IConnectionCache<? extends IRedstoneDevice> getRedstoneConnectionCache() {

            return dev.getRedstoneConnectionCache();
        }

        @Override
        public byte getRedstonePower(EnumFacing side) {

            return dev.getRedstonePower(computeDirection(dev, side));
        }

        @Override
        public void setRedstonePower(EnumFacing side, byte power) {

            dev.setRedstonePower(computeDirection(dev, side), power);
        }

        @Override
        public void onRedstoneUpdate() {

            dev.onRedstoneUpdate();
        }

        @Override
        public boolean isNormalFace(EnumFacing side) {

            return dev.isNormalFace(computeDirection(dev, side));
        }

    }

    private static class RedstoneConductorWrapper extends RedstoneDeviceWrapper implements IRedstoneConductor {

        public RedstoneConductorWrapper(IRedstoneConductor dev, EnumFacing face) {

            super(dev, face);
        }

        @Override
        public boolean hasLoss(EnumFacing side) {

            return ((IRedConductor) dev).hasLoss(computeDirection(dev, side));
        }

        @Override
        public boolean isAnalogue(EnumFacing side) {

            return ((IRedConductor) dev).isAnalogue(computeDirection(dev, side));
        }

        @Override
        public boolean canPropagateFrom(EnumFacing fromSide) {

            return ((IRedstoneConductor) dev).canPropagateFrom(computeDirection(dev, fromSide));
        }

    }

    private static class AdvancedRedstoneConductorWrapper extends RedstoneConductorWrapper implements IAdvancedRedstoneConductor {

        public AdvancedRedstoneConductorWrapper(IAdvancedRedstoneConductor dev, EnumFacing face) {

            super(dev, face);
        }


        @Override
        public Collection<Map.Entry<IConnection<IRedstoneDevice>, Boolean>> propagate(EnumFacing fromSide) {
            return ((IAdvancedRedstoneConductor) dev).propagate(computeDirection(dev, fromSide));
        }
    }

}
