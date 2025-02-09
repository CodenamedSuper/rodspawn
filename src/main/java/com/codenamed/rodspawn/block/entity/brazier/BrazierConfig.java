package com.codenamed.rodspawn.block.entity.brazier;

import com.codenamed.rodspawn.registry.RodspawnBuiltInLootTables;
import com.codenamed.rodspawn.registry.RodspawnItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector.EntitySelector;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public record BrazierConfig(ResourceKey<LootTable> lootTable, double activationRange, double deactivationRange, ItemStack keyItem, Optional<ResourceKey<LootTable>> overrideLootTableToDisplay, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
    public static final String TAG_NAME = "config";
    public static BrazierConfig DEFAULT = new BrazierConfig();
    public static Codec<BrazierConfig> CODEC = RecordCodecBuilder.create((var) -> {
        return var.group(ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("loot_table", DEFAULT.lootTable()).forGetter(BrazierConfig::lootTable), Codec.DOUBLE.lenientOptionalFieldOf("activation_range", DEFAULT.activationRange()).forGetter(BrazierConfig::activationRange), Codec.DOUBLE.lenientOptionalFieldOf("deactivation_range", DEFAULT.deactivationRange()).forGetter(BrazierConfig::deactivationRange), ItemStack.lenientOptionalFieldOf("key_item").forGetter(BrazierConfig::keyItem), ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("override_loot_table_to_display").forGetter(BrazierConfig::overrideLootTableToDisplay)).apply(var, BrazierConfig::new);
    });

    private BrazierConfig() {
        this(RodspawnBuiltInLootTables.NETHER_FORTRESS_REWARD, 4.0, 4.5, new ItemStack(RodspawnItems.NETHER_DUST.get()), Optional.empty(), PlayerDetector.INCLUDING_CREATIVE_PLAYERS, EntitySelector.SELECT_FROM_LEVEL);
    }

    public BrazierConfig(ResourceKey<LootTable> p_335999_, double p_323704_, double p_323499_, ItemStack p_323661_, Optional<ResourceKey<LootTable>> p_323481_) {
        this(p_335999_, p_323704_, p_323499_, p_323661_, p_323481_, DEFAULT.playerDetector(), DEFAULT.entitySelector());
    }

    public BrazierConfig(ResourceKey<LootTable> lootTable, double activationRange, double deactivationRange, ItemStack keyItem, Optional<ResourceKey<LootTable>> overrideLootTableToDisplay, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this.lootTable = lootTable;
        this.activationRange = activationRange;
        this.deactivationRange = deactivationRange;
        this.keyItem = keyItem;
        this.overrideLootTableToDisplay = overrideLootTableToDisplay;
        this.playerDetector = playerDetector;
        this.entitySelector = entitySelector;
    }

    public DataResult<BrazierConfig> validate() {
        return this.activationRange > this.deactivationRange ? DataResult.error(() -> {
            return "Activation range must (" + this.activationRange + ") be less or equal to deactivation range (" + this.deactivationRange + ")";
        }) : DataResult.success(this);
    }

    public ResourceKey<LootTable> lootTable() {
        return this.lootTable;
    }

    public double activationRange() {
        return this.activationRange;
    }

    public double deactivationRange() {
        return this.deactivationRange;
    }

    public ItemStack keyItem() {
        return this.keyItem;
    }

    public Optional<ResourceKey<LootTable>> overrideLootTableToDisplay() {
        return this.overrideLootTableToDisplay;
    }

    public PlayerDetector playerDetector() {
        return this.playerDetector;
    }

    public PlayerDetector.EntitySelector entitySelector() {
        return this.entitySelector;
    }
}