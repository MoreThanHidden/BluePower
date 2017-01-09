package com.bluepowermod.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class GuiRenderHelper {

    private static TextureAtlasSprite icon = null;

    public static void setIcon(TextureAtlasSprite icon) {

        GuiRenderHelper.icon = icon;
    }

    public static TextureAtlasSprite getIcon() {

        return icon;
    }

    private static double getU(double u) {

        return icon == null ? u : icon.getInterpolatedU(u * 16);
    }

    private static double getV(double v) {

        return icon == null ? v : icon.getInterpolatedV(v * 16);
    }

    public static void renderTexturedBox(double x, double y, double width, double height, double z, double u1, double v1, double u2, double v2) {

        renderTexturedBox(x, y, width, height, z, u1, v1, u2, v2, 0xFFFFFF);
    }

    public static void renderTexturedBox(double x, double y, double width, double height, double z, double u1, double v1, double u2, double v2,
            int color) {

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.putColor4(color);
        buffer.pos(x, y + height, z).tex(getU(u1), getV(v2));
        buffer.pos(x + width, y + height, z).tex(getU(u2), getV(v2));
        buffer.pos(x + width, y, z).tex(getU(u2), getV(v1));
        buffer.pos(x, y, z).tex(getU(u1), getV(v1));
        tessellator.draw();
    }
}
