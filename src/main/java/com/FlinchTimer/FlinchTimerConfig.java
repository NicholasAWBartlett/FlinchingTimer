package com.FlinchTimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("Flinch Timer")
public interface FlinchTimerConfig extends Config
{
	@ConfigItem(
			keyName = "targetNPC",
			name = "NPC Name",
			description = "Name of target NPC.",
			position = 0
	)
	default String targetNPC()
	{
		return "";
	}

	@ConfigItem(
			keyName = "useAttackSpeed",
			name = "Enable NPC Attack Speed",
			description = "Calculates true flinch timer based on NPC attack speed.",
			position = 1
	)
	default boolean useAttackSpeed() { return false; }

	@Range(
			min = 0,
			max = 9
	)
	@ConfigItem(
			keyName = "attackSpeed",
			name = "NPC Attack Speed",
			description = "Define attack speed of target NPC",
			position = 2
	)
	default int attackSpeed() {
		return 4;
	};
}
