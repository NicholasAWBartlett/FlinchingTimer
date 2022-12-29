package com.FlinchTimer;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;


public class FlinchTimerOverlay extends Overlay {
    private final Client client;
    private final FlinchTimer flinchTimer;

    @Inject
    private FlinchTimerOverlay(Client client, FlinchTimer flinchTimer) {
        this.flinchTimer = flinchTimer;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        NPC currentNPC = flinchTimer.CURRENT_NPC;

        if (currentNPC != null) {
            float percent;
            Instant now = Instant.now();
            int renderZAxis = currentNPC.getModelHeight() + flinchTimer.RENDER_HEIGHT;

            if (flinchTimer.ENABLE_RETALIATION_DELAY) {
                percent = (now.toEpochMilli() - flinchTimer.NPC_TAGGED_TIME) / (float) (((Math.floorDiv(flinchTimer.NPC_ATTACK_SPEED, 2) + 8) * 1000) * .625);
            } else {
                percent = (now.toEpochMilli() - flinchTimer.NPC_TAGGED_TIME) / (float) 5000;
            }

            Point point = Perspective.localToCanvas(client, currentNPC.getLocalLocation(), client.getPlane(), renderZAxis);
            Color pieBorderColor = Color.YELLOW;
            Color pieFillColor;

            if (percent > 1f) {
                pieFillColor = updateColor(flinchTimer.ATTACK_COLOR);
            } else {
                pieFillColor = updateColor(flinchTimer.WAIT_COLOR);
            }

            ProgressPieComponent progressPieComponent = new ProgressPieComponent();
            progressPieComponent.setBorderColor(pieBorderColor);
            progressPieComponent.setFill(pieFillColor);
            progressPieComponent.setPosition(point);
            progressPieComponent.setProgress(percent);
            progressPieComponent.render(graphics);

        }
        return null;
    }

    private Color updateColor(FlinchTimerConfig.TimerColor color) {
        switch (color) {
            case RED:
                return Color.RED;
            case YELLOW:
                return Color.YELLOW;
            case GREEN:
                return Color.GREEN;
            case BLUE:
                return Color.BLUE;
            case CYAN:
                return Color.CYAN;
            case MAGENTA:
                return Color.MAGENTA;
            case ORANGE:
                return Color.ORANGE;
            case PINK:
                return Color.PINK;
            case BLACK:
                return Color.BLACK;
            case GRAY:
                return Color.GRAY;
            case DARK_GRAY:
                return Color.DARK_GRAY;
            case LIGHT_GRAY:
                return Color.LIGHT_GRAY;
            default:
                return Color.WHITE;
        }
    }
}
