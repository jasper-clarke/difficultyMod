package com.jasperclarke.difficultymod.mixin;

import com.jasperclarke.difficultymod.item.ModItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class FirstJoinHardcoreMixin extends PlayerEntity {
    public FirstJoinHardcoreMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerStatHandler getStatHandler();

    @Inject(method = "playerTick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        // Check if it's the player's first tick in the world
        if (this.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH)) == 1 && this.getWorld().getLevelProperties().isHardcore()) {
            this.getInventory().insertStack(new ItemStack(ModItems.DIFFICULTY_TOGGLE_DISABLED));
        }
    }
}
