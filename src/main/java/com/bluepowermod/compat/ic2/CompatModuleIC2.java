/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package com.bluepowermod.compat.ic2;

import com.bluepowermod.compat.CompatModule;
import com.bluepowermod.init.BPBlocks;
import com.bluepowermod.init.BPItems;
import ic2.api.recipe.Recipes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.GameData;

public class CompatModuleIC2 extends CompatModule {

    @Override
    public void preInit(FMLPreInitializationEvent ev) {

    }

    @Override
    public void init(FMLInitializationEvent ev) {

        Recipes.macerator.addRecipe(new IC2RecipeInput(new ItemStack(BPBlocks.zinc_ore)), null, false, new ItemStack(BPItems.zinc_ore_crushed, 2));
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("amount", 1000);
        Recipes.oreWashing.addRecipe(new IC2RecipeInput(new ItemStack(BPItems.zinc_ore_crushed)), tag, false, new ItemStack(
                BPItems.zinc_ore_purified), new ItemStack(BPItems.zinc_tiny_dust, 2),
                new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("IC2:itemDust")), 1, 9));

        tag = new NBTTagCompound();
        tag.setInteger("minHeat", 2000);
        Recipes.centrifuge.addRecipe(new IC2RecipeInput(new ItemStack(BPItems.zinc_ore_purified)), tag, false, new ItemStack(BPItems.zinc_dust),
                new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("IC2:itemDustSmall")), 1, 6));
        Recipes.centrifuge.addRecipe(new IC2RecipeInput(new ItemStack(BPItems.zinc_ore_crushed)), tag, false, new ItemStack(BPItems.zinc_dust),
                new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("IC2:itemDustSmall")), 1, 6), new ItemStack(Item.REGISTRY
                        .getObject(new ResourceLocation("IC2:itemDust")), 1, 9));

    }

    @Override
    public void postInit(FMLPostInitializationEvent ev) {

    }

    @Override
    public void registerBlocks() {

    }

    @Override
    public void registerItems() {

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerRenders() {

    }
}
