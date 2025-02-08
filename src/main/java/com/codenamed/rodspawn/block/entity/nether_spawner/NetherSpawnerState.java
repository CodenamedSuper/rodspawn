package com.codenamed.rodspawn.block.entity.nether_spawner;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.codenamed.rodspawn.block.NetherSpawnerBlock;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public enum NetherSpawnerState implements StringRepresentable {
    INACTIVE("inactive", 0, NetherSpawnerState.ParticleEmission.NONE, -1.0, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, NetherSpawnerState.ParticleEmission.SMALL_FLAMES, 200.0, true),
    ACTIVE("active", 8, NetherSpawnerState.ParticleEmission.FLAMES_AND_SMOKE, 1000.0, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, NetherSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0, false),
    EJECTING_REWARD("ejecting_reward", 8, NetherSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0, false),
    COOLDOWN("cooldown", 0, NetherSpawnerState.ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
    private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final NetherSpawnerState.ParticleEmission particleEmission;
    private final boolean isCapableOfSpawning;

    private NetherSpawnerState(String name, int lightLevel, NetherSpawnerState.ParticleEmission particleEmission, double spinningMobSpeed, boolean isCapableOfSpawning) {
        this.name = name;
        this.lightLevel = lightLevel;
        this.particleEmission = particleEmission;
        this.spinningMobSpeed = spinningMobSpeed;
        this.isCapableOfSpawning = isCapableOfSpawning;
    }

    NetherSpawnerState tickAndGetNext(BlockPos pos, NetherSpawner spawner, ServerLevel level) {
        int health = level.getBlockState(pos).getValue(NetherSpawnerBlock.HEALTH);
        NetherSpawnerData netherspawnerdata = spawner.getData();
        NetherSpawnerConfig netherspawnerconfig = spawner.getConfig();
        NetherSpawnerState state;
        switch (this.ordinal()) {
            case 0:
                state = netherspawnerdata.getOrCreateDisplayEntity(spawner, level, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
                break;
            case 1:
                if (!spawner.canSpawnInLevel(level)) {
                    netherspawnerdata.reset();
                    state = this;
                } else if (!netherspawnerdata.hasMobToSpawn(spawner, level.random)) {
                    state = INACTIVE;
                } else {
                    netherspawnerdata.tryDetectPlayers(level, pos, spawner);
                    state = netherspawnerdata.detectedPlayers.isEmpty() ? this : ACTIVE;
                }
                break;
            case 2:
                if (!spawner.canSpawnInLevel(level)) {
                    netherspawnerdata.reset();
                    state = WAITING_FOR_PLAYERS;
                } else if (!netherspawnerdata.hasMobToSpawn(spawner, level.random)) {
                    state = INACTIVE;
                } else {
                    int i = netherspawnerdata.countAdditionalPlayers(pos);
                    netherspawnerdata.tryDetectPlayers(level, pos, spawner);
                    if (spawner.isOminous()) {
                        this.spawnOminousOminousItemSpawner(level, pos, spawner);
                    }

                    if (health < 1) {
                        netherspawnerdata.cooldownEndsAt = level.getGameTime() + (long)spawner.getTargetCooldownLength();
                        netherspawnerdata.totalMobsSpawned = 0;
                        netherspawnerdata.nextMobSpawnsAt = 0L;
                        state = WAITING_FOR_REWARD_EJECTION;
                        break;

                    }
                    else if (netherspawnerdata.isReadyToSpawnNextMob(level, netherspawnerconfig, i)) {
                        spawner.spawnMob(level, pos).ifPresent((p_340800_) -> {
                            netherspawnerdata.currentMobs.add(p_340800_);
                            ++netherspawnerdata.totalMobsSpawned;
                            netherspawnerdata.nextMobSpawnsAt = level.getGameTime() + (long)netherspawnerconfig.ticksBetweenSpawn();
                            netherspawnerconfig.spawnPotentialsDefinition().getRandom(level.getRandom()).ifPresent((p_338048_) -> {
                                netherspawnerdata.nextSpawnData = Optional.of((SpawnData)p_338048_.data());
                                spawner.markUpdated();
                            });
                        });
                    }

                    state = this;
                }
                break;
            case 3:
                if (netherspawnerdata.isReadyToOpenShutter(level, 40.0F, spawner.getTargetCooldownLength())) {
                    level.playSound((Player)null, pos, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
                    state = EJECTING_REWARD;
                } else {
                    state = this;
                }
                break;
            case 4:
                if (!netherspawnerdata.isReadyToEjectItems(level, (float)TIME_BETWEEN_EACH_EJECTION, spawner.getTargetCooldownLength())) {
                    state = this;
                } else if (netherspawnerdata.detectedPlayers.isEmpty()) {
                    level.playSound((Player)null, pos, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
                    netherspawnerdata.ejectingLootTable = Optional.empty();
                    state = COOLDOWN;
                } else {
                    if (netherspawnerdata.ejectingLootTable.isEmpty()) {
                        netherspawnerdata.ejectingLootTable = netherspawnerconfig.lootTablesToEject().getRandomValue(level.getRandom());
                    }

                    netherspawnerdata.ejectingLootTable.ifPresent((p_335304_) -> {
                        spawner.ejectReward(level, pos, p_335304_);
                    });
                    netherspawnerdata.detectedPlayers.remove(netherspawnerdata.detectedPlayers.iterator().next());
                    state = this;
                }
                break;
            case 5:
                netherspawnerdata.tryDetectPlayers(level, pos, spawner);
                if (!netherspawnerdata.detectedPlayers.isEmpty()) {
                    netherspawnerdata.totalMobsSpawned = 0;
                    netherspawnerdata.nextMobSpawnsAt = 0L;
                    state = ACTIVE;
                } else if (netherspawnerdata.isCooldownFinished(level)) {
                    spawner.removeOminous(level, pos);
                    netherspawnerdata.reset();
                    level.setBlock(pos, level.getBlockState(pos).setValue(NetherSpawnerBlock.HEALTH, NetherSpawnerBlock.MAX_HEALTH), 2);
                    state = WAITING_FOR_PLAYERS;
                } else {
                    state = this;
                }
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

        return state;
    }

    private void spawnOminousOminousItemSpawner(ServerLevel level, BlockPos pos, NetherSpawner spawner) {
        NetherSpawnerData netherspawnerdata = spawner.getData();
        NetherSpawnerConfig netherspawnerconfig = spawner.getConfig();
        ItemStack itemstack = (ItemStack)netherspawnerdata.getDispensingItems(level, netherspawnerconfig, pos).getRandomValue(level.random).orElse(ItemStack.EMPTY);
        if (!itemstack.isEmpty() && this.timeToSpawnItemSpawner(level, netherspawnerdata)) {
            calculatePositionToSpawnSpawner(level, pos, spawner, netherspawnerdata).ifPresent((p_338064_) -> {
                OminousItemSpawner ominousitemspawner = OminousItemSpawner.create(level, itemstack);
                ominousitemspawner.moveTo(p_338064_);
                level.addFreshEntity(ominousitemspawner);
                float f = (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F;
                level.playSound((Player)null, BlockPos.containing(p_338064_), SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0F, f);
                netherspawnerdata.cooldownEndsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenItemSpawners();
            });
        }

    }

    private static Optional<Vec3> calculatePositionToSpawnSpawner(ServerLevel level, BlockPos pos, NetherSpawner spawner, NetherSpawnerData spawnerData) {
        Stream<UUID> var10000 = spawnerData.detectedPlayers.stream();
        Objects.requireNonNull(level);
        List<Player> list = var10000.map(level::getPlayerByUUID).filter(Objects::nonNull).filter((p_350236_) -> {

            return !p_350236_.isCreative() && !p_350236_.isSpectator() && p_350236_.isAlive() && p_350236_.distanceToSqr(pos.getCenter()) <= (double)Mth.square(spawner.getRequiredPlayerRange());
        }).toList();
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            Entity entity = selectEntityToSpawnItemAbove(list, spawnerData.currentMobs, spawner, pos, level);
            return entity == null ? Optional.empty() : calculatePositionAbove(entity, level);
        }
    }

    private static Optional<Vec3> calculatePositionAbove(Entity entity, ServerLevel level) {
        Vec3 vec3 = entity.position();
        Vec3 vec31 = vec3.relative(Direction.UP, (double)(entity.getBbHeight() + 2.0F + (float)level.random.nextInt(4)));
        BlockHitResult blockhitresult = level.clip(new ClipContext(vec3, vec31, Block.VISUAL, Fluid.NONE, CollisionContext.empty()));
        Vec3 vec32 = blockhitresult.getBlockPos().getCenter().relative(Direction.DOWN, 1.0);
        BlockPos blockpos = BlockPos.containing(vec32);
        return !level.getBlockState(blockpos).getCollisionShape(level, blockpos).isEmpty() ? Optional.empty() : Optional.of(vec32);
    }

    @Nullable
    private static Entity selectEntityToSpawnItemAbove(List<Player> player, Set<UUID> currentMobs, NetherSpawner spawner, BlockPos pos, ServerLevel level) {
        Stream<UUID> var10000 = currentMobs.stream();
        Objects.requireNonNull(level);
        Stream<Entity> stream = var10000.map(level::getEntity).filter(Objects::nonNull).filter((p_338051_) -> {
            return p_338051_.isAlive() && p_338051_.distanceToSqr(pos.getCenter()) <= (double)Mth.square(spawner.getRequiredPlayerRange());
        });
        List<? extends Entity> list = level.random.nextBoolean() ? stream.toList() : player;
        if (list.isEmpty()) {
            return null;
        } else {
            return list.size() == 1 ? (Entity)list.getFirst() : (Entity)Util.getRandom(list, level.random);
        }
    }

    private boolean timeToSpawnItemSpawner(ServerLevel level, NetherSpawnerData spawnerData) {
        return level.getGameTime() >= spawnerData.cooldownEndsAt;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= 0.0;
    }

    public boolean isCapableOfSpawning() {
        return this.isCapableOfSpawning;
    }

    public void emitParticles(Level level, BlockPos pos, boolean isOminous) {
        this.particleEmission.emit(level, level.getRandom(), pos, isOminous);
    }

    public String getSerializedName() {
        return this.name;
    }

    interface ParticleEmission {
        NetherSpawnerState.ParticleEmission NONE = (p_311998_, p_311983_, p_312351_, p_338371_) -> {
        };
        NetherSpawnerState.ParticleEmission SMALL_FLAMES = (p_338069_, p_338070_, p_338071_, p_338072_) -> {
            if (p_338070_.nextInt(2) == 0) {
                Vec3 vec3 = p_338071_.getCenter().offsetRandom(p_338070_, 0.9F);
                addParticle(p_338072_ ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, vec3, p_338069_);
            }

        };
        NetherSpawnerState.ParticleEmission FLAMES_AND_SMOKE = (p_338065_, p_338066_, p_338067_, p_338068_) -> {
            Vec3 vec3 = p_338067_.getCenter().offsetRandom(p_338066_, 1.0F);
            addParticle(ParticleTypes.SMOKE, vec3, p_338065_);
            addParticle(p_338068_ ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, vec3, p_338065_);
        };
        NetherSpawnerState.ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (p_311899_, p_311762_, p_312096_, p_338301_) -> {
            Vec3 vec3 = p_312096_.getCenter().offsetRandom(p_311762_, 0.9F);
            if (p_311762_.nextInt(3) == 0) {
                addParticle(ParticleTypes.SMOKE, vec3, p_311899_);
            }

            if (p_311899_.getGameTime() % 20L == 0L) {
                Vec3 vec31 = p_312096_.getCenter().add(0.0, 0.5, 0.0);
                int i = p_311899_.getRandom().nextInt(4) + 20;

                for(int j = 0; j < i; ++j) {
                    addParticle(ParticleTypes.SMOKE, vec31, p_311899_);
                }
            }

        };

        private static void addParticle(SimpleParticleType particleType, Vec3 pos, Level level) {
            level.addParticle(particleType, pos.x(), pos.y(), pos.z(), 0.0, 0.0, 0.0);
        }

        void emit(Level var1, RandomSource var2, BlockPos var3, boolean var4);
    }

    static class SpinningMob {
        private static final double NONE = -1.0;
        private static final double SLOW = 200.0;
        private static final double FAST = 1000.0;

        private SpinningMob() {
        }
    }

    static class LightLevel {
        private static final int UNLIT = 0;
        private static final int HALF_LIT = 4;
        private static final int LIT = 8;

        private LightLevel() {
        }
    }
}