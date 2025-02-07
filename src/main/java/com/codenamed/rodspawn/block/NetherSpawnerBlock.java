package com.codenamed.rodspawn.block;

import com.codenamed.rodspawn.block.entity.NetherSpawnerBlockEntity;
import com.codenamed.rodspawn.block.entity.nether_spawner.NetherSpawnerState;
import com.codenamed.rodspawn.registry.RodspawnBlockEntityTypes;
import com.codenamed.rodspawn.registry.RodspawnBlockstateProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;
import java.util.List;

public class NetherSpawnerBlock extends BaseEntityBlock {
    public static final MapCodec<NetherSpawnerBlock> CODEC = simpleCodec(NetherSpawnerBlock::new);
    public static final EnumProperty<NetherSpawnerState> STATE;
    public static final BooleanProperty OMINOUS;

    public MapCodec<NetherSpawnerBlock> codec() {
        return CODEC;
    }

    public NetherSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(STATE, NetherSpawnerState.INACTIVE)).setValue(OMINOUS, false));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{STATE, OMINOUS});
    }

    protected RenderShape getRenderShape(BlockState p_312710_) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetherSpawnerBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        BlockEntityTicker var10000;
        if (level instanceof ServerLevel serverlevel) {
            var10000 = createTickerHelper(entityType, RodspawnBlockEntityTypes.NETHER_SPAWNER.get(), (p_337976_, p_337977_, p_337978_, p_337979_) -> {
                p_337979_.getNetherSpawner().tickServer(serverlevel, p_337977_, (Boolean)p_337978_.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false));
            });
        } else {
            var10000 = createTickerHelper(entityType, RodspawnBlockEntityTypes.NETHER_SPAWNER.get(), (p_337980_, p_337981_, p_337982_, p_337983_) -> {
                p_337983_.getNetherSpawner().tickClient(p_337980_, p_337981_, (Boolean)p_337982_.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false));
            });
        }

        return var10000;
    }

    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, components, tooltipFlag);
        Spawner.appendHoverText(itemStack, components, "spawn_data");
    }

    static {
        STATE = RodspawnBlockstateProperties.NETHER_SPAWNER_STATE;
        OMINOUS = BlockStateProperties.OMINOUS;
    }
}
