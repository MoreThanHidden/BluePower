package com.bluepowermod.api.power;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPowerApi {

    /**
     * Returns the powered device at the specified coordinates and on the specified side and face. Data gotten from the registered
     * {@link IPoweredDeviceProvider}s
     *
     * @param world
     *            The world where the device is
     * @param pos
     *            coordinate of the device
     * @param side
     *            Side of the device we're looking for
     * @param face
     *            Face the device must be placed on or {@link null} if not know or not a face device
     * @return The powered device at the specified coords, side and face.
     */
    public IPowered getPoweredDeviceAt(World world, BlockPos pos, EnumFacing face, EnumFacing side);

    /**
     * Registers a redstone/bundled device provider.
     */
    public void registerPoweredDeviceProvider(IPoweredDeviceProvider provider);

    public IPowerBase createPowerHandler(IPowered device);

}
