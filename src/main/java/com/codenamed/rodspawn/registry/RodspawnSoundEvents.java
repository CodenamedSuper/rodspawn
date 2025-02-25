package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RodspawnSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Rodspawn.MOD_ID);

    /*

    public static final Supplier<SoundEvent> NETHER_SPAWNER_BREAK = register("block.nether_spawner.break");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_STEP = register("block.nether_spawner.step");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_PLACE = register("block.nether_spawner.place");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_HIT = register("block.nether_spawner.hit");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_FALL = register("block.nether_spawner.fall");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_SPAWN_MOB = register("block.nether_spawner.spawn_mob");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_ABOUT_TO_SPAWN_ITEM = register("block.nether_spawner.about_to_spawn_item");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_SPAWN_ITEM = register("block.nether_spawner.spawn_item");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_SPAWN_ITEM_BEGIN = register("block.nether_spawner.spawn_item_begin");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_DETECT_PLAYER = register("block.nether_spawner.detect_player");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_OMINOUS_ACTIVATE = register("block.nether_spawner.ominous_activate");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_AMBIENT = register("block.nether_spawner.ambient");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_AMBIENT_OMINOUS = register("block.nether_spawner.ambient_ominous");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_OPEN_SHUTTER = register("block.nether_spawner.open_shutter");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_CLOSE_SHUTTER = register("block.nether_spawner.close_shutter");
    public static final Supplier<SoundEvent> NETHER_SPAWNER_EJECT_ITEM = register("block.nether_spawner.eject_item");

    public static final Supplier<SoundEvent> BRAZIER_ACTIVATE = register("block.brazier.activate");
    public static final Supplier<SoundEvent> BRAZIER_AMBIENT = register("block.brazier.ambient");
    public static final Supplier<SoundEvent> BRAZIER_BREAK = register("block.brazier.break");
    public static final Supplier<SoundEvent> BRAZIER_CLOSE_SHUTTER = register("block.brazier.close_shutter");
    public static final Supplier<SoundEvent> BRAZIER_DEACTIVATE = register("block.brazier.deactivate");
    public static final Supplier<SoundEvent> BRAZIER_EJECT_ITEM = register("block.brazier.eject_item");
    public static final Supplier<SoundEvent> BRAZIER_REJECT_REWARDED_PLAYER = register("block.brazier.reject_rewarded_player");
    public static final Supplier<SoundEvent> BRAZIER_FALL = register("block.brazier.fall");
    public static final Supplier<SoundEvent> BRAZIER_HIT = register("block.brazier.hit");
    public static final Supplier<SoundEvent> BRAZIER_INSERT_ITEM = register("block.brazier.insert_item");
    public static final Supplier<SoundEvent> BRAZIER_INSERT_ITEM_FAIL = register("block.brazier.insert_item_fail");
    public static final Supplier<SoundEvent> BRAZIER_OPEN_SHUTTER = register("block.brazier.open_shutter");
    public static final Supplier<SoundEvent> BRAZIER_PLACE = register("block.brazier.place");
    public static final Supplier<SoundEvent> BRAZIER_STEP = register("block.brazier.step");

    public static final Supplier<SoundEvent> WILDFIRE_AMBIENT = register("entity.wildfire.ambient");
    public static final Supplier<SoundEvent> WILDFIRE_HURT = register("entity.wildfire.hurt");
    public static final Supplier<SoundEvent> WILDFIRE_DEATH = register("entity.wildfire.death");

     */


    private static Supplier<SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Rodspawn.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void init(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
