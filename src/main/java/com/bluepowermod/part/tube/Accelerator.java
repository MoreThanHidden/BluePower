/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.part.tube;

import com.bluepowermod.client.render.IconSupplier;
import com.bluepowermod.part.IPartPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import uk.co.qmunity.lib.helper.OcclusionHelper;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.part.PartSlot;
import uk.co.qmunity.lib.vec.Cuboid;
import uk.co.qmunity.lib.vec.Vector3;

import java.util.ArrayList;
import java.util.List;



/**
 * Accelerator extends PneumaticTube, as that's much easier routing wise.
 *
 * @author MineMaarten
 *
 */
public class Accelerator extends PneumaticTube implements IPartPlacement {

    private EnumFacing rotation = EnumFacing.UP;

    @Override
    public boolean placePart(IQLPart part, World world, BlockPos location, EnumFacing face, boolean simulated) {
        if (part instanceof Accelerator)
            ((Accelerator) part).setRotation(rotation);
        return true;
    }

    @Override
    public String getType() {

        return "accelerator";
    }

    @Override
    public String getUnlocalizedName() {

        return getType();
    }

    public void setRotation(EnumFacing rotation) {

        this.rotation = rotation;
    }

    /**
     * Gets all the occlusion boxes for this block
     *
     * @return A list with the occlusion boxes
     */
    @Override
    public List<Cuboid> getOcclusionBoxes() {

        List<Cuboid> aabbs = new ArrayList<Cuboid>();

        if (rotation == EnumFacing.DOWN || rotation == EnumFacing.UP) {
            aabbs.add(new Cuboid(0, 4 / 16D, 0, 1, 12 / 16D, 1));
        } else if (rotation == EnumFacing.NORTH || rotation == EnumFacing.SOUTH) {
            aabbs.add(new Cuboid(0, 0, 4 / 16D, 1, 1, 12 / 16D));
        } else {
            aabbs.add(new Cuboid(4 / 16D, 0, 0, 12 / 16D, 1, 1));
        }
        return aabbs;
    }

    @Override
    public void update() {

        super.update();
        TubeLogic logic = getLogic();
        for (TubeStack stack : logic.tubeStacks) {
            PneumaticTube tube = getPartCache(stack.heading);
            if (tube instanceof MagTube && isPowered()) {
                stack.setSpeed(1);
            } else {
                stack.setSpeed(TubeStack.ITEM_SPEED);
            }
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

        super.writeToNBT(tag);
        tag.setByte("rotation", (byte) rotation.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);
        rotation = EnumFacing.getFront(tag.getByte("rotation"));
    }

    @Override
    public void writeUpdateData(MCByteBuf buffer) {
        super.writeUpdateData(buffer);
        buffer.writeInt(rotation.ordinal());
    }

    @Override
    public void readUpdateData(MCByteBuf buffer) {
        super.readUpdateData(buffer);
        rotation = EnumFacing.getFront(buffer.readInt());
    }

    @Override
    public boolean isConnected(EnumFacing dir, PneumaticTube otherTube) {

        if (dir == rotation || dir.getOpposite() == rotation) {
            return getWorld() == null
                    || !OcclusionHelper.occlusionTest(getWorld(), getPos(), sideBB);
        } else {
            return false;
        }
    }

    private boolean isPowered() {

        return true;// TODO implement powah!
    }

    // @Override
    // @SideOnly(Side.CLIENT)
    // protected TextureAtlasSprite getSideIcon(EnumFacing side) {
    //
    // return getPartCache(side) instanceof MagTube ? IconSupplier.magTubeSide : IconSupplier.pneumaticTubeSide;
    // }

    @Override
    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getSideIcon() {

        return IconSupplier.pneumaticTubeSide;
    }

    @Override
    public void renderDynamic(Vector3 loc, int pass, float frame) {
        super.renderDynamic(loc, pass, frame);
        if (pass == 0) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Tessellator t = Tessellator.getInstance();
            VertexBuffer b = t.getBuffer();
            
            GL11.glPushMatrix();
            GL11.glTranslatef((float) loc.x + 0.5F, (float) loc.y + 0.5F, (float) loc.z + 0.5F);
            if (rotation == EnumFacing.NORTH || rotation == EnumFacing.SOUTH) {
                GL11.glRotated(90, 1, 0, 0);
            } else if (rotation == EnumFacing.EAST || rotation == EnumFacing.WEST) {
                GL11.glRotated(90, 0, 0, 1);
            }
            GL11.glTranslatef((float) -loc.x - 0.5F, (float) -loc.y - 0.5F, (float) -loc.z - 0.5F);

            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            
            b.putColorRGB_F4(1, 1, 1);
            b.setTranslation((float) loc.x, (float) loc.y, (float) loc.z);

            TextureAtlasSprite icon = isPowered() ? IconSupplier.acceleratorFrontPowered : IconSupplier.acceleratorFront;

            double minX = icon.getInterpolatedU(0);
            double maxX = icon.getInterpolatedU(16);
            double minY = icon.getInterpolatedV(0);
            double maxY = icon.getInterpolatedV(16);

            b.normal(0, -1, 0);
            b.pos(0, 4 / 16D, 0).tex(maxX, maxY).endVertex();// minY
            b.pos(1, 4 / 16D, 0).tex(minX, maxY);
            b.pos(1, 4 / 16D, 1).tex(minX, minY);
            b.pos(0, 4 / 16D, 1).tex(maxX, minY);

            b.normal(0, 1, 1);
            b.pos(0, 12 / 16D, 0).tex(maxX, maxY);// maxY
            b.pos(0, 12 / 16D, 1).tex(minX, maxY);
            b.pos(1, 12 / 16D, 1).tex(minX, minY);
            b.pos(1, 12 / 16D, 0).tex(maxX, minY);

            icon = isPowered() ? IconSupplier.acceleratorSidePowered : IconSupplier.acceleratorSide;

            minX = icon.getInterpolatedU(4);
            maxX = icon.getInterpolatedU(12);
            minY = icon.getInterpolatedV(0);
            maxY = icon.getInterpolatedV(16);

            b.normal(0, 0, 1);
            b.pos(0, 4 / 16D, 1).tex(maxX, minY).endVertex();// maxZ
            b.pos(1, 4 / 16D, 1).tex(maxX, maxY).endVertex();
            b.pos(1, 12 / 16D, 1).tex(minX, maxY).endVertex();
            b.pos(0, 12 / 16D, 1).tex(minX, minY).endVertex();

            b.normal(0, 0, -1);
            b.pos(0, 4 / 16D, 0).tex(minX, maxY).endVertex();// minZ
            b.pos(0, 12 / 16D, 0).tex(maxX, maxY).endVertex();
            b.pos(1, 12 / 16D, 0).tex(maxX, minY).endVertex();
            b.pos(1, 4 / 16D, 0).tex(minX, minY).endVertex();

            b.normal(-1, 0, 0);
            b.pos(0, 4 / 16D, 0).tex(maxX, minY).endVertex();// minX
            b.pos(0, 4 / 16D, 1).tex(maxX, maxY).endVertex();
            b.pos(0, 12 / 16D, 1).tex(minX, maxY).endVertex();
            b.pos(0, 12 / 16D, 0).tex(minX, minY).endVertex();

            b.normal(1, 0, 0);
            b.pos(1, 4 / 16D, 0).tex(maxX, maxY).endVertex();// maxX
            b.pos(1, 12 / 16D, 0).tex(minX, maxY).endVertex();
            b.pos(1, 12 / 16D, 1).tex(minX, minY).endVertex();
            b.pos(1, 4 / 16D, 1).tex(maxX, minY).endVertex();

            icon = IconSupplier.acceleratorInside;

            minX = icon.getInterpolatedU(4);
            maxX = icon.getInterpolatedU(12);
            minY = icon.getInterpolatedV(4);
            maxY = icon.getInterpolatedV(12);

            b.pos(0, 4 / 16D, 6 / 16D).tex(minX, minY).endVertex();// inside maxZ
            b.pos(1, 4 / 16D, 6 / 16D).tex(maxX, maxY).endVertex();
            b.pos(1, 12 / 16D, 6 / 16D).tex(maxX, maxY).endVertex();
            b.pos(0, 12 / 16D, 6 / 16D).tex(minX, minY).endVertex();

            b.pos(0, 4 / 16D, 10 / 16D).tex(minX, maxY).endVertex();// inside minZ
            b.pos(0, 12 / 16D, 10 / 16D).tex(minX, minY).endVertex();
            b.pos(1, 12 / 16D, 10 / 16D).tex(maxX, minY).endVertex();
            b.pos(1, 4 / 16D, 10 / 16D).tex(maxX, maxY).endVertex();

            b.pos(10 / 16D, 4 / 16D, 0).tex(minX, minY).endVertex();// inside minX
            b.pos(10 / 16D, 4 / 16D, 1).tex(maxX, maxY).endVertex();
            b.pos(10 / 16D, 12 / 16D, 1).tex(maxX, maxY).endVertex();
            b.pos(10 / 16D, 12 / 16D, 0).tex(minX, minY).endVertex();

            b.pos(6 / 16D, 4 / 16D, 0).tex(minX, minY).endVertex();// inside maxX
            b.pos(6 / 16D, 12 / 16D, 0).tex(minX, maxY).endVertex();
            b.pos(6 / 16D, 12 / 16D, 1).tex(maxX, maxY).endVertex();
            b.pos(6 / 16D, 4 / 16D, 1).tex(maxX, minY).endVertex();

            b.setTranslation((float) -loc.x, (float) -loc.y, (float) -loc.z);
            t.draw();
            GL11.glPopMatrix();
        }

    }

    @Override
    protected boolean shouldRenderFully() {

        return true;
    }

    @Override
    public int getSlotMask() {
        return PartSlot.CENTER.mask;
    }
}
