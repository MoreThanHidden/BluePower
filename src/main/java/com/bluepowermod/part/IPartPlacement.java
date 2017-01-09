package com.bluepowermod.part;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.qmunity.lib.part.IQLPart;

public interface IPartPlacement {

    public boolean placePart(IQLPart part, World world, BlockPos location, EnumFacing face, boolean simulated);

}