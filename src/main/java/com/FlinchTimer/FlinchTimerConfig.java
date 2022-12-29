package com.FlinchTimer;

import net.runelite.client.config.*;

@ConfigGroup("Flinch Timer")
public interface FlinchTimerConfig extends Config
{
	@ConfigSection(
			name = "General",
			description = "General settings",
			position = 0
	)
	String generalSettings = "generalSettings";

	@ConfigItem(
			keyName = "targetNPC",
			name = "NPC Name",
			description = "Name of target NPC.",
			position = 1,
			section = generalSettings
	)
	default String targetNPC()
	{
		return "";
	}

	@ConfigSection(
			name = "User Interface",
			description = "User interface settings",
			position = 2
	)
	String interfaceSettings = "interfaceSettings";
	@ConfigItem(
			keyName = "useOverheadCounters",
			name = "Enable Overhead Counting",
			description = "Renders tick count over head of target NPC.",
			position = 3,
			section = interfaceSettings
	)
	default boolean useOverheadCounters() { return false; }

	enum TimerColor {
		RED,
		YELLOW,
		GREEN,
		BLUE,
		CYAN,
		MAGENTA,
		ORANGE,
		PINK,
		WHITE,
		BLACK,
		DARK_GRAY,
		GRAY,
		LIGHT_GRAY,
	}
	@ConfigItem(
			keyName = "setWaitColor",
			name = "Wait Color",
			description = "Sets timer fill color while waiting for NPC to be flinchable.",
			position = 4,
			section = interfaceSettings
	)
	default TimerColor setWaitColor() {
		return TimerColor.RED;
	}

	@ConfigItem(
			keyName = "setAttackColor",
			name = "Attack Color",
			description = "Sets timer fill color for NPC that is ready to be flinched.",
			position = 5,
			section = interfaceSettings
	)
	default TimerColor setAttackColor() {
		return TimerColor.GREEN;
	}

	@Range(
			min = -250,
			max = 250
	)
	@ConfigItem(
			keyName = "setRenderHeight",
			name = "Timer Render Height",
			description = "Offsets timer z-axis render height.",
			position = 6,
			section = interfaceSettings
	)
	default int setRenderHeight() {
		return 70;
	}


	@ConfigSection(
			name = "Optional",
			description = "Optional settings",
			position = 7,
			closedByDefault = true
	)
	String optionalSettings = "optionalSettings";
	@ConfigItem(
			keyName = "useRetaliationDelay",
			name = "Enable Retaliation Delay Offset",
			description = "Calculates true flinch timer based on NPC retaliation delay.",
			position = 8,
			section = optionalSettings
	)
	default boolean useRetaliationDelay() { return false; }

	@Range(
			min = 1,
			max = 15
	)
	@ConfigItem(
			keyName = "retaliationDelay",
			name = "NPC Attack Speed",
			description = "Define attack speed of target NPC to calculate retaliation delay.",
			position = 9,
			section = optionalSettings
	)
	default int retaliationDelay() {
		return 4;
	};
}
