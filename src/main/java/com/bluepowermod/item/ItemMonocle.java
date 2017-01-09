package com.bluepowermod.item;

import com.bluepowermod.BluePower;
import com.bluepowermod.init.BPCreativeTabs;
import com.bluepowermod.reference.Refs;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class ItemMonocle extends ItemArmor {

    public static ArmorMaterial material_monocle = EnumHelper.addArmorMaterial(Refs.MONOCLE_NAME,Refs.MODID + ":" + Refs.MONOCLE_NAME, 1024, new int[] { 0, 0, 0, 0 }, 0, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 0);

    @SideOnly(Side.CLIENT)
    public static KeyBinding keybind = new KeyBinding("key." + Refs.MODID + ":monocle", Keyboard.KEY_M, "key.categories.misc");
    public static boolean active = false;
    public static float fovMultipier = 1;
    public static float rate = 0.05F;

    public ItemMonocle() {

        super(material_monocle, 0, EntityEquipmentSlot.HEAD); // RenderingRegistry.addNewArmourRendererPrefix(Refs.MODID + ":" + Refs.MONOCLE_NAME)

        setMaxDamage(0);
        setCreativeTab(BPCreativeTabs.items);
        setUnlocalizedName(Refs.MONOCLE_NAME);

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {

        return "item." + Refs.MODID + ":" + Refs.MONOCLE_NAME + (stack.getItemDamage() == 1 ? "_magnifying" : "");
    }

    @Override
    public boolean isRepairable() {

        return false;
    }

    @Override
    public boolean getHasSubtypes() {

        return true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> l) {
        l.add(new ItemStack(this, 1, 0));
        l.add(new ItemStack(this, 1, 1));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        EntityPlayer player = BluePower.proxy.getPlayer();
        if (player == null)
            return;
        ItemStack helm = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        if (active)
            fovMultipier = Math.max(fovMultipier - rate, 0.25F);
        else
            fovMultipier = Math.min(fovMultipier + rate, 1);

        if (helm == null || helm.getItem() != this || helm.getItemDamage() != 1) {
            if (active) {
                active = false;
                player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Toggled Magnificent Magnifying Monocle: "
                        + (active ? TextFormatting.GREEN + "Active" : TextFormatting.RED + "Inactive")));
            }
            return;
        }

        if (keybind.isPressed()) {
            active = !active;
            player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Toggled Magnificent Magnifying Monocle: "
                    + (active ? TextFormatting.GREEN + "Active" : TextFormatting.RED + "Inactive")));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event) {

        event.setNewfov(event.getFov() * fovMultipier);
    }
}
