package com.bluepowermod.redstone;

import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IPropagator;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import com.bluepowermod.api.wire.redstone.IRedwire.IInsulatedRedwire;
import net.minecraft.util.EnumFacing;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.util.MinecraftColor;

public class BundledPropagator implements IPropagator<IBundledDevice> {

    private IBundledDevice device;
    private EnumFacing side;

    public BundledPropagator(IBundledDevice device, EnumFacing side) {

        this.device = device;
        this.side = side;
    }

    @Override
    public void propagate() {

        if ((device instanceof IQLPart && ((IQLPart) device).getParent() == null) || device.getWorld() == null)
            return;

        for (MinecraftColor c : MinecraftColor.VALID_COLORS) {
            IRedstoneDevice dev;
            if (device instanceof IInsulatedRedwire) {
                dev = (IRedstoneDevice) device;
            } else {
                dev = BundledDeviceWrapper.wrap(device, c);
            }
            if (dev == null)
                continue;
            RedstoneApi.getInstance().getRedstonePropagator(dev, side).propagate();
        }
    }

}
