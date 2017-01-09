/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

package com.bluepowermod.tile.tier1;

import com.bluepowermod.tile.TileMachineBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.List;



public class TileBlockBreaker extends TileMachineBase {
    
    @Override
    protected void redstoneChanged(boolean newValue) {
    
        super.redstoneChanged(newValue);
        
        if (!world.isRemote && newValue && isBufferEmpty()) {
            EnumFacing direction = getFacingDirection();
            IBlockState breakState = world.getBlockState(pos.offset(direction));
            if (!canBreakBlock(breakState.getBlock(), world, breakState, pos.offset(direction))) return;
            List<ItemStack> breakStacks = breakState.getBlock().getDrops(world, pos.offset(direction), breakState, 0);
            world.destroyBlock(pos.offset(direction), false); // destroyBlock
            addItemsToOutputBuffer(breakStacks);
        }
    }

    private boolean canBreakBlock(Block block, World world, IBlockState state, BlockPos pos) {
    
        return !world.isAirBlock(pos) && !(block instanceof BlockLiquid) && !(block instanceof IFluidBlock) && block.getBlockHardness(state, world, pos) > -1.0F;
    }
    
    @Override
    public boolean canConnectRedstone() {
    
        return true;
    }
}
