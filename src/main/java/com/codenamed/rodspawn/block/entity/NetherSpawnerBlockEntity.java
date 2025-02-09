package com.codenamed.rodspawn.block.entity;

import com.codenamed.rodspawn.block.NetherSpawnerBlock;
import com.codenamed.rodspawn.block.entity.nether_spawner.NetherSpawner;
import com.codenamed.rodspawn.block.entity.nether_spawner.NetherSpawnerState;
import com.codenamed.rodspawn.registry.RodspawnBlockEntities;
import com.codenamed.rodspawn.registry.RodspawnBlockstateProperties;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector.EntitySelector;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class NetherSpawnerBlockEntity extends BlockEntity implements Spawner, NetherSpawner.StateAccessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private NetherSpawner netherSpawner;

    public NetherSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(RodspawnBlockEntities.NETHER_SPAWNER.get(), pos, state);
        PlayerDetector playerdetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.EntitySelector playerdetector$entityselector = EntitySelector.SELECT_FROM_LEVEL;
        this.netherSpawner = new NetherSpawner(this, playerdetector, playerdetector$entityselector);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("normal_config")) {
            CompoundTag compoundtag = tag.getCompound("normal_config").copy();
            tag.put("ominous_config", compoundtag.merge(tag.getCompound("ominous_config")));
        }

        DataResult<NetherSpawner> var10000 = this.netherSpawner.codec().parse(NbtOps.INSTANCE, tag);
        Logger var10001 = LOGGER;
        Objects.requireNonNull(var10001);
        var10000.resultOrPartial(var10001::error).ifPresent((p_311911_) -> {
            this.netherSpawner = (NetherSpawner) p_311911_;
        });
        if (this.level != null) {
            this.markUpdated();
        }

    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.netherSpawner.codec().encodeStart(NbtOps.INSTANCE, this.netherSpawner).ifSuccess((p_312175_) -> {
            tag.merge((CompoundTag) p_312175_);
        }).ifError((p_338001_) -> {
            LOGGER.warn("Failed to encode NetherSpawner {}", p_338001_.message());
        });
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.netherSpawner.getData().getUpdateTag((NetherSpawnerState) this.getBlockState().getValue(NetherSpawnerBlock.STATE));
    }

    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public void setEntityId(EntityType<?> entityType, RandomSource random) {
        this.netherSpawner.getData().setEntityId(this.netherSpawner, random, entityType);
        this.setChanged();
    }

    public NetherSpawner getNetherSpawner() {
        return this.netherSpawner;
    }

    public NetherSpawnerState getState() {
        return !this.getBlockState().hasProperty(RodspawnBlockstateProperties.NETHER_SPAWNER_STATE) ? NetherSpawnerState.INACTIVE : (NetherSpawnerState) this.getBlockState().getValue(RodspawnBlockstateProperties.NETHER_SPAWNER_STATE);
    }

    public void setState(Level level, NetherSpawnerState state) {
        this.setChanged();
        level.setBlockAndUpdate(this.worldPosition, (BlockState) this.getBlockState().setValue(RodspawnBlockstateProperties.NETHER_SPAWNER_STATE, state));
    }

    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }

    }
}