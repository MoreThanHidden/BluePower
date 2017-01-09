/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.part.lamp;

import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.connect.IConnection;
import com.bluepowermod.api.connect.IConnectionCache;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import com.bluepowermod.init.BPCreativeTabs;
import com.bluepowermod.part.BPPartFace;
import com.bluepowermod.part.tube.PneumaticTube;
import com.bluepowermod.part.wire.redstone.PartRedwireFreestanding;
import com.bluepowermod.redstone.RedstoneApi;
import com.bluepowermod.redstone.RedstoneConnectionCache;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.qmunity.lib.client.RenderHelper;
import uk.co.qmunity.lib.client.render.RenderContext;
import uk.co.qmunity.lib.helper.MathHelper;
import uk.co.qmunity.lib.helper.OcclusionHelper;
import uk.co.qmunity.lib.helper.RedstoneHelper;
import uk.co.qmunity.lib.model.IVertexConsumer;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.part.IRedstonePart;
import uk.co.qmunity.lib.part.MicroblockShape;
import uk.co.qmunity.lib.part.MultipartCompat;
import uk.co.qmunity.lib.transform.Rotation;
import uk.co.qmunity.lib.util.MinecraftColor;
import uk.co.qmunity.lib.vec.Cuboid;
import uk.co.qmunity.lib.vec.Vector3;

import java.util.Arrays;
import java.util.List;



/**
 * Base class for the lamps that are multiparts.
 *
 * @author Koen Beckers (K4Unl), Amadornes
 *
 */
public abstract class PartLamp extends BPPartFace implements IRedstonePart, IRedstoneDevice {

    protected final MinecraftColor color;
    protected final boolean inverted;

    protected byte power = 0;
    private byte[] input = new byte[6];

    private RedstoneConnectionCache connections = RedstoneApi.getInstance().createRedstoneConnectionCache(this);

    /**
     * @author amadornes
     * @param color
     * @param inverted
     */
    public PartLamp(MinecraftColor color, Boolean inverted) {

        this.color = color;
        this.inverted = inverted;
    }

    @Override
    public String getType() {

        return getLampType() + "." + color.name().toLowerCase() + (inverted ? ".inverted" : "");
    }

    protected abstract String getLampType();

    /**
     * @author amadornes
     */
    @Override
    public String getUnlocalizedName() {

        return getType();
    }

    /**
     * @author amadornes
     */
    @Override
    public List<Cuboid> getOcclusionBoxes() {

        return getSelectionBoxes();
    }

    /**
     * @author amadornes
     */

    @Override
    public List<Cuboid> getSelectionBoxes() {

        return Arrays.asList(new Cuboid(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }


    /**
     * Code to render the actual lamp portion of the lamp. Will be colored
     *
     * @author Koen Beckers (K4Unl)
     * @param pass
     *            The pass that is rendered now. Pass 1 for solids. Pass 2 for transparents
     */
    @SideOnly(Side.CLIENT)
    public void renderGlow(int pass) {

    }

    @Override
    public int getLightValue() {

        int pow = (inverted ? 15 - power : power);

        if (Loader.isModLoaded("coloredlightscore")) {
            int color = this.color.getHex();

            int ri = (color >> 16) & 0xFF;
            int gi = (color >> 8) & 0xFF;
            int bi = (color >> 0) & 0xFF;

            float r = ri / 256F;
            float g = gi / 256F;
            float b = bi / 256F;

            // Clamp color channels
            if (r < 0.0f)
                r = 0.0f;
            else if (r > 1.0f)
                r = 1.0f;

            if (g < 0.0f)
                g = 0.0f;
            else if (g > 1.0f)
                g = 1.0f;

            if (b < 0.0f)
                b = 0.0f;
            else if (b > 1.0f)
                b = 1.0f;

            return pow | ((((int) (15.0F * b)) << 15) + (((int) (15.0F * g)) << 10) + (((int) (15.0F * r)) << 5));
        }

        return pow;
    }

    @Override
    public void onAdded() {

        super.onAdded();

        connections.recalculateConnections();

        onUpdate();
    }

    /**
     * @author amadornes
     */
    @Override
    public void onUpdate() {

        recalculatePower();
    }

    private void recalculatePower() {

        if (getWorld().isRemote)
            return;

        int old = power;

        int pow = 0;
        for (EnumFacing d : EnumFacing.VALUES) {
            IConnection<IRedstoneDevice> con = connections.getConnectionOnSide(d);
            if (con != null) {
                pow = Math.max(pow, input[d.ordinal()] & 0xFF);
            } else {
                pow = Math.max(pow, MathHelper.map(RedstoneHelper.getWeakRedstoneInput(getWorld(), getPos(), d), 0, 15, 0, 255));
            }
        }
        power = (byte) pow;

        if (old != power)
            sendUpdatePacket();
    }

    @Override
    public int getStrongPower(EnumFacing side) {

        return 0;
    }

    @Override
    public int getWeakPower(EnumFacing side) {

        return 0;
    }

    @Override
    public boolean canConnectRedstone(EnumFacing side) {

        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

        super.writeToNBT(tag);
        tag.setByte("power", power);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);
        power = tag.getByte("power");
    }

    @Override
    public void writeUpdateData(MCByteBuf buffer) {
        super.writeUpdateData(buffer);
        buffer.writeByte(power);
    }

    @Override
    public void readUpdateData(MCByteBuf buffer) {
        super.readUpdateData(buffer);
        power = buffer.readByte();

        if (getParent() != null && getWorld() != null)
            getWorld().checkLightFor(EnumSkyBlock.BLOCK, getPos());
    }

    @Override
    public boolean canStay() {

        BlockPos loc = this.getPos().offset(getFace());

        if (MultipartCompat.getHolder(getWorld(), loc) != null) {
            if (MultipartCompat.getPart(getWorld(), loc, PartRedwireFreestanding.class) != null)
                return true;
            PneumaticTube t = (PneumaticTube)MultipartCompat.getPart(getWorld(), loc, PneumaticTube.class);
            if (t != null && t.getRedwireType() != null)
                return true;
        }

        return super.canStay();
    }

    /**
     * @author amadornes
     */
    @Override
    public CreativeTabs getCreativeTab() {

        return BPCreativeTabs.lighting;
    }

    @Override
    public boolean canConnect(EnumFacing side, IRedstoneDevice dev, ConnectionType type) {

        if (side == null)
            return false;

        if (!OcclusionHelper.microblockOcclusionTest(getParent(), MicroblockShape.EDGE, 1, getFace(), side))
            return false;

        return true;
    }

    @Override
    public IConnectionCache<? extends IRedstoneDevice> getRedstoneConnectionCache() {

        return connections;
    }

    @Override
    public byte getRedstonePower(EnumFacing side) {

        return 0;
    }

    @Override
    public void setRedstonePower(EnumFacing side, byte power) {

        input[side.ordinal()] = power;
    }

    @Override
    public void onRedstoneUpdate() {

        recalculatePower();
    }

    @Override
    public boolean isNormalFace(EnumFacing side) {

        return false;
    }

}
