package com.jasperclarke.difficulty.mixin;

import com.jasperclarke.difficulty.item.ModItems;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class FirstJoinHardcoreMixin {
    @Inject(method = "playerTick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        // Check if it's the player's first tick in the world
        if (player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 1) {
            // Check if the world is in hardcore mode
            if (player.getWorld().getLevelProperties().isHardcore()) {
                PlayerInventory inventory = player.getInventory();
                ItemStack difficultyToggler = new ItemStack(ModItems.DIFFICULTY_TOGGLE_DISABLED);
                inventory.insertStack(difficultyToggler);
            }
        }
    }
}
