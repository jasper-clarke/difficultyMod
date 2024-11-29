package com.jasperclarke.difficultymod.ai;

import com.jasperclarke.difficultymod.StateManager;
import com.jasperclarke.difficultymod.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

    public static class ZombieBreakBlockGoal extends Goal {
        private final ZombieEntity zombie;
        private BlockPos targetBlock;
        private int breakingTime;
        private int lastBreakingProgress = -1;
        private PlayerEntity targetPlayer;
        private static final double MAX_BREAK_DISTANCE = 5.0;

        public ZombieBreakBlockGoal(ZombieEntity zombie) {
            this.zombie = zombie;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
        }

        private PlayerEntity findNearestPlayer() {
            // Find nearest player within 32 blocks, ignoring line of sight
            return zombie.getWorld().getClosestPlayer(
                    zombie.getX(), zombie.getY(), zombie.getZ(),
                    32.0D, // Range
                    true   // Ignore line of sight
            );
        }

        private boolean canPathDirectlyToPlayer() {
            if (targetPlayer == null) return false;
            Path path = zombie.getNavigation().findPathTo(targetPlayer, 0);
            return path != null && path.reachesTarget();
        }

        private BlockPos findBlockToBreak() {
            if (targetPlayer == null) return null;

            if (canPathDirectlyToPlayer()) return null;

            Vec3d zombiePos = zombie.getPos();
            Vec3d playerPos = targetPlayer.getPos();

            // Check if zombie is too far from the block
            if (zombiePos.squaredDistanceTo(playerPos) > MAX_BREAK_DISTANCE * MAX_BREAK_DISTANCE) {
                return null;
            }

            // Check if there's a height difference
            double yDiff = playerPos.y - zombiePos.y;

            // If player is above, check blocks below them first
            if (yDiff > 1) {
                BlockPos belowPlayer = targetPlayer.getBlockPos().down();
                BlockState belowState = zombie.getWorld().getBlockState(belowPlayer);
                if (!belowState.isAir() && canBreakBlock(belowState, belowPlayer)) {
                    return belowPlayer;
                }
            }

            // Get direction to player
            Vec3d direction = playerPos.subtract(zombiePos).normalize();

            // Check blocks in the path
            for (int i = 1; i <= 4; i++) {
                Vec3d checkPos = zombiePos.add(direction.multiply(i));
                BlockPos pos = new BlockPos((int)checkPos.x, (int)zombiePos.y, (int)checkPos.z);

                // Check block at feet level
                if (checkBlock(pos)) return pos;

                // Check block above feet level
                if (checkBlock(pos.up())) return pos.up();

                // If we're trying to go up or down, check those blocks too
                if (yDiff > 1) {
                    if (checkBlock(pos.up(2))) return pos.up(2);
                } else if (yDiff < -1) {
                    if (checkBlock(pos.down())) return pos.down();
                }
            }

            return null;
        }

        private boolean checkBlock(BlockPos pos) {
            BlockState state = zombie.getWorld().getBlockState(pos);
            return !state.isAir() && canBreakBlock(state, pos);
        }

        private boolean canBreakBlock(BlockState state, BlockPos pos) {
            float hardness = state.getHardness(zombie.getWorld(), pos);
            if (hardness < 0 || hardness >= 50.0f || state.isOf(Blocks.BEDROCK)) {
                return false;
            }

            ItemStack heldItem = zombie.getEquippedStack(EquipmentSlot.MAINHAND);

            // Check if the block is appropriate for the held tool
            // For pickaxe, allow any block that's not in the shovel list
            if (heldItem.isOf(Items.IRON_SHOVEL)) {
                // List of blocks that can be efficiently mined with a shovel
                return state.isOf(Blocks.DIRT) ||
                        state.isOf(Blocks.GRASS_BLOCK) ||
                        state.isOf(Blocks.SAND) ||
                        state.isOf(Blocks.GRAVEL) ||
                        state.isOf(Blocks.SOUL_SAND) ||
                        state.isOf(Blocks.SOUL_SOIL) ||
                        state.isOf(Blocks.MYCELIUM) ||
                        state.isOf(Blocks.SNOW) ||
                        state.isOf(Blocks.SNOW_BLOCK) ||
                        state.isOf(Blocks.CLAY);
            } else return heldItem.isOf(Items.IRON_PICKAXE);
        }

        @Override
        public boolean canStart() {
            if (!zombie.getWorld().isClient()) {
                StateManager serverState = StateManager.getServerState(zombie.getWorld().getServer());
                if (!serverState.difficultyToggled) {
                    return false;
                }
            }

            if (!zombie.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.IRON_PICKAXE) ||
                    !zombie.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.IRON_SHOVEL)) {
                return false;
            }

            targetPlayer = findNearestPlayer();
            if (targetPlayer == null) return false;

            if (canPathDirectlyToPlayer()) return false;

            targetBlock = findBlockToBreak();
            return targetBlock != null;
        }

        @Override
        public void start() {
            breakingTime = 0;
            lastBreakingProgress = -1;
        }

        @Override
        public void stop() {
            if (targetBlock != null) {
                zombie.getWorld().setBlockBreakingInfo(zombie.getId(), targetBlock, -1);
            }
            targetBlock = null;
        }

        @Override
        public boolean shouldContinue() {
            return targetBlock != null
                    && targetPlayer != null
                    && targetPlayer.isAlive()
                    && !zombie.getWorld().getBlockState(targetBlock).isAir()
                    && (zombie.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.IRON_PICKAXE) ||
                            zombie.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.IRON_SHOVEL))
                    && zombie.squaredDistanceTo(targetPlayer) <= 32 * 32;
        }

        @Override
        public void tick() {
            if (targetBlock == null) return;

            BlockState state = zombie.getWorld().getBlockState(targetBlock);
            float hardness = state.getHardness(zombie.getWorld(), targetBlock);

            // Move closer to the block if too far
            if (zombie.squaredDistanceTo(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY() + 0.5,
                    targetBlock.getZ() + 0.5) > 4.0) {
                zombie.getNavigation().startMovingTo(
                        targetBlock.getX() + 0.5,
                        targetBlock.getY(),
                        targetBlock.getZ() + 0.5,
                        1.0);
            } else {
                zombie.getNavigation().stop();
            }

            zombie.getLookControl().lookAt(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY() + 0.5,
                    targetBlock.getZ() + 0.5
            );

            breakingTime++;
            int progress = (int)((breakingTime / (hardness * 30.0F)) * 10.0F);

            if (progress != lastBreakingProgress) {
                zombie.getWorld().setBlockBreakingInfo(zombie.getId(), targetBlock, progress);
                lastBreakingProgress = progress;
            }

            if (progress >= 10) {
                zombie.getWorld().breakBlock(targetBlock, true, zombie);
                targetBlock = null;
                if (!canPathDirectlyToPlayer()) {
                    targetBlock = findBlockToBreak();
                    if (targetBlock != null) {
                        breakingTime = 0;
                        lastBreakingProgress = -1;
                    }
                }
            }

            // Play breaking sound and particles
            if (breakingTime % 4 == 0) {
                zombie.getWorld().playSound(
                        null,
                        targetBlock,
                        state.getSoundGroup().getHitSound(),
                        SoundCategory.HOSTILE,
                        1.0F,
                        0.8F
                );
            }
        }
    }
}
