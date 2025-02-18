package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RodspawnEnchantmentEffectComponents {
    public static final DeferredRegister<DataComponentType<?>> ENCHANTMENT_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Rodspawn.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentValueEffect>> SPEAR_LAUNCH_STRENGTH =
            ENCHANTMENT_COMPONENT_TYPES.register("spear_launch_strength",
            () -> DataComponentType.<EnchantmentValueEffect>builder()
            .persistent(EnchantmentValueEffect.CODEC)
            .build());

    public static void init(IEventBus eventBus) {
        ENCHANTMENT_COMPONENT_TYPES.register(eventBus);
    }
}