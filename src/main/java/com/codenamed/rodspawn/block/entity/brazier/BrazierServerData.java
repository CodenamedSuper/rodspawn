package com.codenamed.rodspawn.block.entity.brazier;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BrazierServerData {
    public static final String TAG_NAME = "server_data";
    public static Codec<BrazierServerData> CODEC = RecordCodecBuilder.create((p_338073_) -> {
        return p_338073_.group(UUIDUtil.CODEC_LINKED_SET.lenientOptionalFieldOf("rewarded_players", Set.of()).forGetter((p_323523_) -> {
            return p_323523_.rewardedPlayers;
        }), Codec.LONG.lenientOptionalFieldOf("state_updating_resumes_at", 0L).forGetter((p_323634_) -> {
            return p_323634_.stateUpdatingResumesAt;
        }), ItemStack.CODEC.listOf().lenientOptionalFieldOf("items_to_eject", List.of()).forGetter((p_323976_) -> {
            return p_323976_.itemsToEject;
        }), Codec.INT.lenientOptionalFieldOf("total_ejections_needed", 0).forGetter((p_323753_) -> {
            return p_323753_.totalEjectionsNeeded;
        })).apply(p_338073_, BrazierServerData::new);
    });
    private static final int MAX_REWARD_PLAYERS = 128;
    private final Set<UUID> rewardedPlayers = new ObjectLinkedOpenHashSet();
    private long stateUpdatingResumesAt;
    private final List<ItemStack> itemsToEject = new ObjectArrayList();
    private long lastInsertFailTimestamp;
    private int totalEjectionsNeeded;
    public boolean isDirty;

    BrazierServerData(Set<UUID> rewardedPlayers, long stateUpdatingResumesAt, List<ItemStack> itemsToEject, int totalEjectionsNeeded) {
        this.rewardedPlayers.addAll(rewardedPlayers);
        this.stateUpdatingResumesAt = stateUpdatingResumesAt;
        this.itemsToEject.addAll(itemsToEject);
        this.totalEjectionsNeeded = totalEjectionsNeeded;
    }

    public BrazierServerData() {
    }

    public void setLastInsertFailTimestamp(long lastInsertFailTimestamp) {
        this.lastInsertFailTimestamp = lastInsertFailTimestamp;
    }

    public long getLastInsertFailTimestamp() {
        return this.lastInsertFailTimestamp;
    }

    Set<UUID> getRewardedPlayers() {
        return this.rewardedPlayers;
    }

    public boolean hasRewardedPlayer(Player player) {
        return this.rewardedPlayers.contains(player.getUUID());
    }

    @VisibleForTesting
    public void addToRewardedPlayers(Player player) {
        this.rewardedPlayers.add(player.getUUID());
        if (this.rewardedPlayers.size() > 128) {
            Iterator<UUID> iterator = this.rewardedPlayers.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }

        this.markChanged();
    }

    public long stateUpdatingResumesAt() {
        return this.stateUpdatingResumesAt;
    }

    public void pauseStateUpdatingUntil(long time) {
        this.stateUpdatingResumesAt = time;
        this.markChanged();
    }

    List<ItemStack> getItemsToEject() {
        return this.itemsToEject;
    }

    void markEjectionFinished() {
        this.totalEjectionsNeeded = 0;
        this.markChanged();
    }

    public void setItemsToEject(List<ItemStack> itemsToEject) {
        this.itemsToEject.clear();
        this.itemsToEject.addAll(itemsToEject);
        this.totalEjectionsNeeded = this.itemsToEject.size();
        this.markChanged();
    }

    public ItemStack getNextItemToEject() {
        return this.itemsToEject.isEmpty() ? ItemStack.EMPTY : (ItemStack)Objects.requireNonNullElse((ItemStack)this.itemsToEject.get(this.itemsToEject.size() - 1), ItemStack.EMPTY);
    }

    ItemStack popNextItemToEject() {
        if (this.itemsToEject.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.markChanged();
            return (ItemStack)Objects.requireNonNullElse((ItemStack)this.itemsToEject.remove(this.itemsToEject.size() - 1), ItemStack.EMPTY);
        }
    }

    public void set(BrazierServerData other) {
        this.stateUpdatingResumesAt = other.stateUpdatingResumesAt();
        this.itemsToEject.clear();
        this.itemsToEject.addAll(other.itemsToEject);
        this.rewardedPlayers.clear();
        this.rewardedPlayers.addAll(other.rewardedPlayers);
    }

    private void markChanged() {
        this.isDirty = true;
    }

    public float ejectionProgress() {
        return this.totalEjectionsNeeded == 1 ? 1.0F : 1.0F - Mth.inverseLerp((float)this.getItemsToEject().size(), 1.0F, (float)this.totalEjectionsNeeded);
    }
}