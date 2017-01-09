package com.bluepowermod.power;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.power.IPowerBase;
import com.bluepowermod.api.power.IPowered;
import com.bluepowermod.part.wire.ConnectionLogicHelper;
import com.bluepowermod.part.wire.ConnectionLogicHelper.IConnectableProvider;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerConnectionHelper {

    private static ConnectionLogicHelper<IPowerBase, PowerConnection> power = new ConnectionLogicHelper<IPowerBase, PowerConnection>(
            new IConnectableProvider<IPowerBase, PowerConnection>() {

                @Override
                public IPowerBase getConnectableAt(World world, BlockPos pos, EnumFacing side) {
                    return getConnectableAt(world, pos, null, side);
                }

                @Override
                public IPowerBase getConnectableAt(World world, BlockPos pos, EnumFacing face, EnumFacing side) {

                    IPowered d = BPApi.getInstance().getPowerApi().getPoweredDeviceAt(world, pos, face, side);
                    if (d == null)
                        return null;
                    return d.getPowerHandler(side);
                }

                @Override
                public PowerConnection createConnection(IPowerBase a, IPowerBase b, EnumFacing sideA, EnumFacing sideB, ConnectionType type) {

                    return new PowerConnection(a, b, sideA, sideB, type);
                }

                @Override
                public boolean canConnect(IPowerBase from, IPowerBase to, EnumFacing side, ConnectionType type) {

                    return from.getDevice().canConnectPower(side, to.getDevice(), type);
                }

                @Override
                public boolean isValidClosedCorner(IPowerBase o) {

                    return true;
                }

                @Override
                public boolean isValidOpenCorner(IPowerBase o) {

                    return true;
                }

                @Override
                public boolean isValidStraight(IPowerBase o) {

                    return true;
                }

                @Override
                public boolean isNormalFace(IPowerBase o, EnumFacing face) {

                    return o.getDevice().isNormalFace(face);
                }
            });

    public static PowerConnection getNeighbor(IPowerBase device, EnumFacing side) {

        return power.getNeighbor(device, side);
    }
}
