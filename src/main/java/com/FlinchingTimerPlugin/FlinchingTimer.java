package com.FlinchingTimerPlugin;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
        name = "Flinching Timer",
        description = "Renders customizable flinching timer over designated NPC",
        tags = {"combat", "flinch", "flinching", "timers"}
)
public class FlinchingTimer extends Plugin {
    @Inject
    private Client client;

    @Inject
    private FlinchingTimerConfig config;

    @Inject
    private FlinchingTimerOverlay flinchingTimerOverlay;
    @Inject
    private OverlayManager overlayManager;

    public NPC CURRENT_NPC = null;
    public String NPC_NAME = null;
    public int TICKS_OUT_OF_COMBAT = 0;
    public int NPC_ATTACK_SPEED = 4;
    public int RENDER_HEIGHT = 75;
    public long NPC_TAGGED_TIME = 0;
    public boolean ENABLE_RETALIATION_DELAY = false;
    public boolean ENABLE_OVERHEAD_COUNTERS = false;
    public FlinchingTimerConfig.TimerColor WAIT_COLOR = null;
    public FlinchingTimerConfig.TimerColor ATTACK_COLOR = null;


    @Override
    protected void startUp() {
        try {
            NPC_NAME = config.targetNPC().toLowerCase().trim();
            NPC_ATTACK_SPEED = config.retaliationDelay();
            RENDER_HEIGHT = config.setRenderHeight();
            ENABLE_RETALIATION_DELAY = config.useRetaliationDelay();
            ENABLE_OVERHEAD_COUNTERS = config.useOverheadCounters();
            WAIT_COLOR = config.setWaitColor();
            ATTACK_COLOR = config.setAttackColor();
        } catch (Exception ex) {
            log.error("Failed to update configuration parameters", ex);
        }
    }

    @Override
    protected void shutDown() {
        try {
            if (CURRENT_NPC != null) {
                CURRENT_NPC.setOverheadText("");
                overlayManager.remove(flinchingTimerOverlay);
                TICKS_OUT_OF_COMBAT = 0;
                CURRENT_NPC = null;
            }
        } catch (Exception ex) {
            log.error("Failed to modify configuration variables", ex);
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        String key = event.getKey();

        switch (key) {
            case "targetNPC":
                NPC_NAME = config.targetNPC().toLowerCase().trim();
                break;
            case "useRetaliationDelay":
                ENABLE_RETALIATION_DELAY = config.useRetaliationDelay();
                break;
            case "retaliationDelay":
                NPC_ATTACK_SPEED = config.retaliationDelay();
                break;
            case "setRenderHeight":
                RENDER_HEIGHT = config.setRenderHeight();
                break;
            case "useOverheadCounters":
                ENABLE_OVERHEAD_COUNTERS = config.useOverheadCounters();
                if (CURRENT_NPC != null) {
                    CURRENT_NPC.setOverheadText("");
                }
                break;
            case "setWaitColor":
                WAIT_COLOR = config.setWaitColor();
                break;
            case "setAttackColor":
                ATTACK_COLOR = config.setAttackColor();
                break;
        }
    }

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event) {

        if (!(event.getActor() instanceof NPC)) {
            return;
        }

        final NPC npc = (NPC) event.getActor();

        if (npc.getName() == null) {
            return;
        }

        Instant now = Instant.now();

        if (Objects.equals(npc.getName().toLowerCase(), NPC_NAME) && npc.getInteracting() == client.getLocalPlayer()) {
            TICKS_OUT_OF_COMBAT = 0;
            overlayManager.remove(flinchingTimerOverlay);
            NPC_TAGGED_TIME = now.toEpochMilli();
            if (npc.isDead() && npc == CURRENT_NPC) {
                CURRENT_NPC = null;
                return;
            }
            if (npc != CURRENT_NPC) {
                if (CURRENT_NPC != null && ENABLE_OVERHEAD_COUNTERS) {
                    CURRENT_NPC.setOverheadText("");
                }
                CURRENT_NPC = npc;
            }
            overlayManager.add(flinchingTimerOverlay);
        }
    }

    @Subscribe
    public void onNpcDespawned(final NpcDespawned event) {
        if (event.getActor().getName() == null || !(event.getActor() instanceof NPC)) {
            return;
        }

        final NPC npc = (NPC) event.getActor();

        if (npc == CURRENT_NPC) {
            TICKS_OUT_OF_COMBAT = 0;
            overlayManager.remove(flinchingTimerOverlay);
            CURRENT_NPC = null;
        }
    }

    @Subscribe
    public void onHitsplatApplied(final HitsplatApplied event) {
        if (event.getActor().getName() == null || !(event.getActor() instanceof NPC)) {
            return;
        }

        final NPC npc = (NPC) event.getActor();

        if (npc.getName() == null) {
            return;
        }

        Instant now = Instant.now();

        if (Objects.equals(npc.getName().toLowerCase(), NPC_NAME) && npc.getInteracting() == client.getLocalPlayer()) {
            if (npc.isDead() && npc == CURRENT_NPC) {
                TICKS_OUT_OF_COMBAT = 0;
                overlayManager.remove(flinchingTimerOverlay);
                CURRENT_NPC = null;
                return;
            }
            if (client.getLocalPlayer().getAnimation() != -1) {
                TICKS_OUT_OF_COMBAT = 0;
                overlayManager.remove(flinchingTimerOverlay);
                NPC_TAGGED_TIME = now.toEpochMilli();
                if (npc != CURRENT_NPC) {
                    if (CURRENT_NPC != null && ENABLE_OVERHEAD_COUNTERS) {
                        CURRENT_NPC.setOverheadText("");
                    }
                    CURRENT_NPC = npc;
                }
                overlayManager.add(flinchingTimerOverlay);
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (CURRENT_NPC != null) {
            if (ENABLE_RETALIATION_DELAY && ENABLE_OVERHEAD_COUNTERS) {
                TICKS_OUT_OF_COMBAT = updateTicks(TICKS_OUT_OF_COMBAT, (Math.floorDiv(NPC_ATTACK_SPEED, 2) + 8), CURRENT_NPC);
            } else {
                if (ENABLE_OVERHEAD_COUNTERS) {
                    TICKS_OUT_OF_COMBAT = updateTicks(TICKS_OUT_OF_COMBAT, 8, CURRENT_NPC);
                }
            }
        }
    }

    private int updateTicks(int currentTicks, int tickThreshold, NPC currentNPC) {
        if (currentTicks < tickThreshold) {
            currentTicks += 1;
            currentNPC.setOverheadText(String.valueOf(currentTicks));
        } else {
            currentNPC.setOverheadText("FLINCHABLE");
        }
        return currentTicks;
    }

    @Provides
    FlinchingTimerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FlinchingTimerConfig.class);
    }
}
