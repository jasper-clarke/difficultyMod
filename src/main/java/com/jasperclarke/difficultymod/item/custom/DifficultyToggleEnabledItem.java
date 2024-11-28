package com.jasperclarke.difficultymod.item.custom;

import com.jasperclarke.difficultymod.StateManager;
import com.jasperclarke.difficultymod.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

public class DifficultyToggleEnabledItem extends Item {
    public DifficultyToggleEnabledItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            StateManager serverState = StateManager.getServerState(world.getServer());
            serverState.difficultToggled = false;
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_CAT_DEATH, SoundCategory.AMBIENT, 1.0F, 1.0F);
            user.setStackInHand(hand, new ItemStack(ModItems.DIFFICULTY_TOGGLE_DISABLED));
            user.sendMessage(Text.of("Given up already? Enabled: " + serverState.difficultToggled.toString()), true);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.of("Right to disable difficulty"));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
