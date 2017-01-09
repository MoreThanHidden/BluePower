/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.tile.tier2;

import com.bluepowermod.api.tube.IPneumaticTube.TubeColor;
import com.bluepowermod.helper.IOHelper;
import com.bluepowermod.helper.ItemStackHelper;
import com.bluepowermod.init.BPBlocks;
import com.bluepowermod.part.IGuiButtonSensitive;
import com.bluepowermod.part.tube.TubeStack;
import com.bluepowermod.tile.TileMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;

import java.util.List;



/**
 * @author MineMaarten
 */
public class TileRegulator extends TileMachineBase implements ISidedInventory, IGuiButtonSensitive {

    private NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    public TubeColor color = TubeColor.NONE;
    public int mode;
    public int fuzzySetting;

    private enum EnumSection {
        INPUT_FILTER, BUFFER, OUTPUT_FILTER
    }

    @Override
    public void update() {

        super.update();
        if (!world.isRemote && isBufferEmpty()) {
            boolean ratiosMatch = true;
            for (int i = 0; i < 9; i++) {
                if (!inventory.get(i).isEmpty()) {
                    int inputFilterItems = getItemsInSection(inventory.get(i), EnumSection.INPUT_FILTER);
                    int bufferItems = getItemsInSection(inventory.get(i), EnumSection.BUFFER);
                    if (bufferItems < inputFilterItems) {
                        ratiosMatch = false;
                        break;
                    }
                }
            }
            if (ratiosMatch && !isEjecting())
                checkIndividualOutputFilterAndEject();

            if (mode == 1 && !isEjecting()) {// supply mode
                IInventory inv = IOHelper.getInventoryForTE(getTileCache(getOutputDirection()));
                if (inv != null) {
                    int[] accessibleSlots;
                    if (inv instanceof ISidedInventory) {
                        accessibleSlots = ((ISidedInventory) inv).getSlotsForFace(getFacingDirection());
                    } else {
                        accessibleSlots = new int[inv.getSizeInventory()];
                        for (int i = 0; i < accessibleSlots.length; i++)
                            accessibleSlots[i] = i;
                    }
                    for (int i = 18; i < 27; i++) {
                        if (!inventory.get(i).isEmpty()) {
                            int outputFilterItems = getItemsInSection(inventory.get(i), EnumSection.OUTPUT_FILTER);
                            int supplyingInvCount = 0;
                            for (int slot : accessibleSlots) {
                                ItemStack stackInSlot = inv.getStackInSlot(slot);
                                if (!stackInSlot.isEmpty() && ItemStackHelper.areStacksEqual(stackInSlot, inventory.get(i), fuzzySetting)
                                        && IOHelper.canInsertItemToInventory(inv, inventory.get(i), slot, getFacingDirection().ordinal())) {
                                    supplyingInvCount += stackInSlot.getCount();
                                }
                            }
                            if (supplyingInvCount < outputFilterItems) {
                                ItemStack requestedStack = inventory.get(i).copy();
                                requestedStack.setCount(outputFilterItems - supplyingInvCount);
                                ItemStack bufferItems = IOHelper.extract(this, null, requestedStack, true, false, fuzzySetting);// try
                                                                                                                                                  // to
                                                                                                                                                  // extract
                                // the items
                                // needed to fully
                                // supply the
                                // inventory from
                                // the buffer.
                                if (!bufferItems.isEmpty()) {
                                    ItemStack remainder = IOHelper.insert(inv, bufferItems, getFacingDirection().ordinal(), false);// insert into
                                                                                                                                   // supplying inv.
                                    if (!remainder.isEmpty()) {
                                        IOHelper.insert(this, remainder, null, false);// when not every item can be supplied, return
                                                                                                        // those to the buffer.
                                    }
                                }
                            }
                        }
                    }
                }
            }
            boolean shouldEmitRedstone = isSatisfied() || animationTicker >= 0;
            if (isEjecting() != shouldEmitRedstone) {
                setOutputtingRedstone(shouldEmitRedstone);
                sendUpdatePacket();
            }
        }
    }

    @Override
    public boolean isEjecting() {

        return super.isEjecting() || getOutputtingRedstone() > 0;
    }

    /**
     * Returns true if the supplying inventory has the items stated in the output filter.
     * 
     * @return
     */
    private boolean isSatisfied() {

        IInventory inv = IOHelper.getInventoryForTE(getTileCache(getOutputDirection()));
        if (inv != null) {
            int[] accessibleSlots;
            if (inv instanceof ISidedInventory) {
                accessibleSlots = ((ISidedInventory) inv).getSlotsForFace(getFacingDirection());
            } else {
                accessibleSlots = new int[inv.getSizeInventory()];
                for (int i = 0; i < accessibleSlots.length; i++)
                    accessibleSlots[i] = i;
            }
            boolean everythingNull = true;
            for (int i = 18; i < 27; i++) {
                if (!inventory.get(i).isEmpty()) {
                    everythingNull = false;
                    int outputFilterItems = getItemsInSection(inventory.get(i), EnumSection.OUTPUT_FILTER);
                    int supplyingInvCount = 0;
                    for (int slot : accessibleSlots) {
                        ItemStack stackInSlot = inv.getStackInSlot(slot);
                        if (!stackInSlot.isEmpty() && ItemStackHelper.areStacksEqual(stackInSlot, inventory.get(i), fuzzySetting)
                                && IOHelper.canInsertItemToInventory(inv, inventory.get(i), slot, getFacingDirection().ordinal())) {
                            supplyingInvCount += stackInSlot.getCount();
                        }
                    }
                    if (supplyingInvCount < outputFilterItems)
                        return false;
                }
            }
            return !everythingNull;
        }
        return false;
    }

    private void checkIndividualOutputFilterAndEject() {

        // Check in output filter for every slot and look if the items are present in the buffer.
        for (int i = 0; i < 9; i++) {
            if (!inventory.get(i).isEmpty()) {
                int inputFilterItems = getItemsInSection(inventory.get(i), EnumSection.INPUT_FILTER);
                int bufferItems = getItemsInSection(inventory.get(i), EnumSection.BUFFER);
                if (bufferItems >= inputFilterItems) {
                    ItemStack stackFromBuffer = IOHelper.extract(this, null, inventory.get(i), true, false, fuzzySetting);
                    this.addItemToOutputBuffer(stackFromBuffer, color);
                }
            }
        }
    }

    @Override
    public TubeStack acceptItemFromTube(TubeStack stack, EnumFacing from, boolean simulate) {

        if (from == getFacingDirection() && isBufferEmpty()) {
            stack = stack.copy();
            int bufferItems = getItemsInSection(stack.stack, EnumSection.BUFFER);
            int inputFilterItems = getItemsInSection(stack.stack, EnumSection.INPUT_FILTER);
            int allowedItems = inputFilterItems - bufferItems;
            if (allowedItems <= 0)
                return stack;

            ItemStack acceptedStack = stack.stack.splitStack(Math.min(allowedItems, stack.stack.getCount()));

            if (!acceptedStack.isEmpty() && acceptedStack.getCount() > 0) {
                for (int i = EnumSection.INPUT_FILTER.ordinal() * 9; i < EnumSection.INPUT_FILTER.ordinal() * 9 + 9; i++) {
                    if (!inventory.get(i).isEmpty() && ItemStackHelper.areStacksEqual(acceptedStack, inventory.get(i), fuzzySetting)) {
                        //TODO Check this as had unknown ordinal
                        acceptedStack = IOHelper.insert(this, acceptedStack, EnumSection.BUFFER.ordinal() * 9 + i, 0, simulate);

                        if (acceptedStack.isEmpty()) {
                            break;
                        }
                    }
                }

                if (!acceptedStack.isEmpty() && acceptedStack.getCount() != 0) {
                    //TODO Check this as had unknown ordinal
                    ItemStack remainder = IOHelper.insert(this, acceptedStack, 0, simulate);
                    if (!remainder.isEmpty()) {
                        stack.stack.setCount(stack.stack.getCount() + remainder.getCount());
                    }
                }
                if (stack.stack.getCount() > 0)
                    return stack;
                else
                    return null;
            } else {
                return stack;
            }

        } else {
            return super.acceptItemFromTube(stack, from, simulate);
        }
    }
    

    private int getItemsInSection(ItemStack type, EnumSection section) {

        int count = 0;
        for (int i = section.ordinal() * 9; i < section.ordinal() * 9 + 9; i++) {
            if (!inventory.get(i).isEmpty() && ItemStackHelper.areStacksEqual(type, inventory.get(i), fuzzySetting))
                count += inventory.get(i).getCount();
        }
        return count;
    }

    @Override
    public void onButtonPress(EntityPlayer player, int messageId, int value) {

        if (messageId == 1) {
            mode = value;
        } else if (messageId == 0) {
            color = TubeColor.values()[value];
        } else if (messageId == 2) {
            fuzzySetting = value;
        }
    }

    @Override
    public List<ItemStack> getDrops() {

        List<ItemStack> drops = super.getDrops();
        for (int i = 9; i < 18; i++) {
            if (!inventory.get(i).isEmpty())
                drops.add(inventory.get(i));
        }
        return drops;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {

        super.writeToNBT(tag);

        tag.setByte("filterColor", (byte) color.ordinal());
        tag.setByte("mode", (byte) mode);
        tag.setByte("fuzzySetting", (byte) fuzzySetting);

        NBTTagList tagList = new NBTTagList();
        for (int currentIndex = 0; currentIndex < inventory.size(); ++currentIndex) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) currentIndex);
                inventory.get(currentIndex).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
        }
        tag.setTag("Items", tagList);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);

        color = TubeColor.values()[tag.getByte("filterColor")];
        mode = tag.getByte("mode");
        fuzzySetting = tag.getByte("fuzzySetting");

        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = NonNullList.withSize(27, ItemStack.EMPTY);
        for (int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if (slot >= 0 && slot < inventory.size()) {
                inventory.set(slot, new ItemStack(tagCompound));
            }
        }
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

        return BPBlocks.regulator.getUnlocalizedName();
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
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == getFacingDirection() || side == getOutputDirection())
            return new int[0];
        int[] slots = new int[9];
        for (int i = 9; i < 18; i++)
            slots[i - 9] = i;
        return slots;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
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
