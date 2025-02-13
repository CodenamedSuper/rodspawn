package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.block.entity.brazier.BrazierState;
import com.codenamed.rodspawn.block.entity.nether_spawner.NetherSpawnerState;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class RodspawnBlockstateProperties {

    public static final EnumProperty<NetherSpawnerState> NETHER_SPAWNER_STATE;
    public static final EnumProperty<BrazierState> BRAZIER_STATE;
    public static final IntegerProperty NETHER_SPAWNER_HEALTH;
    public static final BooleanProperty NETHER_SPAWNER_BOSS;

    static {
        NETHER_SPAWNER_STATE = EnumProperty.create("nether_spawner_state", NetherSpawnerState.class);
        NETHER_SPAWNER_HEALTH = IntegerProperty.create("health", 0, 15);
        BRAZIER_STATE = EnumProperty.create("brazier_state", BrazierState.class);
        NETHER_SPAWNER_BOSS = BooleanProperty.create("nether_spawner_boss");
    }
}


