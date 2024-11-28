package com.jasperclarke.difficulty;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.Objects;

public class StateManager extends PersistentState {
    public Boolean difficultToggled = false;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean("difficultToggled", difficultToggled);
        return nbt;
    }

    public static StateManager createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateManager state = new StateManager();
        state.difficultToggled = tag.getBoolean("difficultToggled");
        return state;
    }

    private static Type<StateManager> type = new Type<>(
            StateManager::new, // If there's no 'StateManager' yet create one
            StateManager::createFromNbt, // If there is a 'StateManager' NBT, parse it with 'createFromNbt'
            null // Supposed to be an 'DataFixTypes' enum, but we can just pass null
    );

    public static StateManager getServerState(MinecraftServer server) {
        // (Note: arbitrary choice to use 'World.OVERWORLD' instead of 'World.END' or 'World.NETHER'.  Any work)
        PersistentStateManager persistentStateManager = Objects.requireNonNull(server.getWorld(World.OVERWORLD)).getPersistentStateManager();

        // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateManager' and
        // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
        // 'StateManager' NBT on disk to our function 'StateManager::createFromNbt'.
        StateManager state = persistentStateManager.getOrCreate(type, Difficulty.MOD_ID);

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.markDirty();

        return state;
    }
}
