package com.codenamed.rodspawn.block.entity.brazier;

import com.codenamed.rodspawn.block.entity.BrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public enum BrazierState implements StringRepresentable {
    INACTIVE("inactive", BrazierState.LightLevel.UNLIT) {
        protected void onEnter(ServerLevel p_324512_, BlockPos p_324300_, BrazierConfig p_323552_, BrazierSharedData p_324096_, boolean p_338586_) {
            p_324096_.setDisplayItem(ItemStack.EMPTY);
            p_324512_.levelEvent(3016, p_324300_, p_338586_ ? 1 : 0);
        }
    },
    ACTIVE("active", BrazierState.LightLevel.UNLIT) {
        protected void onEnter(ServerLevel p_324513_, BlockPos p_324445_, BrazierConfig p_323855_, BrazierSharedData p_323750_, boolean p_338489_) {
            if (!p_323750_.hasDisplayItem()) {
                BrazierBlockEntity.Server.cycleDisplayItemFromLootTable(p_324513_, this, p_323855_, p_323750_, p_324445_);
            }

            p_324513_.levelEvent(3015, p_324445_, p_338489_ ? 1 : 0);
        }
    },
    UNLOCKING("unlocking", BrazierState.LightLevel.LIT) {
        protected void onEnter(ServerLevel p_324077_, BlockPos p_323729_, BrazierConfig p_323520_, BrazierSharedData p_323550_, boolean p_338182_) {
            p_324077_.playSound((Player)null, p_323729_, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS);
        }
    },
    EJECTING("ejecting", BrazierState.LightLevel.LIT) {
        protected void onEnter(ServerLevel p_324167_, BlockPos p_324285_, BrazierConfig p_324106_, BrazierSharedData p_324596_, boolean p_338590_) {
            p_324167_.playSound((Player)null, p_324285_, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS);
        }

        protected void onExit(ServerLevel p_323987_, BlockPos p_324064_, BrazierConfig p_323588_, BrazierSharedData p_324224_) {
            p_323987_.playSound((Player)null, p_324064_, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS);
        }
    };

    private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;
    private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
    private static final int DELAY_AFTER_LAST_EJECTION_TICKS = 20;
    private static final int DELAY_BEFORE_FIRST_EJECTION_TICKS = 20;
    private final String stateName;
    private final BrazierState.LightLevel lightLevel;

    private BrazierState(String stateName, BrazierState.LightLevel lightLevel) {
        this.stateName = stateName;
        this.lightLevel = lightLevel;
    }

    public String getSerializedName() {
        return this.stateName;
    }

    public int lightLevel() {
        return this.lightLevel.value;
    }

    public BrazierState tickAndGetNext(ServerLevel level, BlockPos pos, BrazierConfig config, BrazierServerData serverData, BrazierSharedData sharedData) {
        BrazierState var10000;
        switch (this.ordinal()) {
            case 0:
                var10000 = updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.activationRange());
                break;
            case 1:
                var10000 = updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.deactivationRange());
                break;
            case 2:
                serverData.pauseStateUpdatingUntil(level.getGameTime() + 20L);
                var10000 = EJECTING;
                break;
            case 3:
                if (serverData.getItemsToEject().isEmpty()) {
                    serverData.markEjectionFinished();
                    var10000 = updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.deactivationRange());
                } else {
                    float f = serverData.ejectionProgress();
                    this.ejectResultItem(level, pos, serverData.popNextItemToEject(), f);
                    sharedData.setDisplayItem(serverData.getNextItemToEject());
                    boolean flag = serverData.getItemsToEject().isEmpty();
                    int i = flag ? 20 : 20;
                    serverData.pauseStateUpdatingUntil(level.getGameTime() + (long)i);
                    var10000 = EJECTING;
                }
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

        return var10000;
    }

    private static BrazierState updateStateForConnectedPlayers(ServerLevel level, BlockPos pos, BrazierConfig config, BrazierServerData severData, BrazierSharedData sharedData, double deactivationRange) {
        sharedData.updateConnectedPlayersWithinRange(level, pos, severData, config, deactivationRange);
        severData.pauseStateUpdatingUntil(level.getGameTime() + 20L);
        return sharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
    }

    public void onTransition(ServerLevel level, BlockPos pos, BrazierState state, BrazierConfig config, BrazierSharedData sharedData, boolean isOminous) {
        this.onExit(level, pos, config, sharedData);
        state.onEnter(level, pos, config, sharedData, isOminous);
    }

    protected void onEnter(ServerLevel level, BlockPos pos, BrazierConfig config, BrazierSharedData sharedData, boolean isOminous) {
    }

    protected void onExit(ServerLevel level, BlockPos pos, BrazierConfig config, BrazierSharedData sharedData) {
    }

    private void ejectResultItem(ServerLevel level, BlockPos pos, ItemStack stack, float ejectionProgress) {
        DefaultDispenseItemBehavior.spawnItem(level, stack, 2, Direction.UP, Vec3.atBottomCenterOf(pos).relative(Direction.UP, 1.2));
        level.levelEvent(3017, pos, 0);
        level.playSound((Player)null, pos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F * ejectionProgress);
    }

    static enum LightLevel {
        UNLIT(0),
        LIT(12);

        final int value;

        private LightLevel(int value) {
            this.value = value;
        }
    }
}