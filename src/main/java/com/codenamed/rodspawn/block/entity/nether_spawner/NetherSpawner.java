package com.codenamed.rodspawn.block.entity.nether_spawner;

import com.codenamed.rodspawn.block.NetherSpawnerBlock;
import com.codenamed.rodspawn.block.entity.NetherSpawnerBlockEntity;
import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.extensions.IOwnedSpawner;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

public final class NetherSpawner implements IOwnedSpawner {

    public static final String NORMAL_CONFIG_TAG_NAME = "normal_config";
    public static final String OMINOUS_CONFIG_TAG_NAME = "ominous_config";
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
    private final NetherSpawnerConfig normalConfig;
    private final NetherSpawnerConfig ominousConfig;
    private final NetherSpawnerData data;
    private final int requiredPlayerRange;
    private final int targetCooldownLength;
    private final NetherSpawner.StateAccessor stateAccessor;
    private PlayerDetector playerDetector;
    private final PlayerDetector.EntitySelector entitySelector;
    private boolean overridePeacefulAndMobSpawnRule;
    private boolean isOminous;
    private boolean isBoss;

    public Codec<NetherSpawner> codec() {
        return RecordCodecBuilder.create((p_338040_) -> {
            return p_338040_.group(NetherSpawnerConfig.CODEC.optionalFieldOf("normal_config", NetherSpawnerConfig.DEFAULT).forGetter(NetherSpawner::getNormalConfig), NetherSpawnerConfig.CODEC.optionalFieldOf("ominous_config", NetherSpawnerConfig.DEFAULT).forGetter(NetherSpawner::getOminousConfigForSerialization), NetherSpawnerData.MAP_CODEC.forGetter(NetherSpawner::getData), Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length", 36000).forGetter(NetherSpawner::getTargetCooldownLength), Codec.intRange(1, 128).optionalFieldOf("required_player_range", 14).forGetter(NetherSpawner::getRequiredPlayerRange)).apply(p_338040_, (p_338035_, p_338036_, p_338037_, p_338038_, p_338039_) -> {
                return new NetherSpawner(p_338035_, p_338036_, p_338037_, p_338038_, p_338039_, this.stateAccessor, this.playerDetector, this.entitySelector);
            });
        });
    }

    public NetherSpawner(Boolean boss,NetherSpawner.StateAccessor stateAccessor, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this((boss ? NetherSpawnerConfig.BOSS : NetherSpawnerConfig.DEFAULT), (boss ? NetherSpawnerConfig.BOSS : NetherSpawnerConfig.DEFAULT), new NetherSpawnerData(), 36000, 14, stateAccessor, playerDetector, entitySelector);
        isBoss = boss;
    }

    public NetherSpawner(NetherSpawnerConfig normalConfig, NetherSpawnerConfig ominousConfig, NetherSpawnerData data, int targetCooldownLength, int requiredPlayerRange, NetherSpawner.StateAccessor stateAccessor, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this.normalConfig = normalConfig;
        this.ominousConfig = ominousConfig;
        this.data = data;
        this.targetCooldownLength = targetCooldownLength;
        this.requiredPlayerRange = requiredPlayerRange;
        this.stateAccessor = stateAccessor;
        this.playerDetector = playerDetector;
        this.entitySelector = entitySelector;
    }

    public NetherSpawnerConfig getConfig() {
        return this.isOminous ? this.ominousConfig : this.normalConfig;
    }

    @VisibleForTesting
    public NetherSpawnerConfig getNormalConfig() {
        return this.normalConfig;
    }

    @VisibleForTesting
    public NetherSpawnerConfig getOminousConfig() {
        return this.ominousConfig;
    }

    private NetherSpawnerConfig getOminousConfigForSerialization() {

        if (isBoss) {
            return !this.ominousConfig.equals(this.normalConfig) ? this.ominousConfig : NetherSpawnerConfig.BOSS;
        }
        else {
            return !this.ominousConfig.equals(this.normalConfig) ? this.ominousConfig : NetherSpawnerConfig.DEFAULT;
        }
    }

    public void applyOminous(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, (BlockState)level.getBlockState(pos).setValue(NetherSpawnerBlock.OMINOUS, true), 3);
        level.levelEvent(3020, pos, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, level);
    }

    public void removeOminous(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, (BlockState)level.getBlockState(pos).setValue(NetherSpawnerBlock.OMINOUS, false), 3);
        this.isOminous = false;
    }

    public boolean isOminous() {
        return this.isOminous;
    }

    public NetherSpawnerData getData() {
        return this.data;
    }

    public int getTargetCooldownLength() {
        return this.targetCooldownLength;
    }

    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }

    public NetherSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public void setState(Level level, NetherSpawnerState state) {
        this.stateAccessor.setState(level, state);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public PlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public PlayerDetector.EntitySelector getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canSpawnInLevel(Level level) {
        if (this.overridePeacefulAndMobSpawnRule) {
            return true;
        } else {
            return level.getDifficulty() == Difficulty.PEACEFUL ? false : level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        }
    }

    public Optional<UUID> spawnMob(ServerLevel level, BlockPos pos) {
        RandomSource randomsource = level.getRandom();
        SpawnData spawndata = this.data.getOrCreateNextSpawnData(this, level.getRandom());
        CompoundTag compoundtag = spawndata.entityToSpawn();
        ListTag listtag = compoundtag.getList("Pos", 6);
        Optional<EntityType<?>> optional = EntityType.by(compoundtag);
        if (optional.isEmpty()) {
            return Optional.empty();
        } else {
            int i = listtag.size();
            double d0 = i >= 1 ? listtag.getDouble(0) : (double)pos.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.getConfig().spawnRange() + 0.5;
            double d1 = i >= 2 ? listtag.getDouble(1) : (double)(pos.getY() + randomsource.nextInt(3) - 1);
            double d2 = i >= 3 ? listtag.getDouble(2) : (double)pos.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.getConfig().spawnRange() + 0.5;
            if (!level.noCollision(((EntityType)optional.get()).getSpawnAABB(d0, d1, d2))) {
                return Optional.empty();
            } else {
                Vec3 vec3 = new Vec3(d0, d1, d2);
                if (!inLineOfSight(level, pos.getCenter(), vec3)) {
                    return Optional.empty();
                } else {
                    BlockPos blockpos = BlockPos.containing(vec3);
                    if (!SpawnPlacements.checkSpawnRules((EntityType)optional.get(), level, MobSpawnType.TRIAL_SPAWNER, blockpos, level.getRandom())) {
                        return Optional.empty();
                    } else {
                        if (spawndata.getCustomSpawnRules().isPresent()) {
                            SpawnData.CustomSpawnRules spawndata$customspawnrules = (SpawnData.CustomSpawnRules)spawndata.getCustomSpawnRules().get();
                            if (!spawndata$customspawnrules.isValidPosition(blockpos, level)) {
                                return Optional.empty();
                            }
                        }

                        Entity entity = EntityType.loadEntityRecursive(compoundtag, level, (p_312375_) -> {
                            p_312375_.moveTo(d0, d1, d2, randomsource.nextFloat() * 360.0F, 0.0F);
                            return p_312375_;
                        });
                        if (entity == null) {
                            return Optional.empty();
                        } else {
                            if (entity instanceof Mob) {
                                Mob mob = (Mob)entity;
                                if (!mob.checkSpawnObstruction(level)) {
                                    return Optional.empty();
                                }

                                boolean flag = spawndata.getEntityToSpawn().size() == 1 && spawndata.getEntityToSpawn().contains("id", 8);
                                EventHooks.finalizeMobSpawnSpawner(mob, level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.TRIAL_SPAWNER, (SpawnGroupData)null, this, flag);
                                mob.setPersistenceRequired();
                                Optional var10000 = spawndata.getEquipment();
                                Objects.requireNonNull(mob);
                                // var10000.ifPresent(mob::equip);
                            }

                            if (!level.tryAddFreshEntityWithPassengers(entity)) {
                                return Optional.empty();
                            } else {
                                FlameParticle netherspawner$flameparticle = this.isOminous ? FlameParticle.OMINOUS : NetherSpawner.FlameParticle.NORMAL;
                                level.levelEvent(3011, pos, netherspawner$flameparticle.encode());
                                level.levelEvent(3012, blockpos, netherspawner$flameparticle.encode());
                                level.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
                                return Optional.of(entity.getUUID());
                            }
                        }
                    }
                }
            }
        }
    }

    public void ejectReward(ServerLevel level, BlockPos pos, ResourceKey<LootTable> lootTable) {
        LootTable loottable = level.getServer().reloadableRegistries().getLootTable(lootTable);
        LootParams lootparams = (new LootParams.Builder(level)).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams);
        if (!objectarraylist.isEmpty()) {
            ObjectListIterator var7 = objectarraylist.iterator();

            while(var7.hasNext()) {
                ItemStack itemstack = (ItemStack)var7.next();
                DefaultDispenseItemBehavior.spawnItem(level, itemstack, 2, Direction.UP, Vec3.atBottomCenterOf(pos).relative(Direction.UP, 1.2));
            }

            level.levelEvent(3014, pos, 0);
        }

    }

    public void tickClient(Level level, BlockPos pos, boolean isOminous) {
        NetherSpawnerState netherspawnerstate = this.getState();
        netherspawnerstate.emitParticles(level, pos, isOminous);
        if (netherspawnerstate.hasSpinningMob()) {
            double d0 = (double)Math.max(0L, this.data.nextMobSpawnsAt - level.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + netherspawnerstate.spinningMobSpeed() / (d0 + 200.0)) % 360.0;
        }

        if (netherspawnerstate.isCapableOfSpawning()) {
            RandomSource randomsource = level.getRandom();
            if (randomsource.nextFloat() <= 0.02F) {
                SoundEvent soundevent = isOminous ? SoundEvents.TRIAL_SPAWNER_AMBIENT_OMINOUS : SoundEvents.TRIAL_SPAWNER_AMBIENT;
                level.playLocalSound(pos, soundevent, SoundSource.BLOCKS, randomsource.nextFloat() * 0.25F + 0.75F, randomsource.nextFloat() + 0.5F, false);
            }
        }

    }

    public void tickServer(ServerLevel level, BlockPos pos, boolean isOminous) {
        this.isOminous = isOminous;
        NetherSpawnerState netherspawnerstate = this.getState();
        if (this.data.currentMobs.removeIf((p_312870_) -> {
            return shouldMobBeUntracked(level, pos, p_312870_);
        })) {
            this.data.nextMobSpawnsAt = level.getGameTime() + (long)this.getConfig().ticksBetweenSpawn();
        }

        NetherSpawnerState netherspawnerstate1 = netherspawnerstate.tickAndGetNext(pos, this, level);
        if (netherspawnerstate1 != netherspawnerstate) {
            this.setState(level, netherspawnerstate1);
        }

    }

    private static boolean shouldMobBeUntracked(ServerLevel level, BlockPos pos, UUID uuid) {
        Entity entity = level.getEntity(uuid);
        return entity == null || !entity.isAlive() || !entity.level().dimension().equals(level.dimension()) || entity.blockPosition().distSqr(pos) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level level, Vec3 spawnerPos, Vec3 mobPos) {
        BlockHitResult blockhitresult = level.clip(new ClipContext(mobPos, spawnerPos, Block.VISUAL, Fluid.NONE, CollisionContext.empty()));
        return blockhitresult.getBlockPos().equals(BlockPos.containing(spawnerPos)) || blockhitresult.getType() == Type.MISS;
    }

    public static void addSpawnParticles(Level level, BlockPos pos, RandomSource random, SimpleParticleType particleType) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d1 = (double)pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d2 = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
            level.addParticle(particleType, d0, d1, d2, 0.0, 0.0, 0.0);
        }

    }

    public static void addBecomeOminousParticles(Level level, BlockPos pos, RandomSource random) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d1 = (double)pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d2 = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d3 = random.nextGaussian() * 0.02;
            double d4 = random.nextGaussian() * 0.02;
            double d5 = random.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.TRIAL_OMEN, d0, d1, d2, d3, d4, d5);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, d3, d4, d5);
        }

    }

    public static void addDetectPlayerParticles(Level level, BlockPos pos, RandomSource random, int type, ParticleOptions particle) {
        for(int i = 0; i < 30 + Math.min(type, 10) * 5; ++i) {
            double d0 = (double)(2.0F * random.nextFloat() - 1.0F) * 0.65;
            double d1 = (double)(2.0F * random.nextFloat() - 1.0F) * 0.65;
            double d2 = (double)pos.getX() + 0.5 + d0;
            double d3 = (double)pos.getY() + 0.1 + (double)random.nextFloat() * 0.8;
            double d4 = (double)pos.getZ() + 0.5 + d1;
            level.addParticle(particle, d2, d3, d4, 0.0, 0.0, 0.0);
        }

    }

    public static void addEjectItemParticles(Level level, BlockPos pos, RandomSource random) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double)pos.getX() + 0.4 + random.nextDouble() * 0.2;
            double d1 = (double)pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double d2 = (double)pos.getZ() + 0.4 + random.nextDouble() * 0.2;
            double d3 = random.nextGaussian() * 0.02;
            double d4 = random.nextGaussian() * 0.02;
            double d5 = random.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.SMALL_FLAME, d0, d1, d2, d3, d4, d5 * 0.25);
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
        }

    }

    /** @deprecated */
    @Deprecated(
            forRemoval = true
    )
    @VisibleForTesting
    public void setPlayerDetector(PlayerDetector playerDetector) {
        this.playerDetector = playerDetector;
    }

    /** @deprecated */
    @Deprecated(
            forRemoval = true
    )
    @VisibleForTesting
    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public @Nullable Either<BlockEntity, Entity> getOwner() {
        StateAccessor var2 = this.stateAccessor;
        if (var2 instanceof NetherSpawnerBlockEntity be) {
            return Either.left(be);
        } else {
            return null;
        }
    }

    public interface StateAccessor {
        void setState(Level var1, NetherSpawnerState var2);

        NetherSpawnerState getState();

        void markUpdated();
    }

    public static enum FlameParticle {
        NORMAL(ParticleTypes.FLAME),
        OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

        public final SimpleParticleType particleType;

        private FlameParticle(SimpleParticleType particleType) {
            this.particleType = particleType;
        }

        public static FlameParticle decode(int id) {
            FlameParticle[] anetherspawner$flameparticle = values();
            return id <= anetherspawner$flameparticle.length && id >= 0 ? anetherspawner$flameparticle[id] : NORMAL;
        }

        public int encode() {
            return this.ordinal();
        }
    }
}