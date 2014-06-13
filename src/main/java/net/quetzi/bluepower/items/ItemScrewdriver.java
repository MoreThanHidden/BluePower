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

package net.quetzi.bluepower.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.quetzi.bluepower.api.part.BPPart;
import net.quetzi.bluepower.api.vec.Vector3;
import net.quetzi.bluepower.compat.CompatibilityUtils;
import net.quetzi.bluepower.compat.fmp.IMultipartCompat;
import net.quetzi.bluepower.init.BPBlocks;
import net.quetzi.bluepower.init.CustomTabs;
import net.quetzi.bluepower.references.Dependencies;
import net.quetzi.bluepower.references.Refs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemScrewdriver extends Item {

    public ItemScrewdriver() {
    
        this.setUnlocalizedName(Refs.SCREWDRIVER_NAME);
        this.setCreativeTab(CustomTabs.tabBluePowerTools);
        this.setMaxDamage(250);
        this.setMaxStackSize(1);
        this.setTextureName(Refs.MODID + ":" + this.getUnlocalizedName().substring(5));
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

        Block block = world.getBlock(x, y, z);
        
        if(block == BPBlocks.multipart){
            IMultipartCompat compat = (IMultipartCompat) CompatibilityUtils.getModule(Dependencies.FMP);
            MovingObjectPosition mop = player.rayTrace(player.capabilities.isCreativeMode ? 5 : 4.5, 0);
            BPPart p = compat.getClickedPart(new Vector3(x, y, z, world), new Vector3(hitX, hitY, hitZ), mop, stack, player);
            
            if(p != null && player.isSneaking()){
                p.onActivated(player, new MovingObjectPosition(x, y, z, side, Vec3.createVectorHelper(x + hitX, y + hitY, z + hitZ)), stack);
            }
            
            return false;
        }
        
        block.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side));
        if (!player.capabilities.isCreativeMode) {
            stack.setItemDamage(stack.getItemDamage() + 1);
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D() {
    
        return true;
    }
    
    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
    
        return EnumAction.block;
    }
}
