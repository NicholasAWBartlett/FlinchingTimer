package com.FlinchTimer;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.util.Objects;

@Slf4j
@PluginDescriptor(name = "Flinch Timer")
public class FlinchTimerMain extends Plugin {
    @Inject
    private Client client;

    @Inject
    private FlinchTimerConfig config;

    @Inject
    private NPCManager npcManager;

    private String NPC_NAME = null;
    private boolean ENABLE_ATTACK_SPEED = false;
    private int NPC_ATTACK_SPEED = 4;
    private NPC currentNPC = null;
    private int ticksOutOfCombat = 0;
    private int retaliationDelay = 0;
    private boolean retaliationDelayActive = false;


    @Override
    protected void startUp() throws Exception {
        log.info("ESG Flinch Timer Initialized!");
        NPC_NAME = config.targetNPC().trim();
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("ESG Flinch Timer Stopped!");
        if (currentNPC != null) {
            currentNPC.setOverheadText("");
            currentNPC = null;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
        }
    }

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event) {

        if (!(event.getActor() instanceof NPC)) {
            return;
        }

        final NPC npc = (NPC) event.getActor();

        if (Objects.equals(npc.getName(), NPC_NAME) && npc.getInteracting() == client.getLocalPlayer()) {
            ticksOutOfCombat = 0;
            if (npc.isDead() && npc == currentNPC) {
                currentNPC = null;
                return;
            }
            if (npc != currentNPC) {
                if (currentNPC != null) {
                    currentNPC.setOverheadText("");
                }
                currentNPC = npc;
            }
        }
    }

    @Subscribe
    public void onHitsplatApplied(final HitsplatApplied event) {
        if (event.getActor().getName() == null || !(event.getActor() instanceof NPC)) {
            return;
        }

        final NPC npc = (NPC) event.getActor();
        if (Objects.equals(npc.getName(), NPC_NAME) && npc.getInteracting() == client.getLocalPlayer()) {
            if (npc.isDead() && npc == currentNPC) {
                ticksOutOfCombat = 0;
                currentNPC = null;
                return;
            }
            if (client.getLocalPlayer().getAnimation() != -1) {
                if (ENABLE_ATTACK_SPEED) {
                    retaliationDelayActive = true;
                    retaliationDelay = Math.floorDiv(NPC_ATTACK_SPEED, 2);
                }
                ticksOutOfCombat = 0;
                if (npc != currentNPC) {
                    if (currentNPC != null) {
                        currentNPC.setOverheadText("");
                    }
                    currentNPC = npc;
                }
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!Objects.equals(config.targetNPC(), NPC_NAME)) {
            NPC_NAME = config.targetNPC().trim();
        }
        ENABLE_ATTACK_SPEED = config.useAttackSpeed();

        if (currentNPC != null && !retaliationDelayActive) {
            if (retaliationDelayActive) {
                currentNPC.setOverheadText(String.valueOf(retaliationDelay));
                if (retaliationDelay > 0) {
                    retaliationDelay -= 1;
                } else {
                    retaliationDelayActive = false;
                }
            } else {
                if (ticksOutOfCombat < 8) {
                    currentNPC.setOverheadText(String.valueOf(ticksOutOfCombat += 1));
                } else {
                    currentNPC.setOverheadText("FLINCHABLE");
                }
            }
        }
    }

    @Provides
    FlinchTimerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FlinchTimerConfig.class);
    }
}
