package com.codenamed.rodspawn.block.entity;

import com.codenamed.rodspawn.block.BrazierBlock;
import com.codenamed.rodspawn.block.entity.brazier.*;
import com.codenamed.rodspawn.registry.RodspawnBlockEntityTypes;
import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class BrazierBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BrazierServerData serverData = new BrazierServerData();
    private final BrazierSharedData sharedData = new BrazierSharedData();
    private final BrazierClientData clientData = new BrazierClientData();
    private BrazierConfig config;

    public BrazierBlockEntity(BlockPos pos, BlockState state) {
        super(RodspawnBlockEntityTypes.BRAZIER.get(), pos, state);
        this.config = BrazierConfig.DEFAULT;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return (CompoundTag)Util.make(new CompoundTag(), (p_330145_) -> {
            p_330145_.put("shared_data", encode(BrazierSharedData.CODEC, this.sharedData, registries));
        });
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("config", encode(BrazierConfig.CODEC, this.config, registries));
        tag.put("shared_data", encode(BrazierSharedData.CODEC, this.sharedData, registries));
        tag.put("server_data", encode(BrazierServerData.CODEC, this.serverData, registries));
    }

    private static <T> Tag encode(Codec<T> codec, T value, HolderLookup.Provider levelRegistry) {
        return (Tag)codec.encodeStart(levelRegistry.createSerializationContext(NbtOps.INSTANCE), value).getOrThrow();
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        DynamicOps<Tag> dynamicops = registries.createSerializationContext(NbtOps.INSTANCE);
        DataResult<BrazierServerData> serverDataDataResult;
        DataResult<BrazierConfig> configDataResult;
        DataResult<BrazierSharedData> clientDataDataResult;

        Logger var10001;
        Optional<BrazierServerData> optionalBrazierServerData;
        Optional<BrazierSharedData> optionalBrazierSharedData;

        if (tag.contains("server_data")) {
            serverDataDataResult = BrazierServerData.CODEC.parse(dynamicops, tag.get("server_data"));
            var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            optionalBrazierServerData = serverDataDataResult.resultOrPartial(var10001::error);
            BrazierServerData var5 = this.serverData;
            Objects.requireNonNull(var5);
            optionalBrazierServerData.ifPresent(var5::set);
        }

        if (tag.contains("config")) {
            configDataResult = BrazierConfig.CODEC.parse(dynamicops, tag.get("config"));
            var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            configDataResult.resultOrPartial(var10001::error).ifPresent((p_324546_) -> {
                this.config = (BrazierConfig) p_324546_;
            });
        }

        if (tag.contains("shared_data")) {
            clientDataDataResult = BrazierSharedData.CODEC.parse(dynamicops, tag.get("shared_data"));
            var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            optionalBrazierSharedData = clientDataDataResult.resultOrPartial(var10001::error);
            BrazierSharedData var6 = this.sharedData;
            Objects.requireNonNull(var6);
            optionalBrazierSharedData.ifPresent(var6::set);
        }

    }

    @Nullable
    public BrazierServerData getServerData() {
        return this.level != null && !this.level.isClientSide ? this.serverData : null;
    }

    public BrazierSharedData getSharedData() {
        return this.sharedData;
    }

    public BrazierClientData getClientData() {
        return this.clientData;
    }

    public BrazierConfig getConfig() {
        return this.config;
    }

    @VisibleForTesting
    public void setConfig(BrazierConfig config) {
        this.config = config;
    }

    public static final class Server {
        private static final int UNLOCKING_DELAY_TICKS = 14;
        private static final int DISPLAY_CYCLE_TICK_RATE = 20;
        private static final int INSERT_FAIL_SOUND_BUFFER_TICKS = 15;

        public Server() {
        }

        public static void tick(ServerLevel level, BlockPos pos, BlockState state, BrazierConfig config, BrazierServerData serverData, BrazierSharedData sharedData) {
            BrazierState vaultstate = (BrazierState)state.getValue(BrazierBlock.STATE);
            if (shouldCycleDisplayItem(level.getGameTime(), vaultstate)) {
                cycleDisplayItemFromLootTable(level, vaultstate, config, sharedData, pos);
            }

            BlockState blockstate = state;
            if (level.getGameTime() >= serverData.stateUpdatingResumesAt()) {
                blockstate = (BlockState)state.setValue(BrazierBlock.STATE, vaultstate.tickAndGetNext(level, pos, config, serverData, sharedData));
                if (!state.equals(blockstate)) {
                    setBrazierState(level, pos, state, blockstate, config, sharedData);
                }
            }

            if (serverData.isDirty || sharedData.isDirty) {
                BrazierBlockEntity.setChanged(level, pos, state);
                if (sharedData.isDirty) {
                    level.sendBlockUpdated(pos, state, blockstate, 2);
                }

                serverData.isDirty = false;
                sharedData.isDirty = false;
            }

        }

        public static void tryInsertKey(ServerLevel level, BlockPos pos, BlockState state, BrazierConfig config, BrazierServerData serverData, BrazierSharedData sharedData, Player player, ItemStack stack) {
            BrazierState vaultstate = (BrazierState)state.getValue(BrazierBlock.STATE);
            if (canEjectReward(config, vaultstate)) {
                if (!isValidToInsert(config, stack)) {
                    playInsertFailSound(level, serverData, pos, SoundEvents.VAULT_INSERT_ITEM_FAIL);
                } else if (serverData.hasRewardedPlayer(player)) {
                    playInsertFailSound(level, serverData, pos, SoundEvents.VAULT_REJECT_REWARDED_PLAYER);
                } else {
                    List<ItemStack> list = resolveItemsToEject(level, config, pos, player);
                    if (!list.isEmpty()) {
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        stack.consume(config.keyItem().getCount(), player);
                        unlock(level, state, pos, config, serverData, sharedData, list);
                        serverData.addToRewardedPlayers(player);
                        sharedData.updateConnectedPlayersWithinRange(level, pos, serverData, config, config.deactivationRange());
                    }
                }
            }

        }

        static void setBrazierState(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState, BrazierConfig config, BrazierSharedData sharedData) {
            BrazierState vaultstate = (BrazierState)oldState.getValue(BrazierBlock.STATE);
            BrazierState vaultstate1 = (BrazierState)newState.getValue(BrazierBlock.STATE);
            level.setBlock(pos, newState, 3);
            vaultstate.onTransition(level, pos, vaultstate1, config, sharedData, (Boolean)newState.getValue(BrazierBlock.OMINOUS));
        }

        public static void cycleDisplayItemFromLootTable(ServerLevel level, BrazierState state, BrazierConfig config, BrazierSharedData sharedData, BlockPos pos) {
            if (!canEjectReward(config, state)) {
                sharedData.setDisplayItem(ItemStack.EMPTY);
            } else {
                ItemStack itemstack = getRandomDisplayItemFromLootTable(level, pos, (ResourceKey)config.overrideLootTableToDisplay().orElse(config.lootTable()));
                sharedData.setDisplayItem(itemstack);
            }

        }

        private static ItemStack getRandomDisplayItemFromLootTable(ServerLevel level, BlockPos pos, ResourceKey<LootTable> lootTable) {
            LootTable loottable = level.getServer().reloadableRegistries().getLootTable(lootTable);
            LootParams lootparams = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).create(LootContextParamSets.VAULT);
            List<ItemStack> list = loottable.getRandomItems(lootparams, level.getRandom());
            return list.isEmpty() ? ItemStack.EMPTY : (ItemStack)Util.getRandom(list, level.getRandom());
        }

        private static void unlock(ServerLevel level, BlockState state, BlockPos pos, BrazierConfig config, BrazierServerData serverData, BrazierSharedData sharedData, List<ItemStack> itemsToEject) {
            serverData.setItemsToEject(itemsToEject);
            sharedData.setDisplayItem(serverData.getNextItemToEject());
            serverData.pauseStateUpdatingUntil(level.getGameTime() + 14L);
            setBrazierState(level, pos, state, (BlockState)state.setValue(BrazierBlock.STATE, BrazierState.UNLOCKING), config, sharedData);
        }

        private static List<ItemStack> resolveItemsToEject(ServerLevel level, BrazierConfig config, BlockPos pos, Player player) {
            LootTable loottable = level.getServer().reloadableRegistries().getLootTable(config.lootTable());
            LootParams lootparams = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.VAULT);
            return loottable.getRandomItems(lootparams);
        }

        private static boolean canEjectReward(BrazierConfig config, BrazierState state) {
            return config.lootTable() != BuiltInLootTables.EMPTY && !config.keyItem().isEmpty() && state != BrazierState.INACTIVE;
        }

        private static boolean isValidToInsert(BrazierConfig config, ItemStack stack) {
            return ItemStack.isSameItemSameComponents(stack, config.keyItem()) && stack.getCount() >= config.keyItem().getCount();
        }

        private static boolean shouldCycleDisplayItem(long gameTime, BrazierState state) {
            return gameTime % 20L == 0L && state == BrazierState.ACTIVE;
        }

        private static void playInsertFailSound(ServerLevel level, BrazierServerData serverData, BlockPos pos, SoundEvent sound) {
            if (level.getGameTime() >= serverData.getLastInsertFailTimestamp() + 15L) {
                level.playSound((Player)null, pos, sound, SoundSource.BLOCKS);
                serverData.setLastInsertFailTimestamp(level.getGameTime());
            }

        }
    }

    public static final class Client {
        private static final int PARTICLE_TICK_RATE = 20;
        private static final float IDLE_PARTICLE_CHANCE = 0.5F;
        private static final float AMBIENT_SOUND_CHANCE = 0.02F;
        private static final int ACTIVATION_PARTICLE_COUNT = 20;
        private static final int DEACTIVATION_PARTICLE_COUNT = 20;

        public Client() {
        }

        public static void tick(Level level, BlockPos pos, BlockState state, BrazierClientData clientData, BrazierSharedData sharedData) {
            clientData.updateDisplayItemSpin();
            if (level.getGameTime() % 20L == 0L) {
                emitConnectionParticlesForNearbyPlayers(level, pos, state, sharedData);
            }

            emitIdleParticles(level, pos, sharedData, (Boolean)state.getValue(BrazierBlock.OMINOUS) ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME);
            playIdleSounds(level, pos, sharedData);
        }

        public static void emitActivationParticles(Level level, BlockPos pos, BlockState state, BrazierSharedData sharedData, ParticleOptions particle) {
            emitConnectionParticlesForNearbyPlayers(level, pos, state, sharedData);
            RandomSource randomsource = level.random;

            for(int i = 0; i < 20; ++i) {
                Vec3 vec3 = randomPosInsideCage(pos, randomsource);
                level.addParticle(ParticleTypes.SMOKE, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
                level.addParticle(particle, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
            }

        }

        public static void emitDeactivationParticles(Level level, BlockPos pos, ParticleOptions particle) {
            RandomSource randomsource = level.random;

            for(int i = 0; i < 20; ++i) {
                Vec3 vec3 = randomPosCenterOfCage(pos, randomsource);
                Vec3 vec31 = new Vec3(randomsource.nextGaussian() * 0.02, randomsource.nextGaussian() * 0.02, randomsource.nextGaussian() * 0.02);
                level.addParticle(particle, vec3.x(), vec3.y(), vec3.z(), vec31.x(), vec31.y(), vec31.z());
            }

        }

        private static void emitIdleParticles(Level level, BlockPos pos, BrazierSharedData sharedData, ParticleOptions particle) {
            RandomSource randomsource = level.getRandom();
            if (randomsource.nextFloat() <= 0.5F) {
                Vec3 vec3 = randomPosInsideCage(pos, randomsource);
                level.addParticle(ParticleTypes.SMOKE, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
                if (shouldDisplayActiveEffects(sharedData)) {
                    level.addParticle(particle, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
                }
            }

        }

        private static void emitConnectionParticlesForPlayer(Level level, Vec3 pos, Player player) {
            RandomSource randomsource = level.random;
            Vec3 vec3 = pos.vectorTo(player.position().add(0.0, (double)(player.getBbHeight() / 2.0F), 0.0));
            int i = Mth.nextInt(randomsource, 2, 5);

            for(int j = 0; j < i; ++j) {
                Vec3 vec31 = vec3.offsetRandom(randomsource, 1.0F);
                level.addParticle(ParticleTypes.VAULT_CONNECTION, pos.x(), pos.y(), pos.z(), vec31.x(), vec31.y(), vec31.z());
            }

        }

        private static void emitConnectionParticlesForNearbyPlayers(Level level, BlockPos pos, BlockState state, BrazierSharedData sharedData) {
            Set<UUID> set = sharedData.getConnectedPlayers();
            if (!set.isEmpty()) {
                Vec3 vec3 = keyholePos(pos, (Direction)state.getValue(BrazierBlock.FACING));
                Iterator var6 = set.iterator();

                while(var6.hasNext()) {
                    UUID uuid = (UUID)var6.next();
                    Player player = level.getPlayerByUUID(uuid);
                    if (player != null && isWithinConnectionRange(pos, sharedData, player)) {
                        emitConnectionParticlesForPlayer(level, vec3, player);
                    }
                }
            }

        }

        private static boolean isWithinConnectionRange(BlockPos pos, BrazierSharedData sharedData, Player player) {
            return player.blockPosition().distSqr(pos) <= Mth.square(sharedData.connectedParticlesRange());
        }

        private static void playIdleSounds(Level level, BlockPos pos, BrazierSharedData sharedData) {
            if (shouldDisplayActiveEffects(sharedData)) {
                RandomSource randomsource = level.getRandom();
                if (randomsource.nextFloat() <= 0.02F) {
                    level.playLocalSound(pos, SoundEvents.VAULT_AMBIENT, SoundSource.BLOCKS, randomsource.nextFloat() * 0.25F + 0.75F, randomsource.nextFloat() + 0.5F, false);
                }
            }

        }

        public static boolean shouldDisplayActiveEffects(BrazierSharedData sharedData) {
            return sharedData.hasDisplayItem();
        }

        private static Vec3 randomPosCenterOfCage(BlockPos pos, RandomSource random) {
            return Vec3.atLowerCornerOf(pos).add(Mth.nextDouble(random, 0.4, 0.6), Mth.nextDouble(random, 0.4, 0.6), Mth.nextDouble(random, 0.4, 0.6));
        }

        private static Vec3 randomPosInsideCage(BlockPos pos, RandomSource random) {
            return Vec3.atLowerCornerOf(pos).add(Mth.nextDouble(random, 0.1, 0.9), Mth.nextDouble(random, 0.25, 0.75), Mth.nextDouble(random, 0.1, 0.9));
        }

        private static Vec3 keyholePos(BlockPos pos, Direction facing) {
            return Vec3.atBottomCenterOf(pos).add((double)facing.getStepX() * 0.5, 1.75, (double)facing.getStepZ() * 0.5);
        }
    }
}