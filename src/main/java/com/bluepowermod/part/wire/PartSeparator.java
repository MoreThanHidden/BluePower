package com.bluepowermod.part.wire;

import com.bluepowermod.api.gate.ic.IIntegratedCircuitPart;
import com.bluepowermod.part.BPPartFaceRotate;
import net.minecraft.util.EnumFacing;
import uk.co.qmunity.lib.client.RenderHelper;
import uk.co.qmunity.lib.client.render.RenderContext;
import uk.co.qmunity.lib.helper.OcclusionHelper;
import uk.co.qmunity.lib.model.IVertexConsumer;
import uk.co.qmunity.lib.part.IMicroblock;
import uk.co.qmunity.lib.part.MicroblockShape;
import uk.co.qmunity.lib.util.Dir;
import uk.co.qmunity.lib.vec.Cuboid;

import java.util.Arrays;
import java.util.List;

public class PartSeparator extends BPPartFaceRotate implements IIntegratedCircuitPart, IMicroblock {

    @Override
    public String getType() {

        return "separator";
    }

    @Override
    public String getUnlocalizedName() {

        return getType();
    }

    @Override
    public boolean canStay() {

        return true;
    }

    @Override
    public List<Cuboid> getOcclusionBoxes() {

        return Arrays.asList(OcclusionHelper.getEdgeMicroblockBox(getSize(), getFace(), Dir.getDirection(EnumFacing.NORTH, getFace(), 0)
                .toEnumFacing(getFace(), getRotation())));
    }

    @Override
    public List<Cuboid> getSelectionBoxes() {

        EnumFacing face = getFace();
        EnumFacing side = EnumFacing.NORTH;
        if (face == EnumFacing.NORTH)
            side = EnumFacing.UP;
        if (face == EnumFacing.SOUTH)
            side = EnumFacing.DOWN;

        return Arrays.asList(OcclusionHelper.getEdgeMicroblockBox(getSize(), face,
                Dir.getDirection(side, face, 0).toEnumFacing(face, getRotation())).expand(0.001));
    }

    @Override
    public boolean renderStatic(RenderContext context, IVertexConsumer consumer, int pass) {
        for (Cuboid c : getSelectionBoxes())
           RenderHelper.drawTesselatedTexturedCube(c);

        return true;
    }

    @Override
    public boolean canPlaceOnIntegratedCircuit() {

        return true;
    }

    @Override
    public MicroblockShape getShape() {
        return MicroblockShape.EDGE;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int getPosition() {
        return 0;
    }
}
