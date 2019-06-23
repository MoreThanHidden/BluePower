package com.bluepowermod.block.machine;

import com.bluepowermod.api.misc.MinecraftColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockLampSurface extends BlockLamp {

    private final String name;
    private final Boolean isInverted;
    private final AxisAlignedBB size;

    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    public BlockLampSurface(String name, boolean isInverted, MinecraftColor color, AxisAlignedBB size) {
        super(name, isInverted, color);
        this.name = name;
        this.isInverted = isInverted;
        this.size = size;
        this.setDefaultState(stateContainer.getBaseState().with(POWER, isInverted ? 15 : 0).with(FACING, Direction.UP));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initModel() {

    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWER, FACING);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean bool) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, bool);
        if (!world.getBlockState(pos.offset(state.get(FACING).getOpposite())).isSolid()) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, Direction facing, BlockState state2, IWorld world, BlockPos pos1, BlockPos pos2, Hand hand) {
        return super.getStateForPlacement(state, facing, state2, world, pos1, pos2, hand).with(FACING, facing);
    }

}
