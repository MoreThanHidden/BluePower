package com.bluepowermod.api.wire.redstone;

import net.minecraft.util.EnumFacing;
import uk.co.qmunity.lib.util.MinecraftColor;

public interface IInsulatedRedstoneDevice extends IRedstoneDevice {

    /**
     * Gets the insulation color on the specified side. This usually determines whether or not things can connect to it.
     */
    public MinecraftColor getInsulationColor(EnumFacing side);

}
