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

import com.bluepowermod.api.misc.IFace;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.part.ISlottedPart;
import uk.co.qmunity.lib.part.PartSlot;

public abstract class BPPartFace extends BPPart implements ISlottedPart, IFace, IPartPlacement {

    private EnumFacing face = EnumFacing.NORTH;

    @Override
    public EnumFacing getFace() {

        return face;
    }

    public void setFace(EnumFacing face) {
        this.face = face;
    }

    public boolean canStay() {

        return getWorld().isSideSolid(getPos().offset(getFace()),
                getFace().getOpposite());
    }

    @Override
    public int getSlotMask() {
        return PartSlot.face(face.getOpposite()).ordinal();
    }

    public void face(EnumFacing face) {

        this.face = face;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

        tag.setInteger("face", face.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        face = EnumFacing.getFront(tag.getInteger("face"));
    }

    @Override
    public void writeUpdateData(MCByteBuf buffer) {
        super.writeUpdateData(buffer);
        buffer.writeInt(face.ordinal());
    }

    @Override
    public void readUpdateData(MCByteBuf buffer) {
        super.readUpdateData(buffer);
        face = EnumFacing.getFront(buffer.readInt());
    }

    @Override
    public void onNeighborBlockChange() {

        if (getParent() == null || getWorld() == null || getWorld().isRemote)
            return;

        if (!canStay()) {
            harvest(null, null);
            return;
        }

        super.onNeighborBlockChange();
    }

    @Override
    public boolean placePart(IQLPart part, World world, BlockPos location, EnumFacing face, boolean simulated) {
        ((BPPartFace)part).setFace(face.getOpposite());
        return true;
    }

}
