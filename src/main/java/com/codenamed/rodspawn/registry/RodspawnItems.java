package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RodspawnItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Rodspawn.MOD_ID);

    //public static final DeferredItem<Item> NETHER_DUST = ITEMS.register("nether_dust", () ->
    //        new Item(new Item.Properties()));

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
