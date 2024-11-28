package com.jasperclarke.difficultymod.ai;

import com.jasperclarke.difficultymod.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class ZombieAI {
    private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = item -> !item.cannotPickup() && item.isAlive() && item.getStack().isOf(ModItems.PUTRID_FLESH);

    public static class AquirePutridFleshGoal extends Goal {
        private final ZombieEntity zombie;
        private int eatingTicks = 0;
        private static final int EATING_DURATION = 60; // 3 seconds (20 ticks per second)

        public AquirePutridFleshGoal(ZombieEntity zombie) {
            this.zombie = zombie;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (!this.zombie.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                return this.zombie.getEquippedStack(EquipmentSlot.MAINHAND).getItem() == ModItems.PUTRID_FLESH; // Keep running if we're holding putrid flesh
            } else {
                List<ItemEntity> list = this.zombie.getWorld()
                        .getEntitiesByClass(ItemEntity.class, this.zombie.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
                return !list.isEmpty();
            }
        }

        @Override
        public void tick() {
            ItemStack heldItem = this.zombie.getEquippedStack(EquipmentSlot.MAINHAND);

            if (heldItem.isOf(ModItems.PUTRID_FLESH)) {
                // Stop movement while eating
                this.zombie.getNavigation().stop();

                eatingTicks++;

                // Play eating sound every 2 ticks while eating
                if (eatingTicks % 2 == 0) {
                    this.zombie.getWorld().playSound(
                            null,
                            this.zombie.getX(),
                            this.zombie.getY(),
                            this.zombie.getZ(),
                            net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EAT,
                            net.minecraft.sound.SoundCategory.HOSTILE,
                            0.5F + 0.5F * (float)this.zombie.getRandom().nextInt(2),
                            (this.zombie.getRandom().nextFloat() - this.zombie.getRandom().nextFloat()) * 0.2F + 1.0F
                    );
                }

                if (eatingTicks >= EATING_DURATION) {
                    // Consume the item after 3 seconds
                    this.zombie.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    this.zombie.setVelocity(0, 1.5, 0);
                    this.zombie.getWorld().playSound(
                            null,
                            this.zombie.getX(),
                            this.zombie.getY(),
                            this.zombie.getZ(),
                            SoundEvents.ENTITY_GENERIC_EXPLODE,
                            SoundCategory.PLAYERS,
                            1F,
                            1F
                    );
                    this.zombie.damage(this.zombie.getServer().getWorld(this.zombie.getWorld().getRegistryKey()), this.zombie.getDamageSources().explosion(this.zombie, this.zombie), Float.MAX_VALUE);
                    eatingTicks = 0;
                }
            } else {
                List<ItemEntity> list = this.zombie.getWorld()
                        .getEntitiesByClass(ItemEntity.class, this.zombie.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);

                if (!list.isEmpty()) {
                    ItemEntity nearest = list.getFirst();
                    this.zombie.getNavigation().startMovingTo(nearest, 1.2F);

                    // Pick up the item if we're close enough
                    if (this.zombie.getBoundingBox().expand(1.0).intersects(nearest.getBoundingBox())) {
                        this.zombie.equipStack(EquipmentSlot.MAINHAND, nearest.getStack().copyWithCount(1));
                        nearest.getStack().decrement(1);
                        if (nearest.getStack().isEmpty()) {
                            nearest.discard();
                        }
                    }
                }
            }
        }

        @Override
        public void start() {
            List<ItemEntity> list = this.zombie.getWorld()
                    .getEntitiesByClass(ItemEntity.class, this.zombie.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            if (!list.isEmpty()) {
                this.zombie.getNavigation().startMovingTo((Entity)list.getFirst(), 1.2F);
            }
        }

        @Override
        public void stop() {
            eatingTicks = 0;
        }
    }
}
