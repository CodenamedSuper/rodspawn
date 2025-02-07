package com.codenamed.rodspawn.block.entity.nether_spawner;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class NetherSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    public static MapCodec<NetherSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec((p_312830_) -> {
        return p_312830_.group(UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", Sets.newHashSet()).forGetter((p_312495_) -> {
            return p_312495_.detectedPlayers;
        }), UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", Sets.newHashSet()).forGetter((p_312798_) -> {
            return p_312798_.currentMobs;
        }), Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", 0L).forGetter((p_312792_) -> {
            return p_312792_.cooldownEndsAt;
        }), Codec.LONG.lenientOptionalFieldOf("next_mob_spawns_at", 0L).forGetter((p_311772_) -> {
            return p_311772_.nextMobSpawnsAt;
        }), Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter((p_312862_) -> {
            return p_312862_.totalMobsSpawned;
        }), SpawnData.CODEC.lenientOptionalFieldOf("spawn_data").forGetter((p_312634_) -> {
            return p_312634_.nextSpawnData;
        }), ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("ejecting_loot_table").forGetter((p_312388_) -> {
            return p_312388_.ejectingLootTable;
        })).apply(p_312830_, NetherSpawnerData::new);
    });
    protected final Set<UUID> detectedPlayers;
    protected final Set<UUID> currentMobs;
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData;
    protected Optional<ResourceKey<LootTable>> ejectingLootTable;
    @Nullable
    protected Entity displayEntity;
    @Nullable
    private SimpleWeightedRandomList<ItemStack> dispensing;
    protected double spin;
    protected double oSpin;

    public NetherSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public NetherSpawnerData(Set<UUID> detectedPlayers, Set<UUID> currentMobs, long cooldownEndsAt, long nextMobSpawnsAt, int totalMobsSpawned, Optional<SpawnData> nextSpawnData, Optional<ResourceKey<LootTable>> ejectingLootTable) {
        this.detectedPlayers = new HashSet();
        this.currentMobs = new HashSet();
        this.detectedPlayers.addAll(detectedPlayers);
        this.currentMobs.addAll(currentMobs);
        this.cooldownEndsAt = cooldownEndsAt;
        this.nextMobSpawnsAt = nextMobSpawnsAt;
        this.totalMobsSpawned = totalMobsSpawned;
        this.nextSpawnData = nextSpawnData;
        this.ejectingLootTable = ejectingLootTable;
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
        this.nextSpawnData = Optional.empty();
    }

    public boolean hasMobToSpawn(NetherSpawner netherSpawner, RandomSource random) {
        boolean flag = this.getOrCreateNextSpawnData(netherSpawner, random).getEntityToSpawn().contains("id", 8);
        return flag || !netherSpawner.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(NetherSpawnerConfig config, int players) {
        return this.totalMobsSpawned >= config.calculateTargetTotalMobs(players);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel level, NetherSpawnerConfig config, int players) {
        return level.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < config.calculateTargetSimultaneousMobs(players);
    }

    public int countAdditionalPlayers(BlockPos pos) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Nether Spawner at " + String.valueOf(pos) + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel level, BlockPos pos, NetherSpawner spawner) {
        boolean flag = (pos.asLong() + level.getGameTime()) % 20L != 0L;
        if (!flag && (!spawner.getState().equals(NetherSpawnerState.COOLDOWN) || !spawner.isOminous())) {
            List<UUID> list = spawner.getPlayerDetector().detect(level, spawner.getEntitySelector(), pos, (double)spawner.getRequiredPlayerRange(), true);
            boolean flag1;
            if (!spawner.isOminous() && !list.isEmpty()) {
                Optional<Pair<Player, Holder<MobEffect>>> optional = findPlayerWithOminousEffect(level, list);
                optional.ifPresent((p_350233_) -> {
                    Player player = (Player)p_350233_.getFirst();
                    if (p_350233_.getSecond() == MobEffects.BAD_OMEN) {
                        transformBadOmenIntoNetherOmen(player);
                    }

                    level.levelEvent(3020, BlockPos.containing(player.getEyePosition()), 0);
                    spawner.applyOminous(level, pos);
                });
                flag1 = optional.isPresent();
            } else {
                flag1 = false;
            }

            if (!spawner.getState().equals(NetherSpawnerState.COOLDOWN) || flag1) {
                boolean flag2 = spawner.getData().detectedPlayers.isEmpty();
                List<UUID> list1 = flag2 ? list : spawner.getPlayerDetector().detect(level, spawner.getEntitySelector(), pos, (double)spawner.getRequiredPlayerRange(), false);
                if (this.detectedPlayers.addAll(list1)) {
                    this.nextMobSpawnsAt = Math.max(level.getGameTime() + 40L, this.nextMobSpawnsAt);
                    if (!flag1) {
                        int i = spawner.isOminous() ? 3019 : 3013;
                        level.levelEvent(i, pos, this.detectedPlayers.size());
                    }
                }
            }
        }

    }

    private static Optional<Pair<Player, Holder<MobEffect>>> findPlayerWithOminousEffect(ServerLevel level, List<UUID> players) {
        Player player = null;
        Iterator var3 = players.iterator();

        while(var3.hasNext()) {
            UUID uuid = (UUID)var3.next();
            Player player1 = level.getPlayerByUUID(uuid);
            if (player1 != null) {
                Holder<MobEffect> holder = MobEffects.TRIAL_OMEN;
                if (player1.hasEffect(holder)) {
                    return Optional.of(Pair.of(player1, holder));
                }

                if (player1.hasEffect(MobEffects.BAD_OMEN)) {
                    player = player1;
                }
            }
        }

        return Optional.ofNullable(player).map((p_350229_) -> {
            return Pair.of(p_350229_, MobEffects.BAD_OMEN);
        });
    }

    public void resetAfterBecomingOminous(NetherSpawner spawner, ServerLevel level) {
        Stream<UUID> var10000 = this.currentMobs.stream();
        Objects.requireNonNull(level);
        var10000.map(level::getEntity).forEach((p_351984_) -> {
            if (p_351984_ != null) {
                level.levelEvent(3012, p_351984_.blockPosition(), NetherSpawner.FlameParticle.NORMAL.encode());
                if (p_351984_ instanceof Mob) {
                    Mob mob = (Mob)p_351984_;
                    mob.dropPreservedEquipment();
                }

                p_351984_.remove(RemovalReason.DISCARDED);
            }

        });
        if (!spawner.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }

        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = level.getGameTime() + (long)spawner.getOminousConfig().ticksBetweenSpawn();
        spawner.markUpdated();
        this.cooldownEndsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenItemSpawners();
    }

    private static void transformBadOmenIntoNetherOmen(Player player) {
        MobEffectInstance mobeffectinstance = player.getEffect(MobEffects.BAD_OMEN);
        if (mobeffectinstance != null) {
            int i = mobeffectinstance.getAmplifier() + 1;
            int j = 18000 * i;
            player.removeEffect(MobEffects.BAD_OMEN);
            player.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, j, 0));
        }

    }

    public boolean isReadyToOpenShutter(ServerLevel level, float delay, int targetCooldownLength) {
        long i = this.cooldownEndsAt - (long)targetCooldownLength;
        return (float)level.getGameTime() >= (float)i + delay;
    }

    public boolean isReadyToEjectItems(ServerLevel level, float delay, int targetCooldownLength) {
        long i = this.cooldownEndsAt - (long)targetCooldownLength;
        return (float)(level.getGameTime() - i) % delay == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel level) {
        return level.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(NetherSpawner spawner, RandomSource random, EntityType<?> entityType) {
        this.getOrCreateNextSpawnData(spawner, random).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
    }

    protected SpawnData getOrCreateNextSpawnData(NetherSpawner spawner, RandomSource random) {
        if (this.nextSpawnData.isPresent()) {
            return (SpawnData)this.nextSpawnData.get();
        } else {
            SimpleWeightedRandomList<SpawnData> simpleweightedrandomlist = spawner.getConfig().spawnPotentialsDefinition();
            Optional<SpawnData> optional = simpleweightedrandomlist.isEmpty() ? this.nextSpawnData : simpleweightedrandomlist.getRandom(random).map(WeightedEntry.Wrapper::data);
            this.nextSpawnData = Optional.of((SpawnData)optional.orElseGet(SpawnData::new));
            spawner.markUpdated();
            return (SpawnData)this.nextSpawnData.get();
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(NetherSpawner spawner, Level level, NetherSpawnerState spawnerState) {
        if (!spawnerState.hasSpinningMob()) {
            return null;
        } else {
            if (this.displayEntity == null) {
                CompoundTag compoundtag = this.getOrCreateNextSpawnData(spawner, level.getRandom()).getEntityToSpawn();
                if (compoundtag.contains("id", 8)) {
                    this.displayEntity = EntityType.loadEntityRecursive(compoundtag, level, Function.identity());
                }
            }

            return this.displayEntity;
        }
    }

    public CompoundTag getUpdateTag(NetherSpawnerState spawnerState) {
        CompoundTag compoundtag = new CompoundTag();
        if (spawnerState == NetherSpawnerState.ACTIVE) {
            compoundtag.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
        }

        this.nextSpawnData.ifPresent((p_338045_) -> {
            compoundtag.put("spawn_data", (Tag)SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, p_338045_).result().orElseThrow(() -> {
                return new IllegalStateException("Invalid SpawnData");
            }));
        });
        return compoundtag;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }

    SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel level, NetherSpawnerConfig config, BlockPos pos) {
        if (this.dispensing != null) {
            return this.dispensing;
        } else {
            LootTable loottable = level.getServer().reloadableRegistries().getLootTable(config.itemsToDropWhenOminous());
            LootParams lootparams = (new LootParams.Builder(level)).create(LootContextParamSets.EMPTY);
            long i = lowResolutionPosition(level, pos);
            ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams, i);
            if (objectarraylist.isEmpty()) {
                return SimpleWeightedRandomList.empty();
            } else {
                SimpleWeightedRandomList.Builder<ItemStack> builder = new SimpleWeightedRandomList.Builder();
                ObjectListIterator var10 = objectarraylist.iterator();

                while(var10.hasNext()) {
                    ItemStack itemstack = (ItemStack)var10.next();
                    builder.add(itemstack.copyWithCount(1), itemstack.getCount());
                }

                this.dispensing = builder.build();
                return this.dispensing;
            }
        }
    }

    private static long lowResolutionPosition(ServerLevel level, BlockPos pos) {
        BlockPos blockpos = new BlockPos(Mth.floor((float)pos.getX() / 30.0F), Mth.floor((float)pos.getY() / 20.0F), Mth.floor((float)pos.getZ() / 30.0F));
        return level.getSeed() + blockpos.asLong();
    }
}
