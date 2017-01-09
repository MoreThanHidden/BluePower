package com.bluepowermod.item;

import com.bluepowermod.api.power.IPowered;
import com.bluepowermod.init.BPCreativeTabs;
import com.bluepowermod.reference.Refs;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import uk.co.qmunity.lib.part.MultipartCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen Beckers (K4Unl)
 */
public class ItemMultimeter extends ItemBase {

    public ItemMultimeter() {

        super();

        setUnlocalizedName(Refs.MULTIMETER_NAME);
        setCreativeTab(BPCreativeTabs.power);
        setRegistryName(Refs.MODID + ":" + Refs.MULTIMETER_NAME);
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!world.isRemote) {
            TileEntity ent = world.getTileEntity(pos);
            Block block = world.getBlockState(pos).getBlock();
            IPowered part = MultipartCompat.getPart(world, pos, IPowered.class);
            if (ent != null) {
                if (ent instanceof IPowered || part != null) { // TODO: Add multipart checking
                    IPowered machine = null;

                    if (part == null) {
                        machine = (IPowered) ent;
                    } else {
                        machine = part;
                    }

                    List<String> messages = new ArrayList<String>();
                    if (machine.getPowerHandler(null) != null) {
                        messages.add(String.format("Charge: %.1f/%.1fV", machine.getPowerHandler(null).getVoltage(), machine
                                .getPowerHandler(null).getMaxVoltage()));
                    } else {
                        messages.add("No handler found!");
                    }

                    if (messages.size() > 0) {
                        for (String msg : messages) {
                            player.sendMessage(new TextComponentString(msg));
                        }
                    }

                    return EnumActionResult.SUCCESS;
                }

            }
        }
        return EnumActionResult.PASS;
    }
}
