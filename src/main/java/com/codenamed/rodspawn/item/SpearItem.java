package com.codenamed.rodspawn.item;

import com.codenamed.rodspawn.registry.RodspawnItemAbilities;
import com.codenamed.rodspawn.registry.RodspawnItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpearItem extends Item {

    public static final ResourceLocation BASE_BURNING_TIME_ID = ResourceLocation.withDefaultNamespace("base_burning_time");
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final int FLAMETHROW_RANGE = 4;

    public SpearItem(Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().
                add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ENTITY_INTERACTION_RANGE_ID, 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 4.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9000000953674316, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0F, 2);
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }


    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        flamethrow(stack, level, livingEntity, timeCharged);
    }

    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(player.getItemInHand(usedHand));
    }

    public void flamethrow(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (livingEntity instanceof Player player) {
            stack.hurtAndBreak(3, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            level.playSound((Player)null, player, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);


            for (Entity target : getFlamethrowedEntities(level, player)) {
                if (target instanceof LivingEntity targetLivingEntity) {
                    targetLivingEntity.igniteForSeconds(12);
                    hurtEnemy(stack, targetLivingEntity, player);

                }
            }

            float flameSpeed = 0.1f;

            for (int i = 0; i < 1; i++) {

                level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), flameSpeed, 0, 0);
                level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), flameSpeed, 0, flameSpeed);
                level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), 0, 0, flameSpeed);
                level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), -flameSpeed, 0, 0);
                level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), -flameSpeed, 0, -flameSpeed);
                level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), 0, 0, -flameSpeed);
                level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), flameSpeed, 0, -flameSpeed);
                level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), -flameSpeed, 0, flameSpeed);

            }
        }
    }


    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            target.igniteForSeconds(6);

        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return RodspawnItemAbilities.DEFAULT_SPEAR_ACTIONS.contains(itemAbility);
    }

    public  List<Entity> getFlamethrowedEntities(Level level, Player player) {

        BlockPos pos = player.getOnPos();
        BlockPos start = new BlockPos(pos.getX() - FLAMETHROW_RANGE, pos.getY(), pos.getZ() - FLAMETHROW_RANGE);
        BlockPos end = new BlockPos(pos.getX() + FLAMETHROW_RANGE, pos.getY() + FLAMETHROW_RANGE, pos.getZ() + FLAMETHROW_RANGE);


        return  level.getEntities(player, new AABB(start.getCenter(), end.getCenter()), Entity::isAlive);
    }
}
