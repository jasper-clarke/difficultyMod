package com.jasperclarke.difficulty;

import net.fabricmc.api.ClientModInitializer;

public class DifficultyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Difficulty.LOGGER.info("Hello Fabric client world!");
    }
}
