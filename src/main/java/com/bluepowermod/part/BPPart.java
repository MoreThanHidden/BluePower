package com.bluepowermod.part;

import com.bluepowermod.api.item.IDatabaseSaveable;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.qmunity.lib.client.render.RenderContext;
import uk.co.qmunity.lib.model.IVertexConsumer;
import uk.co.qmunity.lib.part.IOccludingPart;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.part.IWailaProviderPart;
import uk.co.qmunity.lib.part.QLPart;
import uk.co.qmunity.lib.raytrace.QRayTraceResult;
import uk.co.qmunity.lib.vec.Cuboid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BPPart extends QLPart implements IOccludingPart,  IDatabaseSaveable, IWailaProviderPart{


    @Override
    public World getWorld() {

        if (getParent() == null)
            return null;

        return super.getWorld();
    }

    @Override
    public BlockPos getPos(){

        if (getParent() == null)
            return null;

        return super.getPos();
    }

    public abstract String getUnlocalizedName();

    private PartInfo partInfo;

    @Override
    public boolean renderBreaking(RenderContext context, IVertexConsumer consumer, QRayTraceResult hit, TextureAtlasSprite overrideIcon) {
        return renderStatic(context, consumer, 0);
    }

    @Override
    public List<Cuboid> getOcclusionBoxes() {

        return new ArrayList<Cuboid>();
    }

    @Override
    public List<Cuboid> getSelectionBoxes() {

        return new ArrayList<Cuboid>();
    }

    @Override
    public ItemStack getPickBlock(EntityPlayer player, QRayTraceResult hit) {
        if (partInfo == null)
            partInfo = PartManager.getPartInfo(getType());

        return partInfo.getStack();
    }

    public List<ItemStack> getSubItems() {
        return Arrays.asList(getPickBlock(null, null));
    }

    @Override
    public void onPartChanged(IQLPart part) {

        if (getWorld() != null && !getWorld().isRemote)
            onUpdate();
    }

    @Override
    public void onNeighborBlockChange() {

        if (getWorld() != null && !getWorld().isRemote)
            onUpdate();
    }

    @Override
    public void onNeighborTileChange() {

    }

    @Override
    public void onAdded() {

        if (getWorld() != null && !getWorld().isRemote)
            onUpdate();
    }

    @Override
    public void onRemoved() {

    }

    @Override
    public void onLoaded() {

        if (getWorld() != null && !getWorld().isRemote)
            onUpdate();
    }

    @Override
    public void onUnloaded() {

    }

    @Override
    public void onConverted() {

        if (!getWorld().isRemote)
            onUpdate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addWAILABody(List<String> text) {

    }

    @Override
    public boolean canCopy(ItemStack templateStack, ItemStack outputStack) {

        return false;
    }

    @Override
    public boolean canGoInCopySlot(ItemStack stack) {

        return false;
    }

    @Override
    public List<ItemStack> getItemsOnStack(ItemStack stack) {

        return new ArrayList<ItemStack>();
    }

    @Override
    public boolean onActivated(EntityPlayer player, QRayTraceResult hit, ItemStack item) {

        return false;
    }

    @Override
    public void onClicked(EntityPlayer player, QRayTraceResult hit, ItemStack item) {

    }

    public void onUpdate() {

    }

    public void notifyUpdate() {

        getWorld().updateObservingBlocksAt(getPos(), Blocks.AIR);
    }

    public CreativeTabs[] getCreativeTabs() {

        return new CreativeTabs[] { getCreativeTab() };
    }

    public CreativeTabs getCreativeTab() {

        return null;
    }

    public SoundType getPlacementSound() {

        return SoundType.GLASS;
    }

    @SideOnly(Side.CLIENT)
    public void addTooltip(ItemStack item, List<String> tip) {

    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap reg) {

    }

}
