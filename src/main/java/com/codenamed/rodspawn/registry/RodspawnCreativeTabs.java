package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RodspawnCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Rodspawn.MOD_ID);

    public static final Supplier<CreativeModeTab> RODSPAWN_FIRE = CREATIVE_MODE_TAB.register("rodspawn_fire",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(RodspawnItems.SPEAR.get()))
                    .title(Component.translatable("creativetab.rodspawn.rodspawn_fire"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(RodspawnBlocks.NETHER_PILLAR);
                        output.accept(RodspawnBlocks.GILDED_NETHER_BRICKS);
                        output.accept(RodspawnBlocks.NETHER_SPAWNER);
                        output.accept(RodspawnBlocks.BRAZIER);
                        output.accept(RodspawnItems.NETHER_DUST);
                        output.accept(RodspawnItems.OMINOUS_NETHER_DUST);
                        output.accept(RodspawnItems.SPEARHEAD);
                        output.accept(RodspawnItems.SPEAR);
                        output.accept(RodspawnItems.WILDFIRE_SPAWN_EGG);


                    }).build());

    public static void init(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
