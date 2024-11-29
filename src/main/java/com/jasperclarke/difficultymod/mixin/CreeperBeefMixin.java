package com.jasperclarke.difficultymod.mixin;

import com.jasperclarke.difficultymod.StateManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class CreeperBeefMixin extends HostileEntity {
    @Shadow private int explosionRadius;
    @Shadow private int fuseTime;

    protected CreeperBeefMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(EntityType entityType, World world, CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            StateManager serverState = StateManager.getServerState(this.getWorld().getServer());
            if (serverState.difficultyToggled) {
                this.explosionRadius = 6;
                this.fuseTime = 10;
                this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.35F);
                this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
            }
        }
    }
}
