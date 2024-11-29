package com.jasperclarke.difficultymod.mixin;

import com.jasperclarke.difficultymod.StateManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonEntity.class)
public abstract class SkeletonBeefMixin extends HostileEntity {
    @Mutable
    @Shadow @Final private static int HARD_ATTACK_INTERVAL;

    @Mutable
    @Shadow @Final private static int REGULAR_ATTACK_INTERVAL;

    @Shadow protected abstract PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom);

    protected SkeletonBeefMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(EntityType entityType, World world, CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            StateManager serverState = StateManager.getServerState(this.getWorld().getServer());
            if (serverState.difficultyToggled) {
                HARD_ATTACK_INTERVAL = 5;
                REGULAR_ATTACK_INTERVAL = 10;
            }
        }
    }

    /**
     * @author Jasper Clarke
     * @reason Increase projectile power
     */
    @Overwrite
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
        ItemStack itemStack2 = this.getProjectileType(itemStack);
        PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack2, pullProgress, itemStack);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            ProjectileEntity.spawnWithVelocity(
                    persistentProjectileEntity, serverWorld, itemStack2, d, e + g * 0.2F, f, 5.0F, (float)(14 - serverWorld.getDifficulty().getId() * 4)
            );
        }

        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

}
