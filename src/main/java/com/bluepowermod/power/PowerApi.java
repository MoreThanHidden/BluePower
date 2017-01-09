package com.bluepowermod.power;

import com.bluepowermod.api.power.IPowerApi;
import com.bluepowermod.api.power.IPowerBase;
import com.bluepowermod.api.power.IPowered;
import com.bluepowermod.api.power.IPoweredDeviceProvider;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PowerApi implements IPowerApi {

    private static final PowerApi instance = new PowerApi();

    public static IPowerApi getInstance() {

        return instance;
    }

    private List<IPoweredDeviceProvider> providers = new ArrayList<IPoweredDeviceProvider>();

    @Override
    public IPowered getPoweredDeviceAt(World world, BlockPos pos, EnumFacing face, EnumFacing side) {

        for (IPoweredDeviceProvider provider : providers) {
            IPowered device = provider.getPoweredDeviceAt(world, pos, face, side);
            if (device != null)
                return device;
        }

        return null;
    }

    @Override
    public void registerPoweredDeviceProvider(IPoweredDeviceProvider provider) {

        if (provider == null)
            return;
        if (providers.contains(provider))
            return;

        providers.add(provider);
    }

    @Override
    public IPowerBase createPowerHandler(IPowered device) {

        return new PowerHandler(device);
    }

}
