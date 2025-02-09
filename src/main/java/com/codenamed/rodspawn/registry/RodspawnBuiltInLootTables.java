package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import com.codenamed.rodspawn.block.entity.NetherSpawnerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class RodspawnBuiltInLootTables {

    public static final ResourceKey<LootTable> SPAWNER_NETHER_FORTRESS_NETHER_DUST;
    public static final ResourceKey<LootTable> SPAWNER_CONSUMABLES;
    public static final ResourceKey<LootTable> NETHER_FORTRESS_REWARD;
    public static final ResourceKey<LootTable> NETHER_FORTRESS_REWARD_COMMON;
    public static final ResourceKey<LootTable> NETHER_FORTRESS_REWARD_RARE;
    public static final ResourceKey<LootTable> NETHER_FORTRESS_REWARD_UNIQUE;

    private static ResourceKey<LootTable> register(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Rodspawn.MOD_ID, name));
    }

    public static void init() {

    }

    static {
        SPAWNER_NETHER_FORTRESS_NETHER_DUST = register("spawners/nether_fortress/nether_dust");
        SPAWNER_CONSUMABLES = register("spawners/nether_fortress/consumables");
        NETHER_FORTRESS_REWARD = register("chests/nether_fortress/reward");
        NETHER_FORTRESS_REWARD_COMMON = register("chests/nether_fortress/reward_common");
        NETHER_FORTRESS_REWARD_RARE = register("chests/nether_fortress/reward_rare");
        NETHER_FORTRESS_REWARD_UNIQUE = register("chests/nether_fortress/reward_unique");
    }

}
