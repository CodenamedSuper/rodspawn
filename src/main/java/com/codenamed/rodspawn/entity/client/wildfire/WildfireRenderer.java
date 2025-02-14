package com.codenamed.rodspawn.entity.client.wildfire;

import com.codenamed.rodspawn.Rodspawn;
import com.codenamed.rodspawn.entity.Wildfire;
import com.codenamed.rodspawn.registry.RodspawnModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

public class WildfireRenderer extends MobRenderer<Wildfire, WildfireModel> {
    public WildfireRenderer(EntityRendererProvider.Context context) {
        super(context, new WildfireModel(context.bakeLayer(RodspawnModelLayers.PENGUIN)), 0.75f);
    }

    @Override
    public ResourceLocation getTextureLocation(Wildfire wildfire) {
        return ResourceLocation.fromNamespaceAndPath(Rodspawn.MOD_ID, "textures/entity/wildfire.png");
    }

    @Override
    public void render(Wildfire entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    protected int getBlockLightLevel(Wildfire entity, BlockPos pos) {
        return 15;
    }

}
