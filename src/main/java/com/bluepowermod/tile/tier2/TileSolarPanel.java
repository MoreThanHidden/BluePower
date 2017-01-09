package com.bluepowermod.tile.tier2;

import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.EnumFacing;

import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.misc.IFace;
import com.bluepowermod.api.power.IPowered;
import com.bluepowermod.reference.PowerConstants;
import com.bluepowermod.tile.TileMachineBase;

/**
 * @author Koen Beckers (K4Unl)
 */
public class TileSolarPanel extends TileMachineBase implements IPowered, IFace {

    @Override
    public EnumFacing getFace() {

        return EnumFacing.DOWN;
    }

    @Override
    public boolean canConnectPower(EnumFacing side, IPowered dev, ConnectionType type) {

        if (side == EnumFacing.UP)
            return false;
        if (type == ConnectionType.OPEN_CORNER && side == EnumFacing.DOWN)
            return false;
        if (dev instanceof IFace && type == ConnectionType.OPEN_CORNER && dev.getY() == getY() && ((IFace) dev).getFace() != EnumFacing.DOWN)
            return false;

        return true;
    }

    @Override
    public boolean isNormalFace(EnumFacing side) {

        return side == EnumFacing.DOWN;
    }

    @Override
    public void updateEntity() {

        super.updateEntity();

        double addedEnergy = getDaylightStrength() * PowerConstants.SOLAR_PANEL_MULTIPLIER;
        if (!getWorldObj().isRemote) {
            getPowerHandler(EnumFacing.UNKNOWN).addEnergy(addedEnergy, false);
        }

    }

    public int getDaylightStrength() {

        int i1 = getWorldObj().skylightSubtracted;
        int savedLight = getWorldObj().getSavedLightValue(EnumSkyBlock.Sky, xCoord, yCoord + 1, zCoord);
        i1 = savedLight - i1;
        float f = getWorldObj().getCelestialAngleRadians(1.0F);

        if (f < (float) Math.PI) {
            f += (0.0F - f) * 0.2F;
        } else {
            f += ((float) Math.PI * 2F - f) * 0.2F;
        }

        i1 = Math.round(i1 * MathHelper.cos(f));

        if (i1 < 0) {
            i1 = 0;
        }

        if (i1 > 15) {
            i1 = 15;
        }

        return i1;
    }

}
