package com.jasperclarke.difficultymod.mixin;

import com.jasperclarke.difficultymod.StateManager;
import com.jasperclarke.difficultymod.ai.ZombieAI;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public abstract class ZombieBeefMixin extends MobEntity {
    protected ZombieBeefMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void initCustomGoals(CallbackInfo ci) {
        this.goalSelector.add(0, new ZombieAI.AquirePutridFleshGoal((ZombieEntity) (Object) this));
    }

    @Inject(method = "initAttributes", at = @At("TAIL"))
    private void modifyAttributesForDifficulty(CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            StateManager serverState = StateManager.getServerState(this.getWorld().getServer());
            if (serverState.difficultToggled) {
                this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(100.0D);
                this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
                this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(4.0);
                this.getAttributeInstance(EntityAttributes.ARMOR).setBaseValue(3.0);
            }
        }
    }
}
