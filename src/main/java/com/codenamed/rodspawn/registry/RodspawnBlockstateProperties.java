package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.block.entity.nether_spawner.NetherSpawnerState;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class RodspawnBlockstateProperties {

    public static final EnumProperty<NetherSpawnerState> NETHER_SPAWNER_STATE;


    static {
        NETHER_SPAWNER_STATE = EnumProperty.create("nether_spawner_state", NetherSpawnerState.class);

    }
}


