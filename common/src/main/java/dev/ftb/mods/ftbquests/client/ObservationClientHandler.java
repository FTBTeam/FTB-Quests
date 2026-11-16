package dev.ftb.mods.ftbquests.client;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ObservationTask;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Client-side handling of observation tasks: tracks what the player is looking at, periodically tells the server
 * while the player is observing a task (the server verifies the observation itself), and renders the progress HUD.
 */
public class ObservationClientHandler {
    // ticks between packets sent to the server while observing, which the server uses as sampling points
    private static final long SAMPLE_INTERVAL = 5L;
    // ticks to wait for the server to confirm a completed observation before the client will observe that task again
    private static final long CONFIRM_TIMEOUT = 100L;

    private List<ObservationTask> observationTasks = null;
    private ObservationTask currentlyObserving = null;
    private long currentlyObservingTicks = 0L;

    // task id -> game time after which we stop waiting for the server's completion message
    private final Long2LongMap awaitingConfirmation = new Long2LongOpenHashMap();

    public void clearCachedData() {
        observationTasks = null;
        awaitingConfirmation.clear();
    }

    public void tick(Minecraft mc) {
        if (observationTasks == null) {
            observationTasks = ClientQuestFile.INSTANCE.collect(ObservationTask.class);
        }

        if (observationTasks.isEmpty()) {
            return;
        }

        currentlyObserving = null;

        TeamData selfTeamData = ClientQuestFile.INSTANCE.selfTeamData;
        HitResult fluidHit = null;
        for (ObservationTask task : observationTasks) {
            if (selfTeamData.isCompleted(task.getQuest()) || selfTeamData.isCompleted(task)) {
                continue;
            }
            HitResult hit = mc.hitResult;
            if (task.targetsFluid()) {
                // the vanilla crosshair target ignores fluids, so use a dedicated pick (once per tick)
                if (fluidHit == null) {
                    fluidHit = ObservationTask.pickIncludingFluids(mc.player);
                }
                hit = fluidHit;
            }
            if (hit == null || hit.getType() == HitResult.Type.MISS) {
                continue;
            }
            if (isAwaitingConfirmation(task, mc.level.getGameTime())) {
                // still waiting for the server to confirm: keep nudging it, in case its last sample was borderline
                if (!mc.isPaused() && mc.level.getGameTime() % SAMPLE_INTERVAL == 0L && task.observe(mc.player, hit)) {
                    NetworkManager.sendToServer(new SubmitTaskMessage(task.id));
                }
                continue;
            }
            if (task.observe(mc.player, hit) && selfTeamData.canStartTasks(task.getQuest())) {
                currentlyObserving = task;
                break;
            }
        }

        if (currentlyObserving != null) {
            if (!mc.isPaused()) {
                currentlyObservingTicks++;
            }

            // the server verifies observation itself, by sampling when it receives these packets
            boolean done = currentlyObservingTicks >= currentlyObserving.getTimer();
            if (!mc.isPaused() && (done || currentlyObservingTicks % SAMPLE_INTERVAL == 1L)) {
                NetworkManager.sendToServer(new SubmitTaskMessage(currentlyObserving.id));
            }

            if (done) {
                selfTeamData.addProgress(currentlyObserving, 1L);
                // the server's completion message takes a moment to arrive; don't restart observation meanwhile
                awaitingConfirmation.put(currentlyObserving.id, mc.level.getGameTime() + CONFIRM_TIMEOUT);
                currentlyObserving = null;
                currentlyObservingTicks = 0L;
            }
        } else {
            currentlyObservingTicks = 0L;
        }
    }

    private boolean isAwaitingConfirmation(ObservationTask task, long now) {
        long deadline = awaitingConfirmation.get(task.id);
        if (deadline == 0L) {
            return false;
        }
        if (now > deadline) {
            awaitingConfirmation.remove(task.id);
            return false;
        }
        return true;
    }

    public void render(Minecraft mc, GuiGraphics graphics, DeltaTracker tickDelta) {
        if (currentlyObserving == null) {
            return;
        }

        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        MutableComponent txt = currentlyObserving.getMutableTitle().withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE);
        int txtWidth = mc.font.width(txt);
        int boxWidth = Math.max(txtWidth, 100);

        Color4I.DARK_GRAY.withAlpha(130).draw(graphics, cx - boxWidth / 2 - 3, cy - 63, boxWidth + 6, 29);
        GuiHelper.drawHollowRect(graphics, cx - boxWidth / 2 - 3, cy - 63, boxWidth + 6, 29, Color4I.DARK_GRAY, false);

        graphics.drawString(mc.font, txt, cx - txtWidth / 2, cy - 60, 0xFFFFFF);
        double completed = (currentlyObservingTicks + tickDelta.getGameTimeDeltaPartialTick(false)) / (double) currentlyObserving.getTimer();

        GuiHelper.drawHollowRect(graphics, cx - boxWidth / 2, cy - 49, boxWidth, 12, Color4I.DARK_GRAY, false);
        Color4I.LIGHT_BLUE.withAlpha(130).draw(graphics, cx - boxWidth / 2 + 1, cy - 48, (int) ((boxWidth - 2D) * completed), 10);

        String pctTxt = (currentlyObservingTicks * 100L / currentlyObserving.getTimer()) + "%";
        graphics.drawString(mc.font, pctTxt, cx - mc.font.width(pctTxt) / 2, cy - 47, 0xFFFFFF);
    }
}
