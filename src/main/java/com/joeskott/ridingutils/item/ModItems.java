package com.joeskott.ridingutils.item;

import com.joeskott.ridingutils.RidingUtils;
import com.joeskott.ridingutils.item.custom.RidingCropItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RidingUtils.MOD_ID);

    // ITEM DEFINITIONS
    public static final RegistryObject<Item> RIDING_CROP = ITEMS.register("riding_crop",
            () -> new RidingCropItem(new Item.Properties().tab(CreativeModeTab.TAB_TRANSPORTATION)));

    public static final RegistryObject<Item> REINS = ITEMS.register("reins",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_TRANSPORTATION)));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
