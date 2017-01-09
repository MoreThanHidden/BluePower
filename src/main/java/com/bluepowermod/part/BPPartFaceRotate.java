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

package com.bluepowermod.part;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.part.IQLPart;

public abstract class BPPartFaceRotate extends BPPartFace{

    private int rotation = 0;

    public int getRotation() {

        return rotation;
    }

    public void setRotation(int rotation) {

        int old = this.rotation;
        this.rotation = rotation;
        if (rotation != old)
            sendUpdatePacket();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

        super.writeToNBT(tag);
        tag.setInteger("rotation", rotation);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);
        rotation = tag.getInteger("rotation");
    }

    @Override
    public void writeUpdateData(MCByteBuf buffer) {
        super.writeUpdateData(buffer);
        buffer.writeInt(rotation);
    }

    @Override
    public void readUpdateData(MCByteBuf buffer){
        super.readUpdateData(buffer);
        rotation = buffer.readInt();
    }

    @Override
    public boolean placePart(IQLPart part, World world, BlockPos location, EnumFacing face, boolean simulated) {
        super.placePart(part, world, location, face, simulated);
        ((BPPartFaceRotate)part).setRotation((int)face.getHorizontalAngle());
        return true;
    }
}
