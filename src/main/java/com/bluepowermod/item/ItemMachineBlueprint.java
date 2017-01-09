package com.bluepowermod.item;

import com.bluepowermod.api.item.IMachineBlueprint;
import com.bluepowermod.init.BPCreativeTabs;
import com.bluepowermod.reference.Refs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ItemMachineBlueprint extends ItemBase implements IMachineBlueprint {

    public ItemMachineBlueprint() {

        this.setCreativeTab(BPCreativeTabs.tools);
        this.setUnlocalizedName(Refs.MACHINE_BLUEPRINT_NAME);
        this.setMaxStackSize(1);
    }

    @Override
    public int getDamage(ItemStack stack) {

        return getStoredSettings(stack) != null ? 1 : 0;
    }

    @Override
    public void saveMachineSettings(ItemStack blueprint, String type, NBTTagCompound settings) {

        blueprint.setTagCompound(new NBTTagCompound());
        blueprint.getTagCompound().setString("type", type);
        blueprint.getTagCompound().setTag("settings", settings);
    }

    @Override
    public void clearSettings(ItemStack blueprint) {

        blueprint.setTagCompound(null);
    }

    @Override
    public String getStoredSettingsType(ItemStack blueprint) {

        return blueprint.getTagCompound() == null || !blueprint.getTagCompound().hasKey("type") ? null : blueprint.getTagCompound().getString("type");
    }

    @Override
    public NBTTagCompound getStoredSettings(ItemStack blueprint) {

        return blueprint.getTagCompound() == null || !blueprint.getTagCompound().hasKey("settings") ? null : blueprint.getTagCompound()
                .getCompoundTag("settings");
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && player.isSneaking() && getStoredSettings(player.getHeldItem(hand)) != null) {
            clearSettings(player.getHeldItem(hand));
            player.sendMessage(new TextComponentString("Cleared Machine Blueprint"));
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }


}
