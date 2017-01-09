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

package com.bluepowermod.part.wire.redstone;

import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.connect.IConnection;
import com.bluepowermod.api.connect.IConnectionCache;
import com.bluepowermod.api.connect.IConnectionListener;
import com.bluepowermod.api.gate.ic.IIntegratedCircuitPart;
import com.bluepowermod.api.misc.IFace;
import com.bluepowermod.api.wire.redstone.IBundledConductor.IAdvancedBundledConductor;
import com.bluepowermod.api.wire.redstone.*;
import com.bluepowermod.api.wire.redstone.IRedstoneConductor.IAdvancedRedstoneConductor;
import com.bluepowermod.client.render.IconSupplier;
import com.bluepowermod.helper.VectorHelper;
import com.bluepowermod.init.BPCreativeTabs;
import com.bluepowermod.part.wire.PartWireFace;
import com.bluepowermod.redstone.*;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import uk.co.qmunity.lib.client.render.RenderContext;
import uk.co.qmunity.lib.helper.MathHelper;
import uk.co.qmunity.lib.helper.OcclusionHelper;
import uk.co.qmunity.lib.helper.RedstoneHelper;
import uk.co.qmunity.lib.model.IVertexConsumer;
import uk.co.qmunity.lib.network.MCByteBuf;
import uk.co.qmunity.lib.part.IRedstonePart;
import uk.co.qmunity.lib.part.MicroblockShape;
import uk.co.qmunity.lib.util.MinecraftColor;
import uk.co.qmunity.lib.vec.Cuboid;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public abstract class PartRedwireFace extends PartWireFace implements IRedwire, IRedConductor, IIntegratedCircuitPart, IRedstonePart {

    private RedwireType type;

    public PartRedwireFace(int width, int height, RedwireType type) {

        this.width = width;
        this.height = height;
        this.type = type;
    }

    @Override
    public RedwireType getRedwireType(EnumFacing side) {

        return type;
    }

    // Part methods

    @Override
    public String getUnlocalizedName() {

        return getType();
    }

    // Wire methods

    private int width, height;
    private boolean connections[] = new boolean[6];

    @Override
    protected boolean shouldRenderConnection(EnumFacing side) {

        if (getParent() == null || getWorld() == null)
            return isConnected(side);

        return connections[side.ordinal()];
    }

    protected abstract boolean isConnected(EnumFacing side);

    @Override
    protected double getWidth() {

        return width;
    }

    @Override
    protected double getHeight() {

        return height;
    }

    @Override
    protected boolean extendsToCorner(EnumFacing side) {

        return cornerRender[side.ordinal()];
    }

    // Selection and occlusion boxes

    @Override
    public List<Cuboid> getSelectionBoxes() {

        List<Cuboid> boxes = new ArrayList<Cuboid>();

        boxes.add(new Cuboid(0, 0, 0, 1, getHeight() / 16D, 1).expand(-0.000001));

        VectorHelper.rotateBoxes(boxes, getFace(), 0);
        return boxes;
    }

    @Override
    public List<Cuboid> getOcclusionBoxes() {

        List<Cuboid> boxes = new ArrayList<Cuboid>();

        double h = getHeight() / 16D;
        double d = 4 / 16D;

        boxes.add(new Cuboid(d, 0, d, 1 - d, h, 1 - d));

        VectorHelper.rotateBoxes(boxes, getFace(), 0);
        return boxes;
    }

    // Conductor

    @Override
    public boolean hasLoss(EnumFacing side) {

        return getRedwireType(null).hasLoss();
    }

    @Override
    public boolean isAnalogue(EnumFacing side) {

        return getRedwireType(null).isAnalogue();
    }

    // NBT

    protected boolean[] cornerConnect = new boolean[6];
    protected boolean[] cornerRender = new boolean[6];

    @Override
    public void writeUpdateData(MCByteBuf buffer) {
        super.writeUpdateData(buffer);

        for (EnumFacing d : EnumFacing.VALUES)
            buffer.writeBoolean(isConnected(d));
    }

    @Override
    public void readUpdateData(MCByteBuf buffer) {
        super.readUpdateData(buffer);
        for (EnumFacing d : EnumFacing.VALUES)
            connections[d.ordinal()] = buffer.readBoolean();

        for (int i = 0; i < 6; i++) {
            cornerConnect[i] = buffer.readBoolean();
            cornerRender[i] = buffer.readBoolean();
        }
    }

    @Override
    public boolean canPlaceOnIntegratedCircuit() {

        return true;
    }

    @Override
    public CreativeTabs getCreativeTab() {

        return BPCreativeTabs.wiring;
    }

    public static class PartRedwireFaceUninsulated extends PartRedwireFace implements IAdvancedRedstoneConductor, IConnectionListener{

        private RedstoneConnectionCache connections = RedstoneApi.getInstance().createRedstoneConnectionCache(this);
        private boolean hasUpdated = false;
        private byte power = 0;
        private boolean scheduled = false;

        public PartRedwireFaceUninsulated(RedwireType type) {

            super(2, 2, type);
            connections.listen();
        }

        @Override
        public String getType() {

            return "wire." + getRedwireType(null).getName();
        }

        @Override
        protected boolean isConnected(EnumFacing side) {

            return connections.getConnectionOnSide(side) != null;
        }

        @Override
        protected TextureAtlasSprite getWireIcon(EnumFacing side) {

            return IconSupplier.wire;
        }

        @Override
        protected int getColorMultiplier() {

            return WireHelper.getColorForPowerLevel(getRedwireType(null), power);
        }

        @Override
        public boolean canConnect(EnumFacing side, IRedstoneDevice device, ConnectionType type) {

            if ((type == ConnectionType.STRAIGHT && side == getFace().getOpposite() && device instanceof IFace)
                    || side == null)
                return false;
            if (type == ConnectionType.CLOSED_CORNER) {
                if (side == getFace())
                    return false;
                if (side == getFace().getOpposite())
                    return false;
                if (side == null)
                    return false;
            }

            if (device instanceof IRedwire) {
                RedwireType rwt = getRedwireType(side);
                if (type == null)
                    return false;
                RedwireType rwt_ = ((IRedwire) device).getRedwireType(type == ConnectionType.STRAIGHT ? side.getOpposite()
                        : (type == ConnectionType.CLOSED_CORNER ? getFace() : getFace().getOpposite()));
                if (rwt_ == null)
                    return false;
                if (!rwt.canConnectTo(rwt_))
                    return false;
            }

            if (!OcclusionHelper.microblockOcclusionTest(getParent(), MicroblockShape.EDGE, 2, getFace(), side))
                return false;

            return true;
        }

        @Override
        public RedstoneConnectionCache getRedstoneConnectionCache() {

            return connections;
        }

        @Override
        public void onConnect(IConnection<?> connection) {

            sendUpdatePacket();
        }

        @Override
        public void onDisconnect(IConnection<?> connection) {

            scheduled = true;
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

            byte pow = hasLoss(side) ? power : (((power & 0xFF) > 0) ? (byte) 255 : (byte) 0);
            hasUpdated = hasUpdated | (pow != this.power);
            this.power = pow;
        }

        @Override
        public void onRedstoneUpdate() {

            if (getParent() instanceof FakeMultipartTileIC)
                ((FakeMultipartTileIC) getParent()).getIC().loadWorld();

            if (hasUpdated) {
                sendUpdatePacket();

                for (EnumFacing dir : EnumFacing.VALUES) {
                    IConnection<IRedstoneDevice> c = connections.getConnectionOnSide(dir);
                    IRedstoneDevice dev = null;
                    if (c != null)
                        dev = c.getB();
                    if (dir == getFace()) {
                        RedstoneHelper.notifyRedstoneUpdate(getWorld(), getPos(), dir, true);
                    } else if ((dev == null || dev instanceof DummyRedstoneDevice) && dir != getFace().getOpposite()) {
                        RedstoneHelper.notifyRedstoneUpdate(getWorld(), getPos(), dir, false);
                    }
                }

                hasUpdated = false;
            }
        }

        @Override
        public boolean canPropagateFrom(EnumFacing fromSide) {

            return true;
        }

        @Override
        public List<Entry<IConnection<IRedstoneDevice>, Boolean>> propagate(EnumFacing fromSide) {

            if (getParent() instanceof FakeMultipartTileIC)
                ((FakeMultipartTileIC) getParent()).getIC().loadWorld();

            List<Entry<IConnection<IRedstoneDevice>, Boolean>> l = new ArrayList<Entry<IConnection<IRedstoneDevice>, Boolean>>();

            for (EnumFacing d : EnumFacing.VALUES) {
                IConnection<IRedstoneDevice> c = connections.getConnectionOnSide(d);
                if (c != null)
                    l.add(new Pair<IConnection<IRedstoneDevice>, Boolean>(c, c.getB() instanceof IRedwire
                            && ((IRedwire) c.getB()).getRedwireType(c.getSideB()) != getRedwireType(c.getSideA())));
            }

            return l;
        }

        @Override
        public void onUpdate() {

            // Don't to anything if propagation-related stuff is going on
            if (!RedstoneApi.getInstance().shouldWiresHandleUpdates())
                return;

            super.onUpdate();

            // Do not do anything if we're on the client
            if (getWorld().isRemote)
                return;

            // Refresh connections
            connections.recalculateConnections();
            // Add bottom device (forced)
            if (connections.getConnectionOnSide(getFace()) == null) {
                DummyRedstoneDevice drd = DummyRedstoneDevice.getDeviceAt(this.getPos().offset(getFace()), this.getWorld(), this.getWorld().getBlockState(this.getPos()).getBlock());
                connections.onConnect(getFace(), drd, getFace().getOpposite(), ConnectionType.STRAIGHT);
                drd.getRedstoneConnectionCache().onConnect(getFace().getOpposite(), this, getFace(), ConnectionType.STRAIGHT);
            }

            RedstoneApi.getInstance().getRedstonePropagator(this, getFace()).propagate();
        }

        @Override
        public void update() {

            if (scheduled)
                onUpdate();
            scheduled = false;
        }

        @Override
        public void onRemoved() {

            if (getWorld().isRemote)
                return;

            // Don't to anything if propagation-related stuff is going on
            if (!RedstoneApi.getInstance().shouldWiresHandleUpdates())
                return;

            power = 0;
            hasUpdated = true;

            boolean should = RedstoneApi.getInstance().shouldWiresHandleUpdates();
            RedstoneApi.getInstance().setWiresHandleUpdates(false);
            onRedstoneUpdate();
            connections.disconnectAll();
            RedstoneApi.getInstance().setWiresHandleUpdates(should);
        }

        @Override
        public void writeUpdateData(MCByteBuf buffer) {
            super.writeUpdateData(buffer);

            for (int i = 0; i < 6; i++) {
                boolean connected = false;
                boolean render = false;

                IConnection<IRedstoneDevice> c = getRedstoneConnectionCache().getConnectionOnSide(EnumFacing.getFront(i));
                if (c != null) {
                    IRedstoneDevice dev = c.getB();
                    if (dev instanceof IFace && ((IFace) dev).getFace() == EnumFacing.getFront(i).getOpposite()) {
                        if (dev instanceof IRedwire) {
                            if (dev instanceof IInsulatedRedstoneDevice
                                    && ((IInsulatedRedstoneDevice) dev).getInsulationColor(c.getSideB()) != MinecraftColor.NONE)
                                render = true;
                            if (dev instanceof IFace && getFace().ordinal() > ((IFace) dev).getFace().ordinal())
                                render = true;
                        } else {
                            connected = true;
                            render = true;
                        }
                    }
                }

                buffer.writeBoolean(connected);
                buffer.writeBoolean(render);
            }

            buffer.writeByte(power);
        }

        @Override
        public void readUpdateData(MCByteBuf buffer) {
            super.readUpdateData(buffer);
            power = buffer.readByte();

            if (getParent() != null && getWorld() != null)
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }

        @Override
        public void addWAILABody(List<String> text) {

            super.addWAILABody(text);

            text.add("Power: " + (power & 0xFF));
        }

        @Override
        public boolean canConnectRedstone(EnumFacing side) {

            return side != getFace().getOpposite();
        }

        @Override
        public int getWeakPower(EnumFacing side) {

            if (!RedstoneApi.getInstance().shouldWiresOutputPower(hasLoss(side)))
                return 0;

            if (getWorld().getBlockState(getPos().offset(side)).getBlock() instanceof BlockRedstoneWire)
                return 0;

            if (side == getFace().getOpposite())
                return 0;

            return MathHelper.map(power & 0xFF, 0, 255, 0, 15);
        }

        @Override
        public int getStrongPower(EnumFacing side) {

            if (!RedstoneApi.getInstance().shouldWiresOutputPower(hasLoss(side)))
                return 0;

            if (side != getFace())
                return 0;

            return MathHelper.map(power & 0xFF, 0, 255, 0, 15);
        }

        @Override
        public boolean isNormalFace(EnumFacing side) {

            return false;
        }

    }

    public static class PartRedwireFaceInsulated extends PartRedwireFace implements IAdvancedRedstoneConductor, IInsulatedRedstoneDevice,
            IAdvancedBundledConductor, IConnectionListener, IInsulatedRedwire {

        private RedstoneConnectionCache connections = RedstoneApi.getInstance().createRedstoneConnectionCache(this);
        private BundledConnectionCache bundledConnections = RedstoneApi.getInstance().createBundledConnectionCache(this);
        private boolean hasUpdated = false;
        private byte power = 0;
        private MinecraftColor color;
        private boolean scheduled = false;

        public PartRedwireFaceInsulated(RedwireType type, MinecraftColor color) {

            super(4, 3, type);
            this.color = color;

            connections.listen();
            bundledConnections.listen();
        }

        @Override
        public String getType() {

            return "wire." + getRedwireType(null).getName() + "." + color.name().toLowerCase();
        }

        @Override
        protected boolean isConnected(EnumFacing side) {

            if (getParent() == null || getWorld() == null)
                return true;

            return connections.getConnectionOnSide(side) != null || bundledConnections.getConnectionOnSide(side) != null;
        }

        @Override
        protected TextureAtlasSprite getWireIcon(EnumFacing side) {

            return side == EnumFacing.UP || side == EnumFacing.DOWN ? IconSupplier.wireInsulation1 : IconSupplier.wireInsulation2;
        }

        @Override
        protected int getColorMultiplier() {

            return color.getHex();
        }

        @Override
        public boolean canConnect(EnumFacing side, IRedstoneDevice device, ConnectionType type) {

            if (type == ConnectionType.STRAIGHT && side == getFace().getOpposite() || side == null)
                return false;
            if (type == ConnectionType.CLOSED_CORNER) {
                if (side == getFace())
                    return false;
                if (side == getFace().getOpposite())
                    return false;
                if (side == null)
                    return false;
            }

            if (device instanceof IInsulatedRedstoneDevice) {
                MinecraftColor c = ((IInsulatedRedstoneDevice) device).getInsulationColor(type == ConnectionType.STRAIGHT ? side
                        .getOpposite() : (type == ConnectionType.CLOSED_CORNER ? getFace() : getFace().getOpposite()));
                if (c != null && c != getInsulationColor(side))
                    return false;
            }

            if (device instanceof IRedwire) {
                RedwireType rwt = getRedwireType(side);
                if (type == null)
                    return false;
                RedwireType rwt_ = ((IRedwire) device).getRedwireType(type == ConnectionType.STRAIGHT ? side.getOpposite()
                        : (type == ConnectionType.CLOSED_CORNER ? getFace() : getFace().getOpposite()));
                if (rwt_ == null)
                    return false;
                if (!rwt.canConnectTo(rwt_))
                    return false;
            }

            if (!OcclusionHelper.microblockOcclusionTest(getParent(), MicroblockShape.EDGE, 2, getFace(), side))
                return false;

            return true;
        }

        @Override
        public boolean canConnect(EnumFacing side, IBundledDevice device, ConnectionType type) {

            if (device instanceof IInsulatedRedstoneDevice)
                return false;

            if (device instanceof IRedwire) {
                RedwireType rwt = getRedwireType(side);
                if (type == null)
                    return false;
                RedwireType rwt_ = ((IRedwire) device).getRedwireType(type == ConnectionType.STRAIGHT ? side.getOpposite()
                        : (type == ConnectionType.CLOSED_CORNER ? getFace() : getFace().getOpposite()));
                if (rwt_ == null)
                    return false;
                if (!rwt.canConnectTo(rwt_))
                    return false;
            }

            if (!OcclusionHelper.microblockOcclusionTest(getParent(), MicroblockShape.EDGE, 2, getFace(), side))
                return false;

            return true;
        }

        @Override
        public RedstoneConnectionCache getRedstoneConnectionCache() {

            return connections;
        }

        @Override
        public IConnectionCache<? extends IBundledDevice> getBundledConnectionCache() {

            return bundledConnections;
        }

        @Override
        public void onConnect(IConnection<?> connection) {

            sendUpdatePacket();
        }

        @Override
        public void onDisconnect(IConnection<?> connection) {

            scheduled = true;
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

            byte pow = isAnalogue(side) ? power : (((power & 0xFF) > 0) ? (byte) 255 : (byte) 0);
            hasUpdated = hasUpdated | (pow != this.power);
            this.power = pow;
        }

        @Override
        public byte[] getBundledOutput(EnumFacing side) {

            if (!RedstoneApi.getInstance().shouldWiresOutputPower(hasLoss(side)))
                return new byte[16];

            return getBundledPower(side);
        }

        @Override
        public void setBundledPower(EnumFacing side, byte[] power) {

            this.power = power[getInsulationColor(side).ordinal()];
            hasUpdated = true;
        }

        @Override
        public byte[] getBundledPower(EnumFacing side) {

            byte[] val = new byte[16];
            val[color.ordinal()] = power;
            return val;
        }

        @Override
        public void onRedstoneUpdate() {

            if (getParent() == null || getWorld() == null)
                return;

            if (hasUpdated) {
                sendUpdatePacket();

                for (EnumFacing dir : EnumFacing.VALUES) {
                    IConnection<IRedstoneDevice> c = connections.getConnectionOnSide(dir);
                    if (c == null)
                        continue;
                    IRedstoneDevice dev = c.getB();
                    if (dir == getFace())
                        RedstoneHelper.notifyRedstoneUpdate(getWorld(), getPos(), dir, true);
                    else if ((dev == null || dev instanceof DummyRedstoneDevice) && dir != getFace().getOpposite())
                        RedstoneHelper.notifyRedstoneUpdate(getWorld(), getPos(), dir, false);
                }

                hasUpdated = false;
            }
        }

        @Override
        public void onBundledUpdate() {

            onRedstoneUpdate();
        }

        @Override
        public boolean canPropagateFrom(EnumFacing fromSide) {

            return true;
        }

        @Override
        public boolean canPropagateBundledFrom(EnumFacing fromSide) {

            return true;
        }

        @Override
        public List<Entry<IConnection<IRedstoneDevice>, Boolean>> propagate(EnumFacing fromSide) {

            List<Entry<IConnection<IRedstoneDevice>, Boolean>> l = new ArrayList<Entry<IConnection<IRedstoneDevice>, Boolean>>();

            for (EnumFacing d : EnumFacing.VALUES) {
                IConnection<IRedstoneDevice> c = connections.getConnectionOnSide(d);
                if (c != null)
                    l.add(new Pair<IConnection<IRedstoneDevice>, Boolean>(c, c.getB() instanceof IRedwire
                            && ((IRedwire) c.getB()).getRedwireType(c.getSideB()) != getRedwireType(c.getSideA())));

                IConnection<IBundledDevice> cB = bundledConnections.getConnectionOnSide(d);
                if (cB != null)
                    l.add(new Pair<IConnection<IRedstoneDevice>, Boolean>(new RedstoneConnection(this, BundledDeviceWrapper.wrap(cB.getB(),
                            color), cB.getSideA(), cB.getSideB(), cB.getType()), cB.getB() instanceof IRedwire
                            && ((IRedwire) cB.getB()).getRedwireType(cB.getSideB()) != getRedwireType(cB.getSideA())));
            }

            return l;
        }

        @Override
        public Collection<Entry<IConnection<IBundledDevice>, Boolean>> propagateBundled(EnumFacing fromSide) {

            List<Entry<IConnection<IBundledDevice>, Boolean>> l = new ArrayList<Entry<IConnection<IBundledDevice>, Boolean>>();

            for (EnumFacing d : EnumFacing.VALUES) {
                IConnection<IBundledDevice> c = bundledConnections.getConnectionOnSide(d);
                if (c != null)
                    l.add(new Pair<IConnection<IBundledDevice>, Boolean>(c, c.getB() instanceof IRedwire
                            && ((IRedwire) c.getB()).getRedwireType(c.getSideB()) != getRedwireType(c.getSideA())));
            }

            return l;
        }

        @Override
        public MinecraftColor getBundledColor(EnumFacing side) {

            return MinecraftColor.NONE;
        }

        @Override
        public void onUpdate() {

            // Don't to anything if propagation-related stuff is going on
            if (!RedstoneApi.getInstance().shouldWiresHandleUpdates())
                return;

            super.onUpdate();

            // Do not do anything if we're on the client
            if (getWorld().isRemote)
                return;

            // Refresh connections
            connections.recalculateConnections();
            bundledConnections.recalculateConnections();

            RedstoneApi.getInstance().getRedstonePropagator(this, getFace()).propagate();
            RedstoneApi.getInstance().getBundledPropagator(this, getFace()).propagate();
        }

        @Override
        public void update() {

            if (scheduled)
                onUpdate();
            scheduled = false;
        }

        @Override
        public void onRemoved() {

            if (!getWorld().isRemote) {
                power = 0;
                hasUpdated = true;

                boolean should = RedstoneApi.getInstance().shouldWiresHandleUpdates();
                RedstoneApi.getInstance().setWiresHandleUpdates(false);
                onRedstoneUpdate();
                RedstoneApi.getInstance().setWiresHandleUpdates(should);
            }

            // Don't to anything if propagation-related stuff is going on
            if (!RedstoneApi.getInstance().shouldWiresHandleUpdates())
                return;

            super.onRemoved();

            // Do not do anything if we're on the client
            if (getWorld().isRemote)
                return;

            boolean should = RedstoneApi.getInstance().shouldWiresHandleUpdates();
            RedstoneApi.getInstance().setWiresHandleUpdates(false);
            connections.disconnectAll();
            bundledConnections.disconnectAll();
            RedstoneApi.getInstance().setWiresHandleUpdates(should);
        }

        @Override
        public boolean renderStatic(RenderContext context, IVertexConsumer consumer, int pass) {
            super.renderStatic(context, consumer, pass);

            EnumFacing d1 = EnumFacing.NORTH;
            EnumFacing d2 = EnumFacing.SOUTH;
            EnumFacing d3 = EnumFacing.WEST;
            EnumFacing d4 = EnumFacing.EAST;

            if (getFace() == EnumFacing.NORTH) {
                d1 = EnumFacing.UP;
                d2 = EnumFacing.DOWN;
            } else if (getFace() == EnumFacing.SOUTH) {
                d1 = EnumFacing.DOWN;
                d2 = EnumFacing.UP;
            } else if (getFace() == EnumFacing.WEST) {
                d3 = EnumFacing.UP;
                d4 = EnumFacing.DOWN;
            } else if (getFace() == EnumFacing.EAST) {
                d3 = EnumFacing.DOWN;
                d4 = EnumFacing.UP;
            } else if (getFace() == EnumFacing.UP) {
                d3 = EnumFacing.EAST;
                d4 = EnumFacing.WEST;
            }

            if (getFace() == EnumFacing.NORTH || getFace() == EnumFacing.SOUTH) {
                d1 = d1.rotateAround(getFace().getAxis());
                d2 = d2.rotateAround(getFace().getAxis());
                d3 = d3.rotateAround(getFace().getAxis());
                d4 = d4.rotateAround(getFace().getAxis());
            }

            boolean s1 = shouldRenderConnection(d1);
            boolean s2 = shouldRenderConnection(d2);
            boolean s3 = shouldRenderConnection(d3);
            boolean s4 = shouldRenderConnection(d4);

            double size = 1 / 64D;

            double width = 1 / 32D;
            double y = 0.001;// getHeight() / 16D;
            double height = getHeight() / 16D;

            renderer.setColor(WireHelper.getColorForPowerLevel(getRedwireType(null), power));

            // Center
            if ((s1 && s3) || (s3 && s2) || (s2 && s4) || (s4 && s1)) {
                renderer.renderBox(new Cuboid(8 / 16D - width - size, y, 8 / 16D - width - size, 8 / 16D + width + size, height + size,
                        8 / 16D + width + size), IconSupplier.wire);
            } else {
                renderer.renderBox(new Cuboid(8 / 16D - width, y, 8 / 16D - width, 8 / 16D + width, height + size, 8 / 16D + width),
                        IconSupplier.wire);
            }
            // Sides
            if (s4 || s3) {
                if (s3 || (!s1 && !s2))
                    renderer.renderBox(new Cuboid(s3 ? (cornerConnect[d3.ordinal()] ? -height - size : 0.001) : 5 / 16D, y,
                            8 / 16D - width, 8 / 16D - width, height + size, 8 / 16D + width), IconSupplier.wire);
                if (s4 || (!s1 && !s2))
                    renderer.renderBox(new Cuboid(8 / 16D + width, y, 8 / 16D - width, s4 ? (cornerConnect[d4.ordinal()] ? 1 + height
                            + size : 0.999) : 11 / 16D, height + size, 8 / 16D + width), IconSupplier.wire);
                if (s1)
                    renderer.renderBox(new Cuboid(8 / 16D - width, y, s1 ? (cornerConnect[d1.ordinal()] ? -height - size : 0.001)
                            : 4 / 16D, 8 / 16D + width, height + size, 8 / 16D - width), IconSupplier.wire);
                if (s2)
                    renderer.renderBox(new Cuboid(8 / 16D - width, y, 8 / 16D + width, 8 / 16D + width, height + size,
                            s2 ? (cornerConnect[d2.ordinal()] ? 1 + height + size : 0.999) : 12 / 16D), IconSupplier.wire);
            } else {
                renderer.renderBox(new Cuboid(8 / 16D - width, y, s1 ? (cornerConnect[d1.ordinal()] ? -height - size : 0.001) : 5 / 16D,
                        8 / 16D + width, height + size, 8 / 16D - width), IconSupplier.wire);
                renderer.renderBox(new Cuboid(8 / 16D - width, y, 8 / 16D + width, 8 / 16D + width, height + size,
                        s2 ? (cornerConnect[d2.ordinal()] ? 1 + height + size : 0.999) : 11 / 16D), IconSupplier.wire);
            }

            double len = 1 / 16D;
            width = 1 / 16D;

            if (s4 || s3) {
                if (s3 || (!s1 && !s2))
                    renderer.renderBox(new Cuboid(4 / 16D - len, 0, 8 / 16D - width, 4 / 16D, 2 / 16D, 8 / 16D + width),
                            IconSupplier.wire);

                if (s4 || (!s1 && !s2)) {
                    renderer.renderBox(new Cuboid(12 / 16D, 0, 8 / 16D - width, 12 / 16D + len, 2 / 16D, 8 / 16D + width),
                            IconSupplier.wire);
                }
            } else {
                if (!s1)
                    renderer.renderBox(new Cuboid(8 / 16D - width, 0, 4 / 16D - len, 8 / 16D + width, 2 / 16D, 4 / 16D),
                            IconSupplier.wire);
                if (!s2)
                    renderer.renderBox(new Cuboid(8 / 16D - width, 0, 12 / 16D, 8 / 16D + width, 2 / 16D, 12 / 16D + len),
                            IconSupplier.wire);
            }

            return true;
        }

        @Override
        public void writeUpdateData(MCByteBuf buffer) {
            super.writeUpdateData(buffer);

            for (int i = 0; i < 6; i++) {
                boolean connected = false;
                boolean render = false;

                IConnection<? extends IRedstoneDevice> c = getRedstoneConnectionCache().getConnectionOnSide(
                        EnumFacing.getFront(i));
                if (c != null) {
                    IRedstoneDevice dev = c.getB();
                    if (dev instanceof IFace && ((IFace) dev).getFace() == EnumFacing.getFront(i).getOpposite()) {
                        if (dev instanceof IRedwire) {
                            if (dev instanceof IInsulatedRedstoneDevice
                                    && ((IInsulatedRedstoneDevice) dev).getInsulationColor(c.getSideB()) != MinecraftColor.NONE)
                                connected = true;
                            if (dev instanceof IFace && getFace().ordinal() > ((IFace) dev).getFace().ordinal()) {
                                if (dev instanceof IInsulatedRedstoneDevice
                                        && ((IInsulatedRedstoneDevice) dev).getInsulationColor(c.getSideB()) == getInsulationColor(c
                                                .getSideA()))
                                    render = true;
                                if (getInsulationColor(c.getSideA()) == MinecraftColor.NONE)
                                    render = true;
                            }
                        } else {
                            connected = true;
                            render = true;
                        }
                    }
                }
                IConnection<? extends IBundledDevice> bc = getBundledConnectionCache()
                        .getConnectionOnSide(EnumFacing.getFront(i));
                if (bc != null) {
                    IBundledDevice dev = bc.getB();
                    if (dev instanceof IFace && ((IFace) dev).getFace() == EnumFacing.getFront(i).getOpposite()) {
                        if (dev instanceof IRedwire) {
                            if (dev instanceof IInsulatedRedstoneDevice
                                    && ((IInsulatedRedstoneDevice) dev).getInsulationColor(bc.getSideB()) != MinecraftColor.NONE)
                                connected = true;
                            if (dev instanceof IFace && getFace().ordinal() > ((IFace) dev).getFace().ordinal()) {
                                if (dev instanceof IInsulatedRedstoneDevice
                                        && ((IInsulatedRedstoneDevice) dev).getInsulationColor(bc.getSideB()) == getInsulationColor(bc
                                                .getSideA()))
                                    render = true;
                                if (getInsulationColor(bc.getSideA()) == MinecraftColor.NONE)
                                    render = true;
                            }
                            if (!(dev instanceof IInsulatedRedstoneDevice)) {
                                render = true;
                                connected = true;
                            }
                        } else {
                            connected = true;
                            render = true;
                        }
                    }
                }

                buffer.writeBoolean(connected);
                buffer.writeBoolean(render);
            }

            buffer.writeByte(power);
        }

        @Override
        public void readUpdateData(MCByteBuf buffer) {
            super.readUpdateData(buffer);
            power = buffer.readByte();

            if (getParent() != null && getWorld() != null)
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }

        @Override
        public void addWAILABody(List<String> text) {

            super.addWAILABody(text);

            text.add("Power: " + (power & 0xFF));
        }

        @Override
        public MinecraftColor getInsulationColor(EnumFacing side) {

            return color;
        }

        @Override
        public boolean canConnectRedstone(EnumFacing side) {

            return side != getFace().getOpposite();
        }

        @Override
        public int getWeakPower(EnumFacing side) {

            if (!RedstoneApi.getInstance().shouldWiresOutputPower(hasLoss(side)))
                return 0;

            if (this.getWorld().getBlockState(getPos().offset(side)).getBlock() instanceof BlockRedstoneWire)
                return 0;

            if (side == getFace() || side == getFace().getOpposite())
                return 0;

            return MathHelper.map(power & 0xFF, 0, 255, 0, 15);
        }

        @Override
        public int getStrongPower(EnumFacing side) {

            return 0;
        }

        @Override
        public boolean isNormalFace(EnumFacing side) {

            return false;
        }

    }

    public static class PartRedwireFaceBundled extends PartRedwireFace implements IAdvancedBundledConductor, IConnectionListener {

        private BundledConnectionCache bundledConnections = RedstoneApi.getInstance().createBundledConnectionCache(this);
        private byte[] power = new byte[16];
        private MinecraftColor color;

        public PartRedwireFaceBundled(RedwireType type, MinecraftColor color) {

            super(6, 4, type);
            this.color = color;

            bundledConnections.listen();
        }

        @Override
        public String getType() {

            return "wire." + getRedwireType(null).getName() + ".bundled"
                    + (color != MinecraftColor.NONE ? ("." + color.name().toLowerCase()) : "");
        }

        @Override
        protected boolean isConnected(EnumFacing side) {

            if (getParent() == null || getWorld() == null)
                return true;

            return bundledConnections.getConnectionOnSide(side) != null;
        }

        @Override
        protected TextureAtlasSprite getWireIcon(EnumFacing side) {

            return null;
        }

        @Override
        protected TextureAtlasSprite getWireIcon(EnumFacing side, EnumFacing face) {

            if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                if (side == null) {
                    EnumFacing d1 = EnumFacing.NORTH;
                    EnumFacing d2 = EnumFacing.SOUTH;
                    EnumFacing d3 = EnumFacing.WEST;
                    EnumFacing d4 = EnumFacing.EAST;

                    if (getFace() == EnumFacing.NORTH) {
                        d1 = EnumFacing.UP;
                        d2 = EnumFacing.DOWN;
                    } else if (getFace() == EnumFacing.SOUTH) {
                        d1 = EnumFacing.DOWN;
                        d2 = EnumFacing.UP;
                    } else if (getFace() == EnumFacing.WEST) {
                        d3 = EnumFacing.UP;
                        d4 = EnumFacing.DOWN;
                    } else if (getFace() == EnumFacing.EAST) {
                        d3 = EnumFacing.DOWN;
                        d4 = EnumFacing.UP;
                    } else if (getFace() == EnumFacing.UP) {
                        d3 = EnumFacing.EAST;
                        d4 = EnumFacing.WEST;
                    }

                    if (getFace() == EnumFacing.NORTH || getFace() == EnumFacing.SOUTH) {
                        d1 = d1.rotateAround(getFace().getAxis());
                        d2 = d2.rotateAround(getFace().getAxis());
                        d3 = d3.rotateAround(getFace().getAxis());
                        d4 = d4.rotateAround(getFace().getAxis());
                    }

                    boolean s1 = shouldRenderConnection(d1);
                    boolean s2 = shouldRenderConnection(d2);
                    boolean s3 = shouldRenderConnection(d3);
                    boolean s4 = shouldRenderConnection(d4);

                    if ((s1 || s2) && !(s3 || s4))
                        return IconSupplier.wireBundledStraight1;
                    if (!(s1 || s2) && (s3 || s4))
                        return IconSupplier.wireBundledStraight2;
                }
                return IconSupplier.wireBundledCross;
            }

            if (side == face)
                return IconSupplier.wireBundledConnection;

            if (face == EnumFacing.UP || face == EnumFacing.WEST || face == EnumFacing.NORTH)
                return IconSupplier.wireBundledSide1;

            return IconSupplier.wireBundledSide2;
        }

        @Override
        protected int getColorMultiplier() {

            return 0xFFFFFF;
        }

        @Override
        public boolean canConnect(EnumFacing side, IBundledDevice device, ConnectionType type) {

            if (type == ConnectionType.CLOSED_CORNER) {
                if (side == getFace())
                    return false;
                if (side == getFace().getOpposite())
                    return false;
                if (side == null)
                    return false;
            }

            if (device instanceof IRedwire) {
                RedwireType rwt = getRedwireType(side);
                if (type == null)
                    return false;
                RedwireType rwt_ = ((IRedwire) device).getRedwireType(type == ConnectionType.STRAIGHT ? side.getOpposite()
                        : (type == ConnectionType.CLOSED_CORNER ? getFace() : getFace().getOpposite()));
                if (rwt_ == null)
                    return false;
                if (!rwt.canConnectTo(rwt_))
                    return false;
            }

            if (!color.canConnect(device.getBundledColor(type == ConnectionType.STRAIGHT ? side.getOpposite()
                    : (type == ConnectionType.CLOSED_CORNER ? getFace() : getFace().getOpposite()))))
                return false;

            if (!OcclusionHelper.microblockOcclusionTest(getParent(), MicroblockShape.EDGE, 2, getFace(), side))
                return false;

            return true;
        }

        @Override
        public IConnectionCache<? extends IBundledDevice> getBundledConnectionCache() {

            return bundledConnections;
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
        public byte[] getBundledOutput(EnumFacing side) {

            if (!RedstoneApi.getInstance().shouldWiresOutputPower(hasLoss(side)))
                return new byte[16];

            return getBundledPower(side);
        }

        @Override
        public void setBundledPower(EnumFacing side, byte[] power) {

            this.power = power;
        }

        @Override
        public byte[] getBundledPower(EnumFacing side) {

            return power;
        }

        @Override
        public void onBundledUpdate() {

        }

        @Override
        public boolean canPropagateBundledFrom(EnumFacing fromSide) {

            return true;
        }

        @Override
        public Collection<Entry<IConnection<IBundledDevice>, Boolean>> propagateBundled(EnumFacing fromSide) {

            List<Entry<IConnection<IBundledDevice>, Boolean>> l = new ArrayList<Entry<IConnection<IBundledDevice>, Boolean>>();
            for (EnumFacing d : EnumFacing.VALUES) {
                IConnection<IBundledDevice> c = bundledConnections.getConnectionOnSide(d);
                if (c != null)
                    l.add(new Pair<IConnection<IBundledDevice>, Boolean>(c, c.getB() instanceof IRedwire
                            && ((IRedwire) c.getB()).getRedwireType(c.getSideB()) != getRedwireType(c.getSideA())));
            }

            return l;
        }

        @Override
        public MinecraftColor getBundledColor(EnumFacing side) {

            return color;
        }

        @Override
        public void onUpdate() {

            // Don't to anything if propagation-related stuff is going on
            if (!RedstoneApi.getInstance().shouldWiresHandleUpdates())
                return;

            super.onUpdate();

            // Do not do anything if we're on the client
            if (getWorld().isRemote)
                return;

            // Refresh connections
            bundledConnections.recalculateConnections();
            RedstoneApi.getInstance().getBundledPropagator(this, getFace()).propagate();
        }

        @Override
        public void onRemoved() {

            // Don't to anything if propagation-related stuff is going on
            if (!RedstoneApi.getInstance().shouldWiresHandleUpdates())
                return;

            super.onRemoved();

            // Do not do anything if we're on the client
            if (getWorld().isRemote)
                return;

            boolean should = RedstoneApi.getInstance().shouldWiresHandleUpdates();
            RedstoneApi.getInstance().setWiresHandleUpdates(false);
            bundledConnections.disconnectAll();
            RedstoneApi.getInstance().setWiresHandleUpdates(should);
        }


        @Override
        public boolean renderStatic(RenderContext context, IVertexConsumer consumer, int pass) {
            super.renderStatic(context, consumer, pass);

            EnumFacing d1 = EnumFacing.NORTH;
            EnumFacing d2 = EnumFacing.SOUTH;
            EnumFacing d3 = EnumFacing.WEST;
            EnumFacing d4 = EnumFacing.EAST;

            if (getFace() == EnumFacing.NORTH) {
                d1 = EnumFacing.UP;
                d2 = EnumFacing.DOWN;
            } else if (getFace() == EnumFacing.SOUTH) {
                d1 = EnumFacing.DOWN;
                d2 = EnumFacing.UP;
            } else if (getFace() == EnumFacing.WEST) {
                d3 = EnumFacing.UP;
                d4 = EnumFacing.DOWN;
            } else if (getFace() == EnumFacing.EAST) {
                d3 = EnumFacing.DOWN;
                d4 = EnumFacing.UP;
            } else if (getFace() == EnumFacing.UP) {
                d3 = EnumFacing.EAST;
                d4 = EnumFacing.WEST;
            }

            if (getFace() == EnumFacing.NORTH || getFace() == EnumFacing.SOUTH) {
                d1 = d1.rotateAround(getFace().getAxis());
                d2 = d2.rotateAround(getFace().getAxis());
                d3 = d3.rotateAround(getFace().getAxis());
                d4 = d4.rotateAround(getFace().getAxis());
            }

            boolean s1 = shouldRenderConnection(d1);
            boolean s2 = shouldRenderConnection(d2);
            boolean s3 = shouldRenderConnection(d3);
            boolean s4 = shouldRenderConnection(d4);

            double size = 1 / 64D;

            double width = 1 / 48D;
            double y = 0;
            double height = getHeight() / 16D;

            renderer.setColor(WireHelper.getColorForPowerLevel(getRedwireType(null), (byte) (255 / 2)/* power */));

            // Center
            if ((s1 && s3) || (s3 && s2) || (s2 && s4) || (s4 && s1)) {
                renderer.renderBox(new Cuboid(8 / 16D - width - size, height, 8 / 16D - width - size, 8 / 16D + width + size, height
                        + size, 8 / 16D + width + size), IconSupplier.wire);
            } else {
                renderer.renderBox(
                        new Cuboid(8 / 16D - width, height, 8 / 16D - width, 8 / 16D + width, height + size, 8 / 16D + width),
                        IconSupplier.wire);
            }
            // Sides
            if (s4 || s3) {
                if (s3 || (!s1 && !s2))
                    renderer.renderBox(new Cuboid(s3 ? (cornerConnect[d3.ordinal()] ? -height - size : -size) : 5 / 16D, y,
                            8 / 16D - width, 8 / 16D - width, height + size, 8 / 16D + width), IconSupplier.wire);
                if (s4 || (!s1 && !s2))
                    renderer.renderBox(new Cuboid(8 / 16D + width, y, 8 / 16D - width, s4 ? (cornerConnect[d4.ordinal()] ? 1 + height
                            + size : 1 + size) : 11 / 16D, height + size, 8 / 16D + width), IconSupplier.wire);
                if (s1)
                    renderer.renderBox(new Cuboid(8 / 16D - width, y, s1 ? (cornerConnect[d1.ordinal()] ? -height - size : -size)
                            : 4 / 16D, 8 / 16D + width, height + size, 8 / 16D - width), IconSupplier.wire);
                if (s2)
                    renderer.renderBox(new Cuboid(8 / 16D - width, y, 8 / 16D + width, 8 / 16D + width, height + size,
                            s2 ? (cornerConnect[d2.ordinal()] ? 1 + height + size : 1 + size) : 12 / 16D), IconSupplier.wire);
            } else {
                renderer.renderBox(new Cuboid(8 / 16D - width, y, s1 ? (cornerConnect[d1.ordinal()] ? -height - size : -size) : 5 / 16D,
                        8 / 16D + width, height + size, 8 / 16D - width), IconSupplier.wire);
                renderer.renderBox(new Cuboid(8 / 16D - width, y, 8 / 16D + width, 8 / 16D + width, height + size,
                        s2 ? (cornerConnect[d2.ordinal()] ? 1 + height + size : 1 + size) : 11 / 16D), IconSupplier.wire);
            }

            return true;
        }

        @Override
        public void writeUpdateData(MCByteBuf buffer) {
            super.writeUpdateData(buffer);

            for (int i = 0; i < 6; i++) {
                boolean connected = false;
                boolean render = false;
                IConnection<? extends IBundledDevice> bc = getBundledConnectionCache()
                        .getConnectionOnSide(EnumFacing.getFront(i));
                if (bc != null) {
                    IBundledDevice dev = bc.getB();
                    if (dev instanceof IFace && ((IFace) dev).getFace() == EnumFacing.getFront(i).getOpposite()) {
                        if (dev instanceof IRedwire) {
                            if (dev instanceof IFace && getFace().ordinal() > ((IFace) dev).getFace().ordinal()) {
                                if (!(dev instanceof IInsulatedRedstoneDevice) && dev instanceof IRedwire) {
                                    render = true;
                                    connected = true;
                                }
                            } else if (dev instanceof PartRedwireFaceBundled) {
                                connected = true;
                            }
                        } else {
                            connected = true;
                            render = true;
                        }
                    }
                }

                buffer.writeBoolean(connected);
                buffer.writeBoolean(render);
            }
        }

        @Override
        public void readUpdateData(MCByteBuf buffer) {
            super.readUpdateData(buffer);

            if (getParent() != null && getWorld() != null)
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }

        @Override
        public boolean canConnectRedstone(EnumFacing side) {

            return false;
        }

        @Override
        public int getWeakPower(EnumFacing side) {

            return 0;
        }

        @Override
        public int getStrongPower(EnumFacing side) {

            return 0;
        }

        @Override
        public boolean isNormalFace(EnumFacing side) {

            return false;
        }

    }

}