package com.codenamed.rodspawn.block;

import com.codenamed.rodspawn.block.entity.BrazierBlockEntity;
import com.codenamed.rodspawn.block.entity.brazier.BrazierState;
import com.codenamed.rodspawn.registry.RodspawnBlockEntities;
import com.codenamed.rodspawn.registry.RodspawnBlockstateProperties;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BrazierBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE1 = Block.box(0, 0, 0, 16,8, 16);
    private static final VoxelShape SHAPE2 = Block.box(4, 9, 4, 12,16, 12);

    public static final MapCodec<BrazierBlock> CODEC = simpleCodec(BrazierBlock::new);
    public static final Property<BrazierState> STATE;
    public static final DirectionProperty FACING;
    public static final BooleanProperty OMINOUS;

    public MapCodec<BrazierBlock> codec() {
        return CODEC;
    }

    public BrazierBlock(BlockBehaviour.Properties p_324605_) {
        super(p_324605_);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(STATE, BrazierState.INACTIVE)).setValue(OMINOUS, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.join(Block.box(0, 0, 0, 16, 8, 16), Block.box(3, 8, 3, 13, 16, 13), BooleanOp.OR);
    }

    public ItemInteractionResult useItemOn(ItemStack p_324161_, BlockState p_323816_, Level p_324403_, BlockPos p_324623_, Player p_324219_, InteractionHand p_324416_, BlockHitResult p_324261_) {
        if (!p_324161_.isEmpty() && p_323816_.getValue(STATE) == BrazierState.ACTIVE) {
            if (p_324403_ instanceof ServerLevel) {
                ServerLevel serverlevel = (ServerLevel)p_324403_;
                BlockEntity var10 = serverlevel.getBlockEntity(p_324623_);
                if (var10 instanceof BrazierBlockEntity) {
                    BrazierBlockEntity brazierBlockEntity = (BrazierBlockEntity)var10;
                    BrazierBlockEntity.Server.tryInsertKey(serverlevel, p_324623_, p_323816_, brazierBlockEntity.getConfig(), brazierBlockEntity.getServerData(), brazierBlockEntity.getSharedData(), p_324219_, p_324161_);
                    return ItemInteractionResult.SUCCESS;
                } else {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
            } else {
                return ItemInteractionResult.CONSUME;
            }
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos p_324543_, BlockState p_323652_) {
        return new BrazierBlockEntity(p_324543_, p_323652_);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_323673_) {
        p_323673_.add(new Property[]{FACING, STATE, OMINOUS});
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_323525_, BlockState p_324070_, BlockEntityType<T> p_323541_) {
        BlockEntityTicker var10000;
        if (p_323525_ instanceof ServerLevel serverlevel) {
            var10000 = createTickerHelper(p_323541_, RodspawnBlockEntities.BRAZIER.get(), (p_323957_, p_324322_, p_323828_, p_323769_) -> {
                BrazierBlockEntity.Server.tick(serverlevel, p_324322_, p_323828_, p_323769_.getConfig(), p_323769_.getServerData(), p_323769_.getSharedData());
            });
        } else {
            var10000 = createTickerHelper(p_323541_, RodspawnBlockEntities.BRAZIER.get(), (p_324290_, p_323926_, p_323941_, p_323489_) -> {
                BrazierBlockEntity.Client.tick(p_324290_, p_323926_, p_323941_, p_323489_.getClientData(), p_323489_.getSharedData());
            });
        }

        return var10000;
    }

    public BlockState getStateForPlacement(BlockPlaceContext p_324576_) {
        return (BlockState)this.defaultBlockState().setValue(FACING, p_324576_.getHorizontalDirection().getOpposite());
    }

    public BlockState rotate(BlockState p_324232_, Rotation p_324443_) {
        return (BlockState)p_324232_.setValue(FACING, p_324443_.rotate((Direction)p_324232_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_323894_, Mirror p_324242_) {
        return p_323894_.rotate(p_324242_.getRotation((Direction)p_323894_.getValue(FACING)));
    }

    public RenderShape getRenderShape(BlockState p_324584_) {
        return RenderShape.MODEL;
    }

    static {
        STATE = RodspawnBlockstateProperties.BRAZIER_STATE;
        FACING = HorizontalDirectionalBlock.FACING;
        OMINOUS = BlockStateProperties.OMINOUS;
    }
}