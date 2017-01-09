/*
 * This file is part of Blue Power.
 *
 *      Blue Power is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Blue Power is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

package com.bluepowermod.item;

import com.bluepowermod.api.block.IAdvancedSilkyRemovable;
import com.bluepowermod.api.block.ISilkyRemovable;
import com.bluepowermod.init.BPCreativeTabs;
import com.bluepowermod.reference.Refs;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.qmunity.lib.block.BlockMultipart;
import uk.co.qmunity.lib.part.IPartHolder;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.part.MultipartCompat;
import uk.co.qmunity.lib.raytrace.QRayTraceResult;
import uk.co.qmunity.lib.raytrace.RayTracer;
import uk.co.qmunity.lib.tile.TileMultipart;

import static uk.co.qmunity.lib.block.BlockMultipart.findTile;


public class ItemSilkyScrewdriver extends ItemBase {

    public ItemSilkyScrewdriver() {

        setUnlocalizedName(Refs.SILKYSCREWDRIVER_NAME);
        setCreativeTab(BPCreativeTabs.tools);
        setMaxDamage(250);
        setMaxStackSize(1);
        setRegistryName(Refs.MODID + ":" + Refs.SILKYSCREWDRIVER_NAME);
    }


    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        Block block = world.getBlockState(pos).getBlock();
        TileMultipart te = findTile(world, pos);
        ItemStack stack = player.getHeldItem(hand);

        IPartHolder h = MultipartCompat.getHolder(world, pos);
        if (h != null && te != null && h instanceof BlockMultipart) {
            QRayTraceResult mop = te.rayTrace(RayTracer.getStartVec(player), RayTracer.getEndVec(player));
            if (mop != null) {
                IQLPart p = mop.part;
                if (p instanceof ISilkyRemovable && !world.isRemote) {
                    if (p instanceof IAdvancedSilkyRemovable && !((IAdvancedSilkyRemovable) p).preSilkyRemoval(world, pos))
                        return EnumActionResult.PASS;
                    NBTTagCompound tag = new NBTTagCompound();
                    boolean hideTooltip = false;
                    if (p instanceof IAdvancedSilkyRemovable) {
                        hideTooltip = ((IAdvancedSilkyRemovable) p).writeSilkyData(world, pos, tag);
                    } else {
                        p.writeToNBT(tag);
                    }
                    ItemStack droppedStack = p.getPickBlock(null, null);
                    NBTTagCompound stackTag = droppedStack.getTagCompound();
                    if (stackTag == null) {
                        stackTag = new NBTTagCompound();
                        droppedStack.setTagCompound(stackTag);
                    }
                    stackTag.setTag("tileData", tag);
                    stackTag.setBoolean("hideSilkyTooltip", hideTooltip);

                    world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, droppedStack));
                    h.removePart(p);
                    if (p instanceof IAdvancedSilkyRemovable)
                        ((IAdvancedSilkyRemovable) p).postSilkyRemoval(world, pos);
                    stack.damageItem(1, player);
                    return EnumActionResult.SUCCESS;
                }
            }

            return EnumActionResult.PASS;
        }

        if (block instanceof ISilkyRemovable && !world.isRemote) {
            if (block instanceof IAdvancedSilkyRemovable && !((IAdvancedSilkyRemovable) block).preSilkyRemoval(world, pos))
                return EnumActionResult.PASS;
            if (te == null)
                throw new IllegalStateException(
                        "Block doesn't have a TileEntity?! Implementers of ISilkyRemovable should have one. Offender: "
                                + block.getUnlocalizedName());
            NBTTagCompound tag = new NBTTagCompound();
            te.writeToNBT(tag);
            IBlockState state = world.getBlockState(pos);
            Item item = block.getItemDropped(state, itemRand, 0);
            if (item == null)
                throw new NullPointerException("Block returns null for getItemDropped(meta, rand, fortune)! Offender: "
                        + block.getUnlocalizedName());
            ItemStack droppedStack = new ItemStack(item, 1, block.damageDropped(state));
            NBTTagCompound stackTag = droppedStack.getTagCompound();
            if (stackTag == null) {
                stackTag = new NBTTagCompound();
                droppedStack.setTagCompound(stackTag);
            }
            stackTag.setTag("tileData", tag);
            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, droppedStack));
            world.setBlockToAir(pos);
            if (block instanceof IAdvancedSilkyRemovable)
                ((IAdvancedSilkyRemovable) block).postSilkyRemoval(world, pos);
            stack.damageItem(1, player);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D() {

        return true;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {

        return EnumAction.BLOCK;
    }
}
