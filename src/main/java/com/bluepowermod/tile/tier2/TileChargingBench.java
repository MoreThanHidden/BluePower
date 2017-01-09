package com.bluepowermod.tile.tier2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import uk.co.qmunity.lib.network.annotation.DescSynced;
import uk.co.qmunity.lib.network.annotation.GuiSynced;

import com.bluepowermod.api.power.IPowerBase;
import com.bluepowermod.api.power.IPowered;
import com.bluepowermod.api.power.IRechargeable;
import com.bluepowermod.init.BPBlocks;
import com.bluepowermod.reference.PowerConstants;
import com.bluepowermod.tile.TileBluePowerBase;

/**
 * @author Koen Beckers (K-4U)
 */
public class TileChargingBench extends TileBluePowerBase implements IPowered, IInventory {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(24, ItemStack.EMPTY);

    @DescSynced
    private int textureIndex;

    @GuiSynced
    private final IPowerBase powerBase = getPowerHandler(null);
    private final int powerTransfer = 2;

    @GuiSynced
    private int energyBuffer;

    public static final int MAX_ENERGY_BUFFER = 100;

    @Override
    public void update() {

        super.update();

        if (!getWorld().isRemote) {
            if (isPowered() && energyBuffer < 100) {
                fillPowerBuffer();
            }

            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.get(i) != ItemStack.EMPTY && inventory.get(i).getItem() instanceof IRechargeable) {
                    IRechargeable battery = (IRechargeable) inventory.get(i).getItem();
                    energyBuffer -= battery.addEnergy(inventory.get(i), Math.min((int) PowerConstants.CHARGINGBENCH_CHARGING_TRANSFER, energyBuffer));
                }
            }
            if (world.getWorldTime() % 20 == 0)
                recalculateTextureIndex();
        }

        if (world.getWorldTime() % 20 == 0)
            recalculateTextureIndex();
    }

    private void fillPowerBuffer() {

        energyBuffer += PowerConstants.CHARGINGBENCH_POWERTRANSFER;
        powerBase.addEnergy(-PowerConstants.CHARGINGBENCH_POWERTRANSFER, false);
    }

    private void recalculateTextureIndex() {

        textureIndex = (int) Math.floor((double) energyBuffer / MAX_ENERGY_BUFFER * 4.0);
    }

    public int getTextureIndex() {

        return textureIndex;
    }

    public double getBufferPercentage() {

        return (double) energyBuffer / MAX_ENERGY_BUFFER;
    }

    @Override
    public int getSizeInventory() {

        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.size() == 0;
    }

    @Override
    public ItemStack getStackInSlot(int index) {

        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {

        ItemStack itemStack = getStackInSlot(index);
        if (itemStack != null) {
            if (itemStack.getCount() <= amount) {
                setInventorySlotContents(index, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if (itemStack.getCount() == 0) {
                    setInventorySlotContents(index, null);
                }
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemStack = getStackInSlot(index);
        if (itemStack != null) {
            setInventorySlotContents(index, ItemStack.EMPTY);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack toSet) {

        inventory.set(index, toSet);
    }

    @Override
    public String getName() {
        return BPBlocks.chargingBench.getUnlocalizedName();
    }

    @Override
    public boolean hasCustomName() {

        return true;
    }

    @Override
    public int getInventoryStackLimit() {

        return 1;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }


    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemToTest) {

        return itemToTest != null && itemToTest.getItem() instanceof IRechargeable;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    /**
     * This function gets called whenever the world/chunk loads
     */
    @Override
    public void readFromNBT(NBTTagCompound tCompound) {

        super.readFromNBT(tCompound);

        for (int i = 0; i < 24; i++) {
            NBTTagCompound tc = tCompound.getCompoundTag("inventory" + i);
            inventory.set(i, new ItemStack(tc));
        }
        energyBuffer = tCompound.getInteger("energyBuffer");
    }

    /**
     * This function gets called whenever the world/chunk is saved
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tCompound) {

        super.writeToNBT(tCompound);

        for (int i = 0; i < 24; i++) {
            if (inventory.get(i) != ItemStack.EMPTY) {
                NBTTagCompound tc = new NBTTagCompound();
                inventory.get(i).writeToNBT(tc);
                tCompound.setTag("inventory" + i, tc);
            }
        }
        tCompound.setInteger("energyBuffer", energyBuffer);
    }
}
