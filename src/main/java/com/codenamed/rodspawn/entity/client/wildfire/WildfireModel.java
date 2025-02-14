package com.codenamed.rodspawn.entity.client.wildfire;

import com.codenamed.rodspawn.entity.Wildfire;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class WildfireModel extends HierarchicalModel<Wildfire> {
    private final ModelPart wildfire;
    private final ModelPart head;
    private final ModelPart plates;
    private final ModelPart rod;

    public WildfireModel(ModelPart root) {
        this.wildfire = root.getChild("wildfire");
        this.head = this.wildfire.getChild("head");
        this.plates = this.wildfire.getChild("plates");
        this.rod = this.wildfire.getChild("rod");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wildfire = partdefinition.addOrReplaceChild("wildfire", CubeListBuilder.create(), PartPose.offset(-4.0F, 33.5F, -6.5F));

        PartDefinition head = wildfire.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 2).addBox(-5.0F, 0.375F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(23, 25).addBox(-3.0F, -0.625F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(11, 35).addBox(-2.0F, -4.625F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -39.875F, 6.5F));

        PartDefinition spearhead2_r1 = head.addOrReplaceChild("spearhead2_r1", CubeListBuilder.create().texOffs(11, 35).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.625F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition plates = wildfire.addOrReplaceChild("plates", CubeListBuilder.create(), PartPose.offset(4.0F, -9.5F, 6.5F));

        PartDefinition plate4_r1 = plates.addOrReplaceChild("plate4_r1", CubeListBuilder.create().texOffs(64, 12).addBox(-5.0F, -3.8827F, 11.1859F, 10.0F, 17.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.5F, 0.0F, -2.7925F, 0.7854F, -3.1416F));

        PartDefinition plate3_r1 = plates.addOrReplaceChild("plate3_r1", CubeListBuilder.create().texOffs(64, 12).addBox(-5.0F, -3.8827F, -14.1859F, 10.0F, 17.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.5F, 0.0F, 2.7925F, 0.7854F, 3.1416F));

        PartDefinition plate2_r1 = plates.addOrReplaceChild("plate2_r1", CubeListBuilder.create().texOffs(64, 12).addBox(-5.0F, -3.8827F, 11.1859F, 10.0F, 17.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.5F, 0.0F, 0.3491F, 0.7854F, 0.0F));

        PartDefinition plate1_r1 = plates.addOrReplaceChild("plate1_r1", CubeListBuilder.create().texOffs(64, 12).addBox(-5.0F, -3.8827F, -14.1859F, 10.0F, 17.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.5F, 0.0F, -0.3491F, 0.7854F, 0.0F));

        PartDefinition rod = wildfire.addOrReplaceChild("rod", CubeListBuilder.create().texOffs(45, 34).addBox(2.0F, -28.5F, 4.5F, 4.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(Wildfire wildfire, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animateWalk(WildfireAnimations.IDLE, limbSwing, limbSwingAmount, 1f, 1);
        this.animate(wildfire.idleState, WildfireAnimations.IDLE, ageInTicks, 1);
    }

    private void applyHeadRotation(float headYaw, float headPitch) {
        headYaw = Mth.clamp(headYaw, -30f, 30f);
        headPitch = Mth.clamp(headPitch, -25f, 45);

        this.head.yRot = headYaw * ((float) Math.PI / 180);
        this.head.xRot = headPitch * ((float) Math.PI / 180);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        wildfire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return wildfire;
    }
}
