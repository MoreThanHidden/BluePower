package com.bluepowermod.power;

import com.bluepowermod.api.power.IPowered;
import com.bluepowermod.api.power.IPoweredDeviceProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PoweredDeviceProviderVanilla implements IPoweredDeviceProvider {

    @Override
    public IPowered getPoweredDeviceAt(World world, BlockPos pos, EnumFacing side, EnumFacing face) {

        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof IPowered)
            return (IPowered) te;

        return null;
    }

}
