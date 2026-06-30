package dev.ftb.mods.ftbquests.client.gui;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientConfig;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.SendChangeDescPacket;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class QuestFileChangelog {
    private static final int MAX_SIZE = 100;

    private final Deque<ChangeRecord> changes = new ArrayDeque<>();
    private List<Component> undoDesc = List.of();
    private List<Component> redoDesc = List.of();

    public void addEntry(SendChangeDescPacket message) {
        message.desc().forEach(component -> changes.push(new ChangeRecord(Util.getEpochMillis(), message.changeType(), component)));
        while (changes.size() > MAX_SIZE) {
            changes.removeLast();
        }

        undoDesc = message.undoDesc();
        redoDesc = message.redoDesc();
    }

    public List<Component> getUndoDesc() {
        return undoDesc;
    }

    public List<Component> getRedoDesc() {
        return redoDesc;
    }

    public Collection<ChangeRecord> getRecent(int max) {
        return changes.stream().limit(max).toList();
    }

    public void draw(GuiGraphics graphics, QuestScreen questScreen) {
        boolean forceShow = FTBQuestsClientConfig.CHANGELOG_ALWAYS_SHOW.get();
        float alpha = forceShow ? 1f : getDisplayAlpha();
        if (alpha > 0f) {
            float fontScale = FTBQuestsClientConfig.CHANGELOG_FONT_SCALE.get().floatValue();
            long displayTime = (long) (FTBQuestsClientConfig.CHANGELOG_SHOW_TIME.get() * 1000L);

            var recent = getRecent(FTBQuestsClientConfig.CHANGELOG_MAX_LINES.get());
            if (recent.isEmpty()) {
                return;
            }

            List<Pair<ChangeRecord, Component>> lines = recent.stream().map(rec -> Pair.of(rec, rec.format())).toList();

            Theme theme = questScreen.getGui().getTheme();
            int lh = Math.round(theme.getFontHeight() * fontScale);
            int w = (int) (lines.stream()
                    .map(p -> theme.getStringWidth(p.getSecond()))
                    .max(Integer::compareTo)
                    .orElse(100) * fontScale
            );
            int x = questScreen.getChapterPanelRight();
            int y = questScreen.questPanel.height - 10 - lh;

            graphics.pose().translate(0, 0, QuestScreen.Z_LEVEL);
            Color4I.BLACK.withAlpha((int) (100 * alpha)).draw(graphics, x, y - lh * (recent.size() - 1) - 2, w + 4, lh * recent.size() + 4);

            boolean first = true;
            for (var entry : lines) {
                float lineAlpha = Util.getEpochMillis() - entry.getFirst().timestamp() < displayTime || first ? 1f : 0.5f;
                entry.getFirst().drawAt(graphics, entry.getSecond(), theme.getFont(), x + 2, y, fontScale, Math.max(0.1f, alpha * lineAlpha));
                y -= lh;
                first = false;
            }

            graphics.pose().translate(0, 0, QuestScreen.Z_LEVEL);
        }
    }

    private float getDisplayAlpha() {
        ChangeRecord peek = changes.peek();
        if (peek == null) {
            return 0f;
        }
        long displayTime = (long) (FTBQuestsClientConfig.CHANGELOG_SHOW_TIME.get() * 1000L);
        long delta = Util.getEpochMillis() - peek.timestamp();
        if (delta < displayTime) {
            return 1f;
        } else if (delta > displayTime && delta < displayTime + 1000L) {
            return 1f - ((delta - displayTime) / 1000f);
        } else {
            return 0f;
        }
    }

    public record ChangeRecord(long timestamp, ChangeType changeType, Component text) {
        public Component format() {
            return Component.empty().append(formatTime()).append(" | ").append(changeType.description()).append(text);
        }

        public void drawAt(GuiGraphics graphics, Component toDraw, Font font, int x, int y, float scale, float alpha) {
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            if (scale != 1f) {
                graphics.pose().scale(scale, scale, scale);
            }
            int col = 0x00FFFFFF | ((int) (alpha * 255) << 24);
            graphics.drawString(font, toDraw, 0, 0, col, true);
            graphics.pose().popPose();
        }

        private MutableComponent formatTime() {
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            return Component.literal(df.format(new Date(timestamp))).withStyle(ChatFormatting.YELLOW);
        }
    }
}
