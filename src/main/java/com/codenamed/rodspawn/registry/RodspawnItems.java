package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import com.codenamed.rodspawn.item.SpearItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RodspawnItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Rodspawn.MOD_ID);

    public static final DeferredItem<Item> NETHER_DUST = ITEMS.register("nether_dust", () ->
            new Item(new Item.Properties()));

    public static final DeferredItem<Item> SPEAR = ITEMS.register("spear", () ->
            new MaceItem((new Item.Properties()).rarity(Rarity.EPIC).durability(500).component(DataComponents.TOOL, SpearItem.createToolProperties()).attributes(SpearItem.createAttributes())));
    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
