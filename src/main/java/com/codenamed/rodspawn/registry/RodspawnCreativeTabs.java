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

    public static final Supplier<CreativeModeTab> RODSPAWN = CREATIVE_MODE_TAB.register("rodspawn",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Items.SHORT_GRASS))
                    .title(Component.translatable("creativetab.rodspawn.rodspawn"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(RodspawnBlocks.NETHER_SPAWNER);
                        output.accept(RodspawnItems.NETHER_DUST);

                    }).build());

    public static void init(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
