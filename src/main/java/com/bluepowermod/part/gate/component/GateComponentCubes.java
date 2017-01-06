package com.bluepowermod.part.gate.component;

import com.bluepowermod.part.gate.GateBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.qmunity.lib.client.render.RenderHelper;
import uk.co.qmunity.lib.transform.Scale;
import uk.co.qmunity.lib.transform.Transformation;
import uk.co.qmunity.lib.vec.Vec2dRect;
import uk.co.qmunity.lib.vec.Vec3dCube;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class GateComponentCubes extends GateComponent {

    protected int layoutColor = -1;

    private List<Vec3dCube> cubes = new ArrayList<Vec3dCube>();

    public GateComponentCubes(GateBase gate, int color) {

        super(gate);

        layoutColor = color;
        // if(gate.isExamplePart())
        onLayoutRefresh();
    }

    protected List<Vec3dCube> getCubes() {

        // GateBase ex = getGate().getExample();
        //
        // if (ex == getGate())
        // return cubes;

        return cubes;
    }

    @Override
    public void onLayoutRefresh() {

        if (layoutColor == -1)
            return;

        cubes.clear();

        double height = getHeight();
        double scale = 1D / getGate().getLayout().getLayout(layoutColor).getWidth();

        Transformation transformation = new Scale(scale, 1, scale);
        for (Rectangle r : getGate().getLayout().getSimplifiedLayout(layoutColor).getRectangles())
            cubes.add(new Vec2dRect(r).extrude(height).transform(transformation).add(-0.5 + 1 / 64D, 2 / 16D, -0.5 + 1 / 64D));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(Vec3i translation, RenderHelper renderer, int pass) {

        renderer.setColor(getColor());
        renderer.setRenderSides(false, true, true, true, true, true);

        for (Vec3dCube c : getCubes())
            renderer.renderBox(c, getIcon());

        renderer.resetRenderedSides();
        renderer.setColor(0xFFFFFF);
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIcon() {

        return null;
    }

    public int getColor() {

        return 0xFFFFFF;
    }

    public double getHeight() {

        return 0;
    }

}
