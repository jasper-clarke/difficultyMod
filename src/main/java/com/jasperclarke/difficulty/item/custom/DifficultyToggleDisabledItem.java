package com.jasperclarke.difficulty.item.custom;

import com.jasperclarke.difficulty.Difficulty;
import com.jasperclarke.difficulty.StateManager;
import com.jasperclarke.difficulty.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class DifficultyToggleDisabledItem extends Item {
    public DifficultyToggleDisabledItem(Settings settings) {
        super(settings);
    }


    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            StateManager serverState = StateManager.getServerState(world.getServer());
            serverState.difficultToggled = true;
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.AMBIENT, 1.0F, 1.0F);
            user.setStackInHand(hand, new ItemStack(ModItems.DIFFICULTY_TOGGLE_ENABLED));
            user.sendMessage(Text.of("Prepare for pain... Enabled: " + serverState.difficultToggled.toString()), true);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.of("Right to enable difficulty"));
        // Colored tooltip text
        tooltip.add(Text.of("§c§lUp for the challenge?"));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
