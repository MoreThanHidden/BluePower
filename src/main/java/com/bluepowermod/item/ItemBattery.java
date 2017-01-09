package com.bluepowermod.item;

import com.bluepowermod.api.power.IRechargeable;
import com.bluepowermod.init.BPCreativeTabs;
import com.bluepowermod.reference.Refs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * @author Koen Beckers (K4Unl)
 */
public class ItemBattery extends ItemBase implements IRechargeable {

    private final int maxVoltage;

    public ItemBattery(int maxVoltage_) {

        super();

        setUnlocalizedName(Refs.BATTERY_ITEM_NAME);
        setCreativeTab(BPCreativeTabs.power);
        setRegistryName(Refs.MODID + ":" + Refs.BATTERY_ITEM_NAME);
        setMaxStackSize(1);
        maxVoltage = maxVoltage_;
        setMaxDamage(maxVoltage);
        setNoRepair();
    }

    @Override
    public int getVoltage(ItemStack stack) {

        return maxVoltage - stack.getItemDamage();
    }

    @Override
    public int getMaxVoltage() {

        return maxVoltage;
    }

    @Override
    public int addEnergy(ItemStack stack, int voltage) {
        int oldAmp = getVoltage(stack);
        if (voltage > 0) {
            if (voltage + getVoltage(stack) < getMaxVoltage()) {
                stack.setItemDamage(getMaxDamage() - (getVoltage(stack) + voltage));
            } else {
                stack.setItemDamage(0/*getMaxDamage()*/);
            }
        } else {
            if (getVoltage(stack) + voltage > 0) {
                stack.setItemDamage(getMaxDamage() - (getVoltage(stack) + voltage));
            } else {
                stack.setItemDamage(maxVoltage);
            }
        }
        return getVoltage(stack) - oldAmp;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        subItems.add(new ItemStack(itemIn, 1, 0));
        subItems.add(new ItemStack(itemIn, 1, getMaxDamage()));
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4) {
        infoList.add("Charge: " + Math.round(((double)getVoltage(stack) / (double)getMaxVoltage()) * 100) + "%");
    }
}
