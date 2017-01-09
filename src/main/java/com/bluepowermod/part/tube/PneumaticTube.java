/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.part.tube;

import com.bluepowermod.BluePower;
import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.connect.IConnection;
import com.bluepowermod.api.connect.IConnectionCache;
import com.bluepowermod.api.connect.IConnectionListener;
import com.bluepowermod.api.misc.IFace;
import com.bluepowermod.api.misc.IScrewdriver;
import com.bluepowermod.api.tube.IPneumaticTube.TubeColor;
import com.bluepowermod.api.tube.ITubeConnection;
import com.bluepowermod.api.wire.redstone.IRedstoneConductor;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import com.bluepowermod.api.wire.redstone.IRedwire;
import com.bluepowermod.api.wire.redstone.RedwireType;
import com.bluepowermod.client.render.IconSupplier;
import com.bluepowermod.helper.IOHelper;
import com.bluepowermod.helper.PartCache;
import com.bluepowermod.helper.TileEntityCache;
import com.bluepowermod.init.BPCreativeTabs;
import com.bluepowermod.init.BPItems;
import com.bluepowermod.init.Config;
import com.bluepowermod.item.ItemDamageableColorableOverlay;
import com.bluepowermod.item.ItemPart;
import com.bluepowermod.part.BPPart;
import com.bluepowermod.part.PartManager;
import com.bluepowermod.part.wire.PartWireFreestanding;
import com.bluepowermod.part.wire.redstone.PartRedwireFace.PartRedwireFaceUninsulated;
import com.bluepowermod.part.wire.redstone.WireHelper;
import com.bluepowermod.redstone.DummyRedstoneDevice;
import com.bluepowermod.redstone.RedstoneApi;
import com.bluepowermod.redstone.RedstoneConnectionCache;
import com.bluepowermod.util.Color;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.qmunity.lib.client.render.RenderContext;
import uk.co.qmunity.lib.helper.MathHelper;
import uk.co.qmunity.lib.helper.OcclusionHelper;
import uk.co.qmunity.lib.helper.RedstoneHelper;
import uk.co.qmunity.lib.model.IVertexConsumer;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.part.IRedstonePart;
import uk.co.qmunity.lib.part.IThruHolePart;
import uk.co.qmunity.lib.part.MicroblockShape;
import uk.co.qmunity.lib.raytrace.QRayTraceResult;
import uk.co.qmunity.lib.raytrace.RayTracer;
import uk.co.qmunity.lib.transform.Rotation;
import uk.co.qmunity.lib.util.MinecraftColor;
import uk.co.qmunity.lib.vec.Cuboid;
import uk.co.qmunity.lib.vec.Vector3;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author MineMaarten
 */

public class PneumaticTube extends PartWireFreestanding implements IThruHolePart, IRedstonePart, IRedstoneConductor,
        IConnectionListener, IRedwire {

    public final boolean[] connections = new boolean[6];
    public final boolean[] redstoneConnections = new boolean[6];
    /**
     * true when != 2 connections, when this is true the logic doesn't have to 'think' which way an item should go.
     */
    public boolean isCrossOver;
    protected final Cuboid sideBB = new Cuboid(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.25, 0.75));
    private TileEntityCache tileCache;
    private PartCache<PneumaticTube> partCache;
    protected final TubeColor[] color = { TubeColor.NONE, TubeColor.NONE, TubeColor.NONE, TubeColor.NONE, TubeColor.NONE, TubeColor.NONE };
    private final TubeLogic logic = new TubeLogic(this);
    public boolean initialized; // workaround to the connections not properly initialized, but being tried to be used.
    private int tick;

    private RedwireType redwireType = null;

    @Override
    public String getType() {

        return "pneumaticTube";
    }

    @Override
    public String getUnlocalizedName() {

        return "pneumaticTube";
    }

    /**
     * Gets all the selection boxes for this block
     *
     * @return A list with the selection boxes
     */
    @Override
    public List<Cuboid> getSelectionBoxes() {

        return getTubeBoxes();
    }

    protected List<Cuboid> getTubeBoxes() {

        List<Cuboid> aabbs = getOcclusionBoxes();
        for (int i = 0; i < 6; i++) {
            EnumFacing d = EnumFacing.getFront(i);
            if (connections[i] || redstoneConnections[i] || getDeviceOnSide(d) != null) {
                aabbs.add(sideBB);
            }
        }
        return aabbs;
    }

    /**
     * Gets all the occlusion boxes for this block
     *
     * @return A list with the occlusion boxes
     */
    @Override
    public List<Cuboid> getOcclusionBoxes() {

        List<Cuboid> aabbs = new ArrayList<Cuboid>();
        aabbs.add(new Cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75));
        return aabbs;
    }

    @Override
    public void update() {

        if (initialized)
            logic.update();
        if (tick++ == 3) {
            clearCache();
            updateConnections();
        } else if (tick == 40) {
            sendUpdatePacket();
        }
        if (getWorld().isRemote && tick % 40 == 0)
            clearCache();// reset on the client, as it doesn't get update on neighbor block updates (as the
        // method isn't called on the client)
    }

    /**
     * Event called whenever a nearby block updates
     */
    @Override
    public void onUpdate() {

        if (getParent() != null && getWorld() != null) {

            // Redstone update

            // Don't to anything if propagation-related stuff is going on
            if (RedstoneApi.getInstance().shouldWiresHandleUpdates()) {
                getRedstoneConnectionCache().recalculateConnections();

                EnumFacing d = null;
                for (EnumFacing dir : EnumFacing.VALUES)
                    if (getDeviceOnSide(dir) != null)
                        d = dir;
                if (d != null) {
                    RedstoneApi.getInstance().getRedstonePropagator(this, d).propagate();
                }

                sendUpdatePacket();
            }

            // Cache and connection refresh
            clearCache();
            updateConnections();
        }
    }

    public TileEntity getTileCache(EnumFacing d) {

        if (tileCache == null) {
            tileCache = new TileEntityCache(getWorld(), getPos());
        }
        return tileCache.getValue(d);
    }

    public PneumaticTube getPartCache(EnumFacing d) {

        if (partCache == null) {
            partCache = new PartCache<PneumaticTube>(getWorld(), getPos(), PneumaticTube.class);
        }
        return partCache.getValue(d);
    }

    public void clearCache() {

        tileCache = null;
        partCache = null;
    }

    public TubeLogic getLogic() {

        return logic;
    }

    protected boolean canConnectToInventories() {

        return true;
    }

    private void updateConnections() {

        if (getWorld() != null && !getWorld().isRemote) {
            int connectionCount = 0;
            boolean clearedCache = false;
            clearCache();
            for (int i = 0; i < 6; i++) {
                boolean oldState = connections[i];
                EnumFacing d = EnumFacing.getFront(i);
                TileEntity neighbor = getTileCache(d);
                connections[i] = IOHelper.canInterfaceWith(neighbor, d.getOpposite(), this, canConnectToInventories());

                if (!connections[i])
                    connections[i] = neighbor instanceof ITubeConnection && ((ITubeConnection) neighbor).isConnectedTo(d.getOpposite());
                if (connections[i]) {
                    connections[i] = isConnected(d, null);
                }
                if (connections[i])
                    connectionCount++;
                if (!clearedCache && oldState != connections[i]) {
                    if (Config.enableTubeCaching)
                        getLogic().clearNodeCaches();
                    clearedCache = true;
                }
            }
            isCrossOver = connectionCount != 2;
            sendUpdatePacket();
        }
        initialized = true;
    }

    public boolean isConnected(EnumFacing dir, PneumaticTube otherTube) {

        if (otherTube != null) {
            if (!(this instanceof Accelerator) && this instanceof MagTube != otherTube instanceof MagTube && !(otherTube instanceof Accelerator))
                return false;
            TubeColor otherTubeColor = otherTube.getColor(dir.getOpposite());
            if (otherTubeColor != TubeColor.NONE && getColor(dir) != TubeColor.NONE && getColor(dir) != otherTubeColor)
                return false;
        }
        return getWorld() == null || OcclusionHelper.microblockOcclusionTest(getParent(), true, MicroblockShape.FACE_HOLLOW, 8, dir);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

        super.writeToNBT(tag);

        for (int i = 0; i < 6; i++) {
            tag.setBoolean("connections" + i, connections[i]);
            tag.setBoolean("redstoneConnections" + i, getDeviceOnSide(EnumFacing.getFront(i)) != null);
        }
        for (int i = 0; i < color.length; i++)
            tag.setByte("tubeColor" + i, (byte) color[i].ordinal());

        if (redwireType != null)
            tag.setInteger("wireType", redwireType.ordinal());
        tag.setByte("power", getPower());

        NBTTagCompound logicTag = new NBTTagCompound();
        logic.writeToNBT(logicTag);
        tag.setTag("logic", logicTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);

        int connectionCount = 0;
        for (int i = 0; i < 6; i++) {
            connections[i] = tag.getBoolean("connections" + i);
            redstoneConnections[i] = tag.getBoolean("redstoneConnections" + i);
            if (connections[i])
                connectionCount++;
        }
        isCrossOver = connectionCount != 2;
        for (int i = 0; i < color.length; i++)
            color[i] = TubeColor.values()[tag.getByte("tubeColor" + i)];

        if (tag.hasKey("wireType"))
            redwireType = RedwireType.values()[tag.getInteger("wireType")];
        else
            redwireType = null;
        setRedstonePower(null, tag.getByte("power"));

        if (getParent() != null && getWorld() != null)
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());

        NBTTagCompound logicTag = tag.getCompoundTag("logic");
        logic.readFromNBT(logicTag);
    }

    @Override
    public void writeUpdateData(MCByteBuf buffer) {
        super.writeUpdateData(buffer);

        // Connections
        for (int i = 0; i < 6; i++)
            buffer.writeBoolean(connections[i]);
        for (int i = 0; i < 6; i++)
            buffer.writeBoolean(getDeviceOnSide(EnumFacing.getFront(i)) != null);

        // Colors
        for (int i = 0; i < color.length; i++)
            buffer.writeInt(color[i].ordinal());

        // Redwire
        if (redwireType != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(redwireType.ordinal());
            buffer.writeByte(getPower());
        } else {
            buffer.writeBoolean(false);
        }

        // Logic
        logic.writeData(buffer);
    }

    @Override
    public void readUpdateData(MCByteBuf buffer) {
        super.readUpdateData(buffer);

        // Connections
        for (int i = 0; i < 6; i++)
            connections[i] = buffer.readBoolean();
        for (int i = 0; i < 6; i++)
            redstoneConnections[i] = buffer.readBoolean();

        int connectionCount = 0;
        for (int i = 0; i < 6; i++)
            if (connections[i] || redstoneConnections[i])
                connectionCount++;
        isCrossOver = connectionCount != 2;

        // Colors
        for (int i = 0; i < color.length; i++)
            color[i] = TubeColor.values()[buffer.readInt()];

        // Redwire
        if (buffer.readBoolean()) {
            redwireType = RedwireType.values()[buffer.readInt()];
            setRedstonePower(null, buffer.readByte());
        } else {
            redwireType = null;
        }

        // Logic
        logic.readData(buffer);

        // Render update
        if (getParent() != null && getWorld() != null)
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    /**
     * Event called when the part is activated (right clicked)
     *
     * @param player
     *            Player that right clicked the part
     * @param item
     *            Item that was used to click it
     * @return Whether or not an action occurred
     */
    @Override
    public boolean onActivated(EntityPlayer player, QRayTraceResult mop, ItemStack item) {

        if (getWorld() == null)
            return false;

        if (item != null) {
            TubeColor newColor = null;
            if (item.getItem() == BPItems.paint_brush && ((ItemDamageableColorableOverlay) BPItems.paint_brush).tryUseItem(item)) {
                newColor = TubeColor.values()[item.getItemDamage()];
            } else if (item.getItem() == Items.WATER_BUCKET || (item.getItem() == BPItems.paint_brush && item.getItemDamage() == 16)) {
                newColor = TubeColor.NONE;
            }
            if (newColor != null) {
                if (!getWorld().isRemote) {
                    List<Cuboid> boxes = getTubeBoxes();
                    Cuboid box = mop.cube;
                    int face = -1;
                    if (box.equals(boxes.get(0))) {
                        face = mop.sideHit.ordinal();
                    } else {
                        face = getSideFromAABBIndex(boxes.indexOf(box));
                    }
                    color[face] = newColor;
                    updateConnections();
                    getLogic().clearNodeCaches();
                    notifyUpdate();
                }
                return true;
            }

            if (item.getItem() instanceof ItemPart) {
                BPPart part = PartManager.getExample(item);
                if (redwireType == null && part instanceof PartRedwireFaceUninsulated) {
                    if (!getWorld().isRemote) {
                        redwireType = ((IRedwire) part).getRedwireType(null);
                        if (!player.capabilities.isCreativeMode)
                            item.setCount(item.getCount() - 1);

                        // Redstone update
                        getRedstoneConnectionCache().recalculateConnections();
                        RedstoneApi.getInstance().getRedstonePropagator(this, EnumFacing.DOWN).propagate();

                        updateConnections();
                        getLogic().clearNodeCaches();
                        notifyUpdate();
                        sendUpdatePacket();
                    }
                    return true;
                }
            }
            // Removing redwire
            if (redwireType != null && item.getItem() instanceof IScrewdriver && player.isSneaking()) {
                if (!getWorld().isRemote) {
                    IOHelper.spawnItemInWorld(getWorld(), PartManager.getPartInfo("wire." + redwireType.getName()).getStack(), getPos().getX() + 0.5,
                            getPos().getY() + 0.5, getPos().getZ() + 0.5);
                    redwireType = null;

                    // Redstone update
                    getRedstoneConnectionCache().recalculateConnections();
                    RedstoneApi.getInstance().getRedstonePropagator(this, EnumFacing.DOWN).propagate();

                    ((IScrewdriver) item.getItem()).damage(item, 1, player, false);

                    updateConnections();
                    getLogic().clearNodeCaches();
                    notifyUpdate();
                    sendUpdatePacket();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getDrops() {

        List<ItemStack> drops = super.getDrops();
        for (TubeStack stack : logic.tubeStacks) {
            drops.add(stack.stack);
        }
        return drops;
    }

    /**
     * How 'dense' the tube is to the pathfinding algorithm. Is altered in the RestrictionTube
     *
     * @return
     */
    public int getWeight() {

        return 1;
    }

    public TubeColor getColor(EnumFacing dir) {

        return color[dir.ordinal()];
    }

    private double getAddedThickness() {

        return 0;// 0.125 / 16D;
    }

    protected boolean shouldRenderFully() {

        boolean renderFully = false;
        int count = 0;

        for (int i = 0; i < 6; i++) {
            if (shouldRenderConnection(EnumFacing.getFront(i)))
                count++;
            if (i % 2 == 0 && connections[i] != connections[i + 1])
                renderFully = true;
        }

        renderFully |= count > 2 || count < 2;
        renderFully |= getParent() == null || getWorld() == null;

        return renderFully;
    }

    protected void renderSide() {

    }

    /**
     * Hacky method to get the right side
     *
     * @return
     */
    private int getSideFromAABBIndex(int index) {

        int curIndex = 0;
        for (int side = 0; side < 6; side++) {
            if (connections[side]) {
                curIndex++;
                if (index == curIndex)
                    return side;
            }
        }
        return 0;
    }

    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getSideIcon() {

        return IconSupplier.pneumaticTubeSide;
    }

    /**
     * Adds information to the waila tooltip
     *
     * @author amadornes
     *
     * @param info
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void addWAILABody(List<String> info) {

        boolean addTooltip = false;
        for (TubeColor col : color) {
            if (col != TubeColor.NONE) {
                addTooltip = true;
                break;
            }
        }
        if (addTooltip) {
            info.add(Color.YELLOW + I18n.format("waila.bluepower:pneumaticTube.color"));
            for (int i = 0; i < 6; i++) {
                if (color[i] != TubeColor.NONE) {
                    if (color[i] != TubeColor.NONE)
                        info.add(TextFormatting.DARK_AQUA
                                + I18n.format("bluepower:face." + EnumFacing.getFront(i).toString().toLowerCase()) + ": "
                                + TextFormatting.WHITE + I18n.format("bluepower:color." + ItemDye.DYE_COLORS[color[i].ordinal()]));
                }
            }
        }
    }

    @Override
    public CreativeTabs getCreativeTab() {

        return BPCreativeTabs.machines;
    }

    @Override
    public int getHollowSize(EnumFacing side) {

        return 8;
    }

    @Override
    protected boolean shouldRenderConnection(EnumFacing side) {

        return connections[side.ordinal()] || redstoneConnections[side.ordinal()];
    }

    @Override
    protected int getSize() {

        return 0;
    }

    @Override
    protected TextureAtlasSprite getWireIcon(EnumFacing side) {

        return null;
    }

    @Override
    protected TextureAtlasSprite getFrameIcon() {

        return getSideIcon();
    }

    public RedwireType getRedwireType() {

        return redwireType;
    }

    @Override
    public QRayTraceResult rayTrace(Vec3d start, Vec3d end) {

        QRayTraceResult mop = super.rayTrace(start, end);
        if (mop == null)
            return null;

        EntityPlayer player = BluePower.proxy.getPlayer();
        if (redwireType != null && player != null && player.isSneaking()) {
            double wireSize = getSize() / 16D;
            double frameSeparation = 4 / 16D - (wireSize - 2 / 16D);
            double frameThickness = 1 / 16D;
            frameThickness /= 1.5;
            frameSeparation -= 1 / 32D;

            QRayTraceResult wire = RayTracer.instance().rayTraceCuboids(new Vector3(start), new Vector3(end), getFrameBoxes(wireSize, frameSeparation, frameThickness, shouldRenderConnection(EnumFacing.DOWN),
                    shouldRenderConnection(EnumFacing.UP), shouldRenderConnection(EnumFacing.WEST),
                    shouldRenderConnection(EnumFacing.EAST), shouldRenderConnection(EnumFacing.NORTH),
                    shouldRenderConnection(EnumFacing.SOUTH), redstoneConnections[EnumFacing.DOWN.ordinal()],
                    redstoneConnections[EnumFacing.UP.ordinal()], redstoneConnections[EnumFacing.WEST.ordinal()],
                    redstoneConnections[EnumFacing.EAST.ordinal()], redstoneConnections[EnumFacing.NORTH.ordinal()],
                    redstoneConnections[EnumFacing.SOUTH.ordinal()], getParent() != null && getWorld() != null));
            QRayTraceResult frame = RayTracer.instance().rayTraceCuboids(new Vector3(start), new Vector3(end), getFrameBoxes());

            if (wire != null) {
                if (frame != null) {
                    if (wire.hitVec.distanceTo(start) < frame.hitVec.distanceTo(start))
                        mop.hitInfo = PartManager.getPartInfo("wire." + redwireType.getName()).getStack();
                } else {
                    mop.hitInfo = PartManager.getPartInfo("wire." + redwireType.getName()).getStack();
                }
            }
        }

        return mop;
    }

    @Override
    public ItemStack getPickBlock(EntityPlayer player, QRayTraceResult hit) {
        Object o = hit.hitInfo;
        if (o != null && o instanceof ItemStack)
            return (ItemStack) o;

        return super.getPickBlock(player, hit);
    }

    @Override
    public int getStrongPower(EnumFacing side) {

        return 0;
    }

    @Override
    public int getWeakPower(EnumFacing side) {

        if (getRedwireType() == null)
            return 0;

        return MathHelper.map(getPower() & 0xFF, 0, 255, 0, 15);
    }

    @Override
    public boolean canConnectRedstone(EnumFacing side) {

        if (getRedwireType() == null)
            return false;

        return true;
    }

    private RedstoneConnectionCache redConnections = RedstoneApi.getInstance().createRedstoneConnectionCache(this);

    private byte power = 0;

    @Override
    public boolean canConnect(EnumFacing side, IRedstoneDevice device, ConnectionType type) {

        if (type == ConnectionType.STRAIGHT) {
            if (getRedwireType(side) == null)
                return false;

            if (device instanceof IRedwire) {
                RedwireType rwt = getRedwireType(side);
                if (type == null)
                    return false;
                RedwireType rwt_ = ((IRedwire) device).getRedwireType(type == ConnectionType.STRAIGHT ? side.getOpposite() : side.getOpposite());
                if (rwt_ == null)
                    return false;
                if (!rwt.canConnectTo(rwt_))
                    return false;
            }

            if (device instanceof IFace)
                return ((IFace) device).getFace() == side.getOpposite();
            if (!OcclusionHelper.microblockOcclusionTest(this.getWorld(), this.getPos(), MicroblockShape.FACE_HOLLOW, 8, side))
                return false;
            if (device instanceof PneumaticTube)
                if (device instanceof MagTube != this instanceof MagTube)
                    return false;

            return true;
        }

        return false;
    }

    @Override
    public IConnectionCache<? extends IRedstoneDevice> getRedstoneConnectionCache() {

        return redConnections;
    }

    @Override
    public void onConnect(IConnection<?> connection) {

        sendUpdatePacket();
    }

    @Override
    public void onDisconnect(IConnection<?> connection) {

        sendUpdatePacket();
    }

    @Override
    public byte getRedstonePower(EnumFacing side) {

        if (!RedstoneApi.getInstance().shouldWiresOutputPower(hasLoss(side)))
            return 0;

        if (!isAnalogue(side))
            return (byte) ((power & 0xFF) > 0 ? 255 : 0);

        return power;
    }

    @Override
    public void setRedstonePower(EnumFacing side, byte power) {

        this.power = power;
    }

    @Override
    public void onRedstoneUpdate() {

        for (EnumFacing dir : EnumFacing.VALUES) {
            IConnection<IRedstoneDevice> c = redConnections.getConnectionOnSide(dir);
            IRedstoneDevice dev = null;
            if (c != null)
                dev = c.getB();
            if (dev == null || dev instanceof DummyRedstoneDevice)
                RedstoneHelper.notifyRedstoneUpdate(getWorld(), getPos(), dir, false);
        }

        sendUpdatePacket();
    }

    @Override
    public boolean hasLoss(EnumFacing side) {

        if (getRedwireType() == null)
            return false;

        return getRedwireType().hasLoss();
    }

    @Override
    public boolean isAnalogue(EnumFacing side) {

        if (getRedwireType() == null)
            return false;

        return getRedwireType().isAnalogue();
    }

    @Override
    public boolean canPropagateFrom(EnumFacing fromSide) {

        return true;// getRedwireType() != null;
    }

    public byte getPower() {

        return power;
    }

    public IRedstoneDevice getDeviceOnSide(EnumFacing d) {

        @SuppressWarnings("unchecked")
        IConnection<IRedstoneDevice> c = (IConnection<IRedstoneDevice>) getRedstoneConnectionCache().getConnectionOnSide(d);

        if (c == null)
            return null;

        return c.getB();
    }

    @Override
    public RedwireType getRedwireType(EnumFacing side) {

        return getRedwireType();
    }

    @Override
    public boolean isNormalFace(EnumFacing side) {

        return false;
    }
}
