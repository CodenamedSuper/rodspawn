package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import com.codenamed.rodspawn.block.entity.BrazierBlockEntity;
import com.codenamed.rodspawn.block.entity.NetherSpawnerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RodspawnBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Rodspawn.MOD_ID);

    public static final Supplier<BlockEntityType<NetherSpawnerBlockEntity>> NETHER_SPAWNER = BLOCK_ENTITIES.register("nether_spawner",
            () -> BlockEntityType.Builder.of(NetherSpawnerBlockEntity::new, RodspawnBlocks.NETHER_SPAWNER.get()).build(null));

    public static final Supplier<BlockEntityType<BrazierBlockEntity>> BRAZIER = BLOCK_ENTITIES.register("brazier",
            () -> BlockEntityType.Builder.of(BrazierBlockEntity::new, RodspawnBlocks.BRAZIER.get()).build(null));

    public static void init(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}