package com.jasperclarke.difficulty.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.jasperclarke.difficulty.mixin.ZombieEntityAccessor;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

@Mixin(ZombieEntity.class)
public class ZombieLikesPutridFleshMixin {
    @Inject(method = "initGoals", at = @At("TAIL"))
    private void initCustomGoals(CallbackInfo ci) {
        ZombieEntity zombieEntity = (ZombieEntity) (Object) this;
        zombieEntity.goalSelector.add(1, new AquirePutridFleshGoal());
    }

    private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = item -> !item.cannotPickup() && item.isAlive();

    private static class AquirePutridFleshGoal extends Goal {
        ZombieEntity zombieEntity = (ZombieEntity) (Object) this;
        public AquirePutridFleshGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (!zombieEntity.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            } else {
                List<ItemEntity> list = zombieEntity.getWorld()
                        .getEntitiesByClass(ItemEntity.class, zombieEntity.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
                return !list.isEmpty() && zombieEntity.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
            }
        }

        @Override
        public void tick() {
            List<ItemEntity> list = zombieEntity.getWorld()
                    .getEntitiesByClass(ItemEntity.class, zombieEntity.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            ItemStack itemStack = zombieEntity.getEquippedStack(EquipmentSlot.MAINHAND);
            if (itemStack.isEmpty() && !list.isEmpty()) {
                zombieEntity.getNavigation().startMovingTo((Entity)list.get(0), 1.2F);
            }
        }

        @Override
        public void start() {
            List<ItemEntity> list = zombieEntity.getWorld()
                    .getEntitiesByClass(ItemEntity.class, zombieEntity.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            if (!list.isEmpty()) {
                zombieEntity.getNavigation().startMovingTo((Entity)list.get(0), 1.2F);
            }
        }
    }
}
