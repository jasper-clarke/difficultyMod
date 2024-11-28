package com.jasperclarke.difficultymod;

import com.jasperclarke.difficultymod.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DifficultyMod implements ModInitializer {
	public static final String MOD_ID = "difficultymod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
	}
}