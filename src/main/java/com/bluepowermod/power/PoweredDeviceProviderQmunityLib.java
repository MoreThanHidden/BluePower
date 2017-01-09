package com.bluepowermod.power;

import com.bluepowermod.api.misc.IFace;
import com.bluepowermod.api.power.IPowered;
import com.bluepowermod.api.power.IPoweredDeviceProvider;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.qmunity.lib.part.IPartHolder;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.part.MultipartCompat;

public class PoweredDeviceProviderQmunityLib implements IPoweredDeviceProvider {

    @Override
    public IPowered getPoweredDeviceAt(World world, BlockPos pos, EnumFacing side, EnumFacing face) {

        IPartHolder holder = MultipartCompat.getHolder(world, pos);
        if (holder != null) {
            for (IQLPart p : holder.getParts()) {
                if (p instanceof IPowered) {
                    if (p instanceof IFace) {
                        if (((IFace) p).getFace() == face && ((IPowered) p).getPowerHandler(side) != null)
                            return (IPowered) p;
                    } else {
                        if (face == null && ((IPowered) p).getPowerHandler(side) != null)
                            return (IPowered) p;
                    }
                }
            }
        }

        return null;
    }

}
