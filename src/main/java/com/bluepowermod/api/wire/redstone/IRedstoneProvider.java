package com.bluepowermod.api.wire.redstone;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;



public interface IRedstoneProvider {

    /**
     * Returns the redstone device at the specified coordinates and on the specified side and face.
     *
     * @param world
     *            The world where the device is
     * @param pos
     *            Location of the device.
     * @param side
     *            Side of the device we're looking for
     * @param face
     *            Face the device must be placed on or {@link null} if not know or not a face device
     * @return The redstone device at the specified coords, side and face.
     */
    public IRedstoneDevice getRedstoneDeviceAt(World world, BlockPos pos, EnumFacing side, EnumFacing face);

    /**
     * Returns the bundled device at the specified coordinates and on the specified side and face.
     *
     * @param world
     *            The world where the device is
     * @param pos
     *            Coordinate of the device
     * @param side
     *            Side of the device we're looking for
     * @param face
     *            Face the device must be placed on or {@link null} if not know or not a face device
     * @return The bundled device at the specified coords, side and face.
     */
    public IBundledDevice getBundledDeviceAt(World world, BlockPos pos, EnumFacing side, EnumFacing face);



}
