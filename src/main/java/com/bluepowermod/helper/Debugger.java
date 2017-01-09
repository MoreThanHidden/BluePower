/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.helper;

import com.bluepowermod.network.BPNetworkHandler;
import com.bluepowermod.network.message.MessageDebugBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.qmunity.lib.part.IPart;

import java.util.Random;

/**
 * Class aimed for debugging purposes only
 *
 * @author MineMaarten
 */
public class Debugger {

    private static Random rand = new Random();

    public static void indicateBlock(TileEntity te) {

        indicateBlock(te.getWorld(), te.getPos());
    }

    public static void indicateBlock(IQLPart part) {

        indicateBlock(part.getWorld(),part.getPos());
    }

    public static void indicateBlock(World world, BlockPos pos) {

        if (world != null) {
            if (world.isRemote) {
                for (int i = 0; i < 5; i++) {
                    double dx = pos.getX() + 0.5;
                    double dy = pos.getY() + 0.5;
                    double dz = pos.getZ() + 0.5;
                    world.spawnParticle(EnumParticleTypes.FLAME, dx, dy, dz, 0, 0, 0);
                }
            } else {
                BPNetworkHandler.INSTANCE.sendToAllAround(new MessageDebugBlock(pos), world);
            }
        }
    }
}
