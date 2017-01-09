package com.bluepowermod.item;

import com.bluepowermod.api.misc.IScrewdriver;
import com.bluepowermod.block.BlockContainerBase;
import com.bluepowermod.reference.PowerConstants;
import com.bluepowermod.reference.Refs;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.qmunity.lib.block.BlockMultipart;
import uk.co.qmunity.lib.part.IPartHolder;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.part.MultipartCompat;
import uk.co.qmunity.lib.raytrace.QRayTraceResult;
import uk.co.qmunity.lib.raytrace.RayTracer;
import uk.co.qmunity.lib.tile.TileMultipart;

import static uk.co.qmunity.lib.block.BlockMultipart.findTile;

/**
 * @author Koen Beckers (K-4U)
 */
public class ItemSonicScrewdriver extends ItemBattery implements IScrewdriver {

    public ItemSonicScrewdriver() {

        super(1000);//How much power this screwdriver holds.

        setUnlocalizedName(Refs.SONIC_SCREWDRIVER_NAME);
        setRegistryName(Refs.MODID + ":" + Refs.SONIC_SCREWDRIVER_NAME);
    }

    @Override
    public boolean damage(ItemStack stack, int damage, EntityPlayer player, boolean simulated) {
        if (player != null && player.capabilities.isCreativeMode)
            return true;

        if (!simulated) {
            addEnergy(stack, -(int)PowerConstants.POWER_PER_ACTION);
        }

        return getVoltage(stack) > 0;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        Block block = world.getBlockState(pos).getBlock();

        if (player.isSneaking()) {
            IPartHolder itph = MultipartCompat.getHolder(world, pos);

            if (itph != null && itph instanceof BlockMultipart) {
                TileMultipart te = findTile(world, pos);
                QRayTraceResult mop = te.rayTrace(RayTracer.getStartVec(player), RayTracer.getEndVec(player));
                if (mop == null)
                    return EnumActionResult.PASS;
                IQLPart p = mop.part;
                    if (p.onActivated(player, mop, player.getHeldItem(hand))) {
                        damage(player.getHeldItem(hand), 1, player, false);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }

        if (block instanceof BlockContainerBase) {
            if (((BlockContainerBase) block).getGuiID().ordinal() >= 0) {
                if (player.isSneaking()) {
                    if (block.rotateBlock(world, pos, side)) {
                        damage(player.getHeldItem(hand), 1, player, false);
                        return EnumActionResult.SUCCESS;
                    }
                }
            } else {
                if (!player.isSneaking() && block.rotateBlock(world, pos, side)) {
                    damage(player.getHeldItem(hand), 1, player, false);
                    return EnumActionResult.SUCCESS;
                }
            }
        } else {
            if (!player.isSneaking() && block.rotateBlock(world, pos, side)) {
                damage(player.getHeldItem(hand), 1, player, false);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
}
