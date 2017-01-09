/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.tile.tier1;

import com.bluepowermod.helper.ItemStackHelper;
import com.bluepowermod.init.BPBlocks;
import com.bluepowermod.part.IGuiButtonSensitive;
import com.bluepowermod.part.tube.TubeStack;
import com.bluepowermod.tile.TileMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;

import java.util.List;



/**
 * @author MineMaarten
 */
public class TileItemDetector extends TileMachineBase implements ISidedInventory, IGuiButtonSensitive {

    public int mode;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(10, ItemStack.EMPTY);
    private int savedPulses = 0;
    public int fuzzySetting;

    @Override
    public void update() {

        super.update();
        if (!world.isRemote) {
            if (mode == 0 || mode == 1) {
                if (world.getWorldTime() % 2 == 0) {
                    if (getOutputtingRedstone() > 0) {
                        this.setOutputtingRedstone(false);
                        sendUpdatePacket();
                    } else if (savedPulses > 0) {
                        savedPulses--;
                        setOutputtingRedstone(true);
                        sendUpdatePacket();
                    }
                }
            } else {
                if (isBufferEmpty() == getOutputtingRedstone() > 0) {
                    setOutputtingRedstone(!isBufferEmpty());
                    sendUpdatePacket();
                }
            }
        }
    }

    @Override
    public boolean isEjecting() {

        return getOutputtingRedstone() > 0;
    }

    @Override
    public TubeStack acceptItemFromTube(TubeStack stack, EnumFacing from, boolean simulate) {

        if (from == getFacingDirection() && !isItemAccepted(stack.stack))
            return stack;
        TubeStack remainder = super.acceptItemFromTube(stack, from, simulate);
        if (remainder == null && !simulate && mode < 2) {
            savedPulses += mode == 0 ? stack.stack.getCount() : 1;
        }
        return remainder;
    }

    private boolean isItemAccepted(ItemStack item) {

        boolean everythingNull = true;
        for (ItemStack invStack : inventory) {
            if (!invStack.isEmpty()) {
                if (ItemStackHelper.areStacksEqual(invStack, item, fuzzySetting)) {
                    return true;
                }
                everythingNull = false;
            }
        }
        return everythingNull;
    }

    /**
     * This function gets called whenever the world/chunk loads
     */
    @Override
    public void readFromNBT(NBTTagCompound tCompound) {

        super.readFromNBT(tCompound);
        for (int i = 0; i < 9; i++) {
            NBTTagCompound tc = tCompound.getCompoundTag("inventory" + i);
            inventory.set(i, new ItemStack(tc));
        }

        mode = tCompound.getByte("mode");
        fuzzySetting = tCompound.getByte("fuzzySetting");
        savedPulses = tCompound.getInteger("savedPulses");
    }

    /**
     * This function gets called whenever the world/chunk is saved
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tCompound) {

        super.writeToNBT(tCompound);

        for (int i = 0; i < 9; i++) {
                NBTTagCompound tc = new NBTTagCompound();
                inventory.get(i).writeToNBT(tc);
                tCompound.setTag("inventory" + i, tc);
        }

        tCompound.setByte("mode", (byte) mode);
        tCompound.setByte("fuzzySetting", (byte) fuzzySetting);
        tCompound.setInteger("savedPulses", savedPulses);

        return tCompound;
    }

    @Override
    public int getSizeInventory() {

        return inventory.size();
    }

    @Override
    public ItemStack getStackInSlot(int i) {

        return inventory.get(i);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {

        ItemStack itemStack = getStackInSlot(slot);
        if (!itemStack.isEmpty()) {
            if (itemStack.getCount() <= amount) {
                setInventorySlotContents(slot, ItemStack.EMPTY);
            } else {
                itemStack = itemStack.splitStack(amount);
                if (itemStack.getCount() == 0) {
                    setInventorySlotContents(slot, ItemStack.EMPTY);
                }
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        ItemStack itemStack = getStackInSlot(i);
        if (!itemStack.isEmpty()) {
            setInventorySlotContents(i, ItemStack.EMPTY);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemStack) {

        inventory.set(i, itemStack);
    }

    @Override
    public String getName() {

        return BPBlocks.item_detector.getUnlocalizedName();
    }

    @Override
    public boolean hasCustomName() {

        return false;
    }

    @Override
    public int getInventoryStackLimit() {

        return 64;
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
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {

        return true;
    }

    @Override
    public List<ItemStack> getDrops() {

        List<ItemStack> drops = super.getDrops();
        for (ItemStack stack : inventory)
            if (!stack.isEmpty())
                drops.add(stack);
        return drops;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        EnumFacing direction = getFacingDirection();

        if (side == direction || side == direction.getOpposite()) {
            return new int[] {};
        }
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }


    @Override
    public void onButtonPress(EntityPlayer player, int messageId, int value) {

        if (messageId == 0)
            mode = value;
        if (messageId == 1) {
            fuzzySetting = value;
        }
    }

    @Override
    public boolean canConnectRedstone() {

        return true;
    }

    //Todo Fields
    @Override
    public boolean isEmpty() {
        return inventory.size() == 0;
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

}
