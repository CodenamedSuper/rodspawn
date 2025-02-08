package com.codenamed.rodspawn.block;

import com.codenamed.rodspawn.block.entity.NetherSpawnerBlockEntity;
import com.codenamed.rodspawn.block.entity.nether_spawner.NetherSpawnerState;
import com.codenamed.rodspawn.registry.RodspawnBlockEntityTypes;
import com.codenamed.rodspawn.registry.RodspawnBlocks;
import com.codenamed.rodspawn.registry.RodspawnBlockstateProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

public class NetherSpawnerBlock extends BaseEntityBlock {
    public static final MapCodec<NetherSpawnerBlock> CODEC = simpleCodec(NetherSpawnerBlock::new);
    public static final EnumProperty<NetherSpawnerState> STATE;
    public static final BooleanProperty OMINOUS;
    public static final IntegerProperty HEALTH;

    public static final int MAX_HEALTH = 15;

    public MapCodec<NetherSpawnerBlock> codec() {
        return CODEC;
    }

    public NetherSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(HEALTH, MAX_HEALTH).setValue(STATE, NetherSpawnerState.INACTIVE)).setValue(OMINOUS, false));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{STATE, OMINOUS, HEALTH});
    }

    protected RenderShape getRenderShape(BlockState p_312710_) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetherSpawnerBlockEntity(pos, state);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {

        if (projectile instanceof Arrow) {
            damage(level, state, hit.getBlockPos(), 1);
        }
        else if ( projectile instanceof ThrownTrident) {
            damage(level, state, hit.getBlockPos(), 2);

        }


        super.onProjectileHit(level, state, hit, projectile);
    }

    public void damage(Level level, BlockState state, BlockPos pos ,int dmg) {

        if (state.getValue(HEALTH) < 1) return;

        level.setBlock(pos, state.setValue(HEALTH, state.getValue(HEALTH ) - dmg), 2);

        RandomSource randomsource = level.getRandom();
        level.playSound((Player)null, pos, SoundEvents.TRIAL_SPAWNER_FALL, SoundSource.BLOCKS, randomsource.nextFloat() + 0.75F, randomsource.nextFloat() + 0.5F);

    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        BlockEntityTicker ticker;
        if (level instanceof ServerLevel serverlevel) {
            ticker = createTickerHelper(entityType, RodspawnBlockEntityTypes.NETHER_SPAWNER.get(), (p_337976_, p_337977_, p_337978_, p_337979_) -> {
                p_337979_.getNetherSpawner().tickServer(serverlevel, p_337977_, (Boolean)p_337978_.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false));
            });
        } else {
            ticker = createTickerHelper(entityType, RodspawnBlockEntityTypes.NETHER_SPAWNER.get(), (p_337980_, p_337981_, p_337982_, p_337983_) -> {
                p_337983_.getNetherSpawner().tickClient(p_337980_, p_337981_, (Boolean)p_337982_.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false));
            });
        }

        return ticker;
    }

    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, components, tooltipFlag);
        Spawner.appendHoverText(itemStack, components, "spawn_data");
    }

    static {
        STATE = RodspawnBlockstateProperties.NETHER_SPAWNER_STATE;
        OMINOUS = BlockStateProperties.OMINOUS;
        HEALTH = RodspawnBlockstateProperties.NETHER_SPAWNER_HEALTH;
    }
}
