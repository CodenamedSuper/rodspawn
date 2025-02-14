package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import com.codenamed.rodspawn.entity.Wildfire;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RodspawnEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Rodspawn.MOD_ID);

    public static final Supplier<EntityType<Wildfire>> WILDFIRE =
            ENTITY_TYPES.register("wildfire", () -> EntityType.Builder.of(Wildfire::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).fireImmune().build("wildfire"));



    public static void init(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
