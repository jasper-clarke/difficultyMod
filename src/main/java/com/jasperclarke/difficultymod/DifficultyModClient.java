package com.jasperclarke.difficultymod;

import net.fabricmc.api.ClientModInitializer;

public class DifficultyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DifficultyMod.LOGGER.info("Hello Fabric client world!");
    }
}
