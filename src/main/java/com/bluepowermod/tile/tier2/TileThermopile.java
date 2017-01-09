package com.bluepowermod.tile.tier2;


import com.bluepowermod.BluePower;
import com.bluepowermod.api.power.IPowered;
import com.bluepowermod.reference.PowerConstants;
import com.bluepowermod.tile.TileMachineBase;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * @author Koen Beckers (K-4U)
 */
public class TileThermopile extends TileMachineBase implements IPowered {

    private int updateTemperatureTicks = 0;
    private double temperatureDifference;

    @Override
    public void update() {
        super.update();

        if (!getWorld().isRemote) {
            if(updateTemperatureTicks == 3){
                temperatureDifference = getTemperatureDifference();
                updateTemperatureTicks++;
            }else if(updateTemperatureTicks < 3){
                updateTemperatureTicks ++;
            }
            double addedEnergy = temperatureDifference * PowerConstants.THERMOPILE_MULTIPLIER;
            getPowerHandler(null).addEnergy(addedEnergy, false);
        }
    }

    @Override
    public void onBlockNeighbourChanged() {
        super.onBlockNeighbourChanged();
        updateTemperatureTicks = 0;
    }

    /**
     * @author Koen Beckers (K-4U)
     * @return double of the difference in temperature there exists between each block
     */
    private int getTemperatureDifference() {
        //Temperature is in kelvin.
        int lowestTemperature = Integer.MAX_VALUE;
        int temperature = 0;
        int amountOfBlocks = 0;
        for(EnumFacing dir : EnumFacing.VALUES){
            BlockPos pos = this.getPos().offset(dir);
            Block toCheck = getWorld().getBlockState(pos).getBlock();
            Fluid blockFluid = FluidRegistry.lookupFluidForBlock(toCheck);
            if(toCheck == Blocks.FLOWING_LAVA && blockFluid == null){
                blockFluid = FluidRegistry.LAVA;
            }
            if(toCheck == Blocks.FLOWING_WATER && blockFluid == null){
                blockFluid = FluidRegistry.WATER;
            }

            if(blockFluid != null){
                lowestTemperature = Math.min(blockFluid.getTemperature(getWorld(), pos), lowestTemperature);
                temperature += blockFluid.getTemperature(getWorld(), pos);
                amountOfBlocks++;
            }
            if(toCheck == Blocks.ICE || toCheck == Blocks.PACKED_ICE || toCheck == Blocks.SNOW){
                lowestTemperature = Math.min(273, lowestTemperature);
                temperature += 273;
                amountOfBlocks++;
            }
        }

        BluePower.log.info("We found " + amountOfBlocks + " L:" + lowestTemperature + " T:" + temperature);
        temperature = temperature - (lowestTemperature * amountOfBlocks);

        return temperature;
    }

}
