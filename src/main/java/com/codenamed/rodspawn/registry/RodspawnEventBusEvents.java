package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import com.codenamed.rodspawn.entity.Wildfire;
import com.codenamed.rodspawn.entity.client.wildfire.WildfireModel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Rodspawn.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class RodspawnEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RodspawnModelLayers.PENGUIN, WildfireModel::createBodyLayer);

    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RodspawnEntities.WILDFIRE.get(), Wildfire.createAttributes().build());

    }
}