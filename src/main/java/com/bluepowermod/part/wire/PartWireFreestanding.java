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
package com.bluepowermod.part.wire;

import com.bluepowermod.part.BPPart;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.qmunity.lib.client.RenderHelper;
import uk.co.qmunity.lib.client.render.RenderContext;
import uk.co.qmunity.lib.model.IVertexConsumer;
import uk.co.qmunity.lib.part.IThruHolePart;
import uk.co.qmunity.lib.vec.Cuboid;

import java.util.ArrayList;
import java.util.List;

public abstract class PartWireFreestanding extends BPPart implements IThruHolePart {

    protected abstract boolean shouldRenderConnection(EnumFacing side);

    protected abstract int getSize();

    protected int getColorMultiplier() {

        return 0xFFFFFF;
    }

    protected int getFrameColorMultiplier() {

        return 0xFFFFFF;
    }


    protected List<Cuboid> getFrameBoxes() {

        double wireSize = getSize() / 16D;
        double frameSeparation = 4 / 16D - (wireSize - 2 / 16D);
        double frameThickness = 1 / 16D;

        boolean isInWorld = getParent() != null;

        boolean down = shouldRenderConnection(EnumFacing.DOWN);
        boolean up = shouldRenderConnection(EnumFacing.UP);
        boolean north = shouldRenderConnection(EnumFacing.NORTH);
        boolean south = shouldRenderConnection(EnumFacing.SOUTH);
        boolean west = shouldRenderConnection(EnumFacing.WEST);
        boolean east = shouldRenderConnection(EnumFacing.EAST);

        return getFrameBoxes(wireSize, frameSeparation, frameThickness, down, up, west, east, north, south, isInWorld);
    }

    protected boolean shouldRenderFullFrame() {

        return false;
    }

    protected List<Cuboid> getFrameBoxes(double wireSize, double frameSeparation, double frameThickness, boolean down, boolean up,
            boolean west, boolean east, boolean north, boolean south, boolean isInWorld) {

        return getFrameBoxes(wireSize, frameSeparation, frameThickness, down, up, west, east, north, south, down, up, west, east, north,
                south, isInWorld);
    }

    protected List<Cuboid> getFrameBoxes(double wireSize, double frameSeparation, double frameThickness, boolean down, boolean up,
            boolean west, boolean east, boolean north, boolean south, boolean sideDown, boolean sideUp, boolean sideWest, boolean sideEast,
            boolean sideNorth, boolean sideSouth, boolean isInWorld) {

        List<Cuboid> boxes = new ArrayList<Cuboid>();

        // Top
        if (west == up || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 + ((wireSize + frameSeparation) / 2),
                    0.5 - ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2), 0.5
                            + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5 + ((wireSize + frameSeparation) / 2)));
        if (east == up || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2),
                    0.5 - ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5
                            + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5 + ((wireSize + frameSeparation) / 2)));
        if (south == up || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2),
                    0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2), 0.5
                            + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5 + ((wireSize + frameSeparation) / 2)
                            + frameThickness));
        if (north == up || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2), 0.5
                    - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2), 0.5
                    + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5 - ((wireSize + frameSeparation) / 2)));
        // Bottom
        if (west == down || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2)
                    - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2),
                    0.5 - ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2)));
        if (east == down || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2) - frameThickness,
                    0.5 - ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness,
                    0.5 - ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2)));
        if (south == down || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2) - frameThickness,
                    0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2),
                    0.5 - ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness));
        if (north == down || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2) - frameThickness,
                    0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2),
                    0.5 - ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2)));

        // Sides
        if (north == west || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2),
                    0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2),
                    0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2)));
        if (south == west || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2),
                    0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2),
                    0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness));
        if (north == east || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2), 0.5
                    - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness,
                    0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2)));
        if (south == east || !isInWorld || shouldRenderFullFrame())
            boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2),
                    0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness,
                    0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness));

        // Corners
        boxes.add(new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 + ((wireSize + frameSeparation) / 2), 0.5
                - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2), 0.5
                + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5 - ((wireSize + frameSeparation) / 2)));
        boxes.add(new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 + ((wireSize + frameSeparation) / 2),
                0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2), 0.5
                        + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness));
        boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2), 0.5
                - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5
                + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5 - ((wireSize + frameSeparation) / 2)));
        boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2),
                0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5
                        + ((wireSize + frameSeparation) / 2) + frameThickness, 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness));

        boxes.add(new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2)
                - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2),
                0.5 - ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2)));
        boxes.add(new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2)
                - frameThickness, 0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2),
                0.5 - ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness));
        boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5
                - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness,
                0.5 - ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2)));
        boxes.add(new Cuboid(0.5 + ((wireSize + frameSeparation) / 2), 0.5 - ((wireSize + frameSeparation) / 2) - frameThickness,
                0.5 + ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness,
                0.5 - ((wireSize + frameSeparation) / 2), 0.5 + ((wireSize + frameSeparation) / 2) + frameThickness));

        if (isInWorld) {
            // Connections
            Cuboid box = new Cuboid(0.5 - ((wireSize + frameSeparation) / 2) - frameThickness, 0, 0.5
                    - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2), 0.5
                    - ((wireSize + frameSeparation) / 2) - frameThickness, 0.5 - ((wireSize + frameSeparation) / 2));

            if (sideDown)
                for (int i = 0; i < 4; i++)
                    boxes.add(box.onFace(EnumFacing.DOWN));
            if (sideUp)
                for (int i = 0; i < 4; i++)
                    boxes.add(box.onFace(EnumFacing.UP));
            if (sideWest)
                for (int i = 0; i < 4; i++)
                    boxes.add(box.onFace(EnumFacing.WEST));
            if (sideEast)
                for (int i = 0; i < 4; i++)
                    boxes.add(box.onFace(EnumFacing.EAST));
            if (sideNorth)
                for (int i = 0; i < 4; i++)
                    boxes.add(box.onFace(EnumFacing.NORTH));
            if (sideSouth)
                for (int i = 0; i < 4; i++)
                    boxes.add(box.onFace(EnumFacing.SOUTH));
        }

        return boxes;
    }

    protected void renderFrame(double wireSize, double frameSeparation, double frameThickness, boolean down,
                               boolean up, boolean west, boolean east, boolean north, boolean south, boolean sideDown, boolean sideUp, boolean sideWest,
                               boolean sideEast, boolean sideNorth, boolean sideSouth, boolean isInWorld, TextureAtlasSprite texture, int color) {

        Tessellator t = Tessellator.getInstance();
        VertexBuffer buffer = t.getBuffer();
        buffer.putColor4(color);

        for (Cuboid box : getFrameBoxes(wireSize, frameSeparation, frameThickness, down, up, west, east, north, south, sideDown, sideUp,
                sideWest, sideEast, sideNorth, sideSouth, isInWorld))
            RenderHelper.drawTesselatedTexturedCube(box);

        buffer.color(255,255,255,1);
    }

    protected void renderFrame(double wireSize, double frameSeparation, double frameThickness, boolean down,
            boolean up, boolean west, boolean east, boolean north, boolean south, boolean isInWorld, TextureAtlasSprite texture, int color) {

        renderFrame(wireSize, frameSeparation, frameThickness, down, up, west, east, north, south, down, up, west, east, north,
                south, isInWorld, texture, color);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderStatic(RenderContext context, IVertexConsumer consumer, int pass) {
        double wireSize = getSize() / 16D;
        double frameSeparation = 4 / 16D - (wireSize - 2 / 16D);
        double frameThickness = 1 / 16D;

        int color = getColorMultiplier();

        boolean isInWorld = getParent() != null && getWorld() != null;

        boolean down = shouldRenderConnection(EnumFacing.DOWN);
        boolean up = shouldRenderConnection(EnumFacing.UP);
        boolean north = shouldRenderConnection(EnumFacing.NORTH);
        boolean south = shouldRenderConnection(EnumFacing.SOUTH);
        boolean west = shouldRenderConnection(EnumFacing.WEST);
        boolean east = shouldRenderConnection(EnumFacing.EAST);

        Tessellator t = Tessellator.getInstance();
        VertexBuffer buffer = t.getBuffer();
        buffer.putColor4(color);

        // Wire

        RenderHelper.drawTesselatedTexturedCube(new Cuboid(0.5 - (wireSize / 2), 0.5 - (wireSize / 2), 0.5 - (wireSize / 2), 0.5 + (wireSize / 2),
                0.5 + (wireSize / 2), 0.5 + (wireSize / 2)));
        if (up || !isInWorld)
           RenderHelper.drawTesselatedTexturedCube(new Cuboid(0.5 - (wireSize / 2), 0.5 + (wireSize / 2), 0.5 - (wireSize / 2), 0.5 + (wireSize / 2), 1,
                    0.5 + (wireSize / 2)));
        if (down || !isInWorld)
            RenderHelper.drawTesselatedTexturedCube(new Cuboid(0.5 - (wireSize / 2), 0, 0.5 - (wireSize / 2), 0.5 + (wireSize / 2), 0.5 - (wireSize / 2),
                    0.5 + (wireSize / 2)));
        if (north || !isInWorld)
            RenderHelper.drawTesselatedTexturedCube(new Cuboid(0.5 - (wireSize / 2), 0.5 - (wireSize / 2), 0, 0.5 + (wireSize / 2), 0.5 + (wireSize / 2),
                    0.5 - (wireSize / 2)));
        if (south || !isInWorld)
            RenderHelper.drawTesselatedTexturedCube(new Cuboid(0.5 - (wireSize / 2), 0.5 - (wireSize / 2), 0.5 + (wireSize / 2), 0.5 + (wireSize / 2),
                    0.5 + (wireSize / 2), 1));
        if (west || !isInWorld)
            RenderHelper.drawTesselatedTexturedCube(new Cuboid(0, 0.5 - (wireSize / 2), 0.5 - (wireSize / 2), 0.5 - (wireSize / 2), 0.5 + (wireSize / 2),
                    0.5 + (wireSize / 2)));
        if (east || !isInWorld)
            RenderHelper.drawTesselatedTexturedCube(new Cuboid(0.5 + (wireSize / 2), 0.5 - (wireSize / 2), 0.5 - (wireSize / 2), 1, 0.5 + (wireSize / 2),
                    0.5 + (wireSize / 2)));

        buffer.putColor4(getFrameColorMultiplier());

        // Frame
        renderFrame(wireSize, frameSeparation, frameThickness, down, up, west, east, north, south, isInWorld, getFrameIcon(),
                getFrameColorMultiplier());

        return true;
    }

    @Override
    public int getHollowSize(EnumFacing side) {

        return 8;
    }

    @SideOnly(Side.CLIENT)
    protected abstract TextureAtlasSprite getWireIcon(EnumFacing side);

    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getWireIcon(EnumFacing side, EnumFacing face) {

        return getWireIcon(face);
    }

    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite[] getIcons(EnumFacing side) {

        return new TextureAtlasSprite[] { getWireIcon(side, EnumFacing.DOWN), getWireIcon(side, EnumFacing.UP),
                getWireIcon(side, EnumFacing.WEST), getWireIcon(side, EnumFacing.EAST), getWireIcon(side, EnumFacing.NORTH),
                getWireIcon(side, EnumFacing.SOUTH) };
    }

    @SideOnly(Side.CLIENT)
    protected abstract TextureAtlasSprite getFrameIcon();


}
