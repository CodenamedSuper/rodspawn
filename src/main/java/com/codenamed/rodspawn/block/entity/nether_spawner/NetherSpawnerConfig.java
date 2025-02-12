package com.codenamed.rodspawn.block.entity.nether_spawner;

import com.codenamed.rodspawn.registry.RodspawnBuiltInLootTables;
import com.codenamed.rodspawn.registry.RodspawnItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public record NetherSpawnerConfig(int spawnRange, float totalMobs, float simultaneousMobs, float totalMobsAddedPerPlayer, float simultaneousMobsAddedPerPlayer, int ticksBetweenSpawn, SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition, SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject, ResourceKey<LootTable> itemsToDropWhenOminous) {
    public static final NetherSpawnerConfig DEFAULT;
    public static final Codec<NetherSpawnerConfig> CODEC;

    public NetherSpawnerConfig(int spawnRange, float totalMobs, float simultaneousMobs, float totalMobsAddedPerPlayer, float simultaneousMobsAddedPerPlayer, int ticksBetweenSpawn, SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition, SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject, ResourceKey<LootTable> itemsToDropWhenOminous) {
        this.spawnRange = spawnRange;
        this.totalMobs = totalMobs;
        this.simultaneousMobs = simultaneousMobs;
        this.totalMobsAddedPerPlayer = totalMobsAddedPerPlayer;
        this.simultaneousMobsAddedPerPlayer = simultaneousMobsAddedPerPlayer;
        this.ticksBetweenSpawn = ticksBetweenSpawn;
        this.spawnPotentialsDefinition = spawnPotentialsDefinition;
        this.lootTablesToEject = lootTablesToEject;
        this.itemsToDropWhenOminous = itemsToDropWhenOminous;
    }

    public int calculateTargetTotalMobs(int players) {
        return (int)Math.floor((double)(this.totalMobs + this.totalMobsAddedPerPlayer * (float)players));
    }

    public int calculateTargetSimultaneousMobs(int players) {
        return (int)Math.floor((double)(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float)players));
    }

    public long ticksBetweenItemSpawners() {
        return 160L;
    }

    public int spawnRange() {
        return this.spawnRange;
    }

    public float totalMobs() {
        return this.totalMobs;
    }

    public float simultaneousMobs() {
        return this.simultaneousMobs;
    }

    public float totalMobsAddedPerPlayer() {
        return this.totalMobsAddedPerPlayer;
    }

    public float simultaneousMobsAddedPerPlayer() {
        return this.simultaneousMobsAddedPerPlayer;
    }

    public int ticksBetweenSpawn() {
        return this.ticksBetweenSpawn;
    }

    public SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition() {
        return this.spawnPotentialsDefinition;
    }

    public SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject() {
        return this.lootTablesToEject;
    }

    public ResourceKey<LootTable> itemsToDropWhenOminous() {
        return this.itemsToDropWhenOminous;
    }

    static {
        DEFAULT = new NetherSpawnerConfig(4, 6.0F, 3.0F, 3.0F, 1.0F, 20, SimpleWeightedRandomList.empty(), (SimpleWeightedRandomList)SimpleWeightedRandomList.builder().add(RodspawnBuiltInLootTables.SPAWNER_CONSUMABLES).add(RodspawnBuiltInLootTables.SPAWNER_NETHER_FORTRESS_NETHER_DUST).build(), RodspawnBuiltInLootTables.SPAWNER_NETHER_ITEMS_TO_DROP_WHEN_OMINOUS);
        CODEC = RecordCodecBuilder.create((p_338041_) -> {
            return p_338041_.group(Codec.intRange(1, 128).lenientOptionalFieldOf("spawn_range", DEFAULT.spawnRange).forGetter(NetherSpawnerConfig::spawnRange), Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("total_mobs", DEFAULT.totalMobs).forGetter(NetherSpawnerConfig::totalMobs), Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("simultaneous_mobs", DEFAULT.simultaneousMobs).forGetter(NetherSpawnerConfig::simultaneousMobs), Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer).forGetter(NetherSpawnerConfig::totalMobsAddedPerPlayer), Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer).forGetter(NetherSpawnerConfig::simultaneousMobsAddedPerPlayer), Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("ticks_between_spawn", DEFAULT.ticksBetweenSpawn).forGetter(NetherSpawnerConfig::ticksBetweenSpawn), SpawnData.LIST_CODEC.lenientOptionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty()).forGetter(NetherSpawnerConfig::spawnPotentialsDefinition), SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceKey.codec(Registries.LOOT_TABLE)).lenientOptionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject).forGetter(NetherSpawnerConfig::lootTablesToEject), ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("items_to_drop_when_ominous", DEFAULT.itemsToDropWhenOminous).forGetter(NetherSpawnerConfig::itemsToDropWhenOminous)).apply(p_338041_, NetherSpawnerConfig::new);
        });
    }
}