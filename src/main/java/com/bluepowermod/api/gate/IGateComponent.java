package com.bluepowermod.api.gate;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.qmunity.lib.client.RenderHelper;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.vec.Cuboid;
import uk.co.qmunity.lib.vec.Vector3;

import java.util.List;

public interface IGateComponent {

    public IGate getGate();

    public void addCollisionBoxes(List<Cuboid> boxes);

    public List<Cuboid> getOcclusionBoxes();

    public void tick();

    @SideOnly(Side.CLIENT)
    public void renderStatic(Vec3i translation, RenderHelper renderer, int pass);

    @SideOnly(Side.CLIENT)
    public void renderDynamic(Vector3 translation, int pass);

    public void onLayoutRefresh();

    public void writeToNBT(NBTTagCompound tag);

    public void readFromNBT(NBTTagCompound tag);

    public void writeData(MCByteBuf buffer);

    public void readData(MCByteBuf buffer);

    public boolean needsSyncing();
}
