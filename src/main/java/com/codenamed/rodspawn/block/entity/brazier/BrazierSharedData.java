package com.codenamed.rodspawn.block.entity.brazier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public class BrazierSharedData {
    public static final String TAG_NAME = "shared_data";
    public static Codec<BrazierSharedData> CODEC = RecordCodecBuilder.create((p_338074_) -> {
        return p_338074_.group(ItemStack.lenientOptionalFieldOf("display_item").forGetter((p_324217_) -> {
            return p_324217_.displayItem;
        }), UUIDUtil.CODEC_LINKED_SET.lenientOptionalFieldOf("connected_players", Set.of()).forGetter((p_324110_) -> {
            return p_324110_.connectedPlayers;
        }), Codec.DOUBLE.lenientOptionalFieldOf("connected_particles_range", BrazierConfig.DEFAULT.deactivationRange()).forGetter((p_323486_) -> {
            return p_323486_.connectedParticlesRange;
        })).apply(p_338074_, BrazierSharedData::new);
    });
    private ItemStack displayItem;
    private Set<UUID> connectedPlayers;
    private double connectedParticlesRange;
    public boolean isDirty;

    BrazierSharedData(ItemStack displayItem, Set<UUID> connectedPlayers, double connectedParticlesRange) {
        this.displayItem = ItemStack.EMPTY;
        this.connectedPlayers = new ObjectLinkedOpenHashSet();
        this.connectedParticlesRange = BrazierConfig.DEFAULT.deactivationRange();
        this.displayItem = displayItem;
        this.connectedPlayers.addAll(connectedPlayers);
        this.connectedParticlesRange = connectedParticlesRange;
    }

    public BrazierSharedData() {
        this.displayItem = ItemStack.EMPTY;
        this.connectedPlayers = new ObjectLinkedOpenHashSet();
        this.connectedParticlesRange = BrazierConfig.DEFAULT.deactivationRange();
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public boolean hasDisplayItem() {
        return !this.displayItem.isEmpty();
    }

    public void setDisplayItem(ItemStack displayItem) {
        if (!ItemStack.matches(this.displayItem, displayItem)) {
            this.displayItem = displayItem.copy();
            this.markDirty();
        }

    }

    boolean hasConnectedPlayers() {
        return !this.connectedPlayers.isEmpty();
    }

    public Set<UUID> getConnectedPlayers() {
        return this.connectedPlayers;
    }

    public double connectedParticlesRange() {
        return this.connectedParticlesRange;
    }

    public void updateConnectedPlayersWithinRange(ServerLevel level, BlockPos pos, BrazierServerData serverData, BrazierConfig config, double deactivationRange) {
        Set<UUID> set = (Set)config.playerDetector().detect(level, config.entitySelector(), pos, deactivationRange, false).stream().filter((p_324308_) -> {
            return !serverData.getRewardedPlayers().contains(p_324308_);
        }).collect(Collectors.toSet());
        if (!this.connectedPlayers.equals(set)) {
            this.connectedPlayers = set;
            this.markDirty();
        }

    }

    private void markDirty() {
        this.isDirty = true;
    }

    public void set(BrazierSharedData other) {
        this.displayItem = other.displayItem;
        this.connectedPlayers = other.connectedPlayers;
        this.connectedParticlesRange = other.connectedParticlesRange;
    }
}

