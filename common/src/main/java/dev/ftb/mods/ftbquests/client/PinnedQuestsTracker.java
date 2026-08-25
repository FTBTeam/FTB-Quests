package dev.ftb.mods.ftbquests.client;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum PinnedQuestsTracker {
    INSTANCE;

    private static final int INTERVAL = 30;
    public static final int VERTICAL_SPACING = 3;

    private int ticker = 0;
    private boolean showChapterTitle;
    private boolean refreshNeeded = true;
    @Nullable
    private PinnedQuestsTracker.RenderData renderData;

    public void tick(ClientQuestFile file) {
        if (ticker < INTERVAL) {
            ticker++;
        }

        if (refreshNeeded && ticker >= INTERVAL) {
            collectPinnedQuests(file);
            refreshNeeded = false;
            ticker = 0;
        }
    }

    public void refresh() {
        refreshNeeded = true;
    }

    private void collectPinnedQuests(ClientQuestFile file) {
        if (FTBQuestsClientConfig.PINNED_VISIBILITY.get() == PinnedTrackerVisibility.HIDDEN) {
            renderData = null;
            return;
        }

        TeamData data = FTBQuestsClient.getClientPlayerData();

        showChapterTitle = false;
        List<Quest> pinnedQuests = new ArrayList<>();
        LongSet pinnedIds = data.getPinnedQuestIds(ClientUtils.getClientPlayer());
        if (!pinnedIds.isEmpty()) {
            if (pinnedIds.contains(TeamData.AUTO_PIN_ID)) {
                // special auto-pin value: collect all quests which can be done now
                boolean wholeBook = FTBQuestsClientConfig.AUTO_PIN_FOLLOWS.get() == AutoPinTarget.QUEST_BOOK;
                file.forAllQuests(quest -> {
                    if (!data.isCompleted(quest)
                            && quest.isVisible(data)
                            && data.canStartTasks(quest, !FTBQuestsClientConfig.PINNED_EXCLUDE_FLEXIBLE.get())
                            && (wholeBook || file.isChapterSelected(quest.getChapter())))
                    {
                        pinnedQuests.add(quest);
                    }
                });
                showChapterTitle = !wholeBook;
            } else {
                for (long id : pinnedIds) {
                    var quest = file.getQuest(id);
                    if (quest != null) {
                        pinnedQuests.add(quest);
                    }
                }
            }
        }

        rebuildPinnedText(pinnedQuests, Minecraft.getInstance(), data);
    }

    private void rebuildPinnedText(List<Quest> pinnedQuests, Minecraft mc, TeamData data) {
        if (FTBQuestsClientConfig.PINNED_VISIBILITY.get() == PinnedTrackerVisibility.HIDDEN) {
            return;
        }

        ImmutableList.Builder<FormattedCharSequence> linesBuilder = ImmutableList.builder();
        for (int i = 0; i < pinnedQuests.size(); i++) {
            Quest quest = pinnedQuests.get(i);

            if (i > 0) linesBuilder.add(FormattedCharSequence.EMPTY);  // separator line between quests

            linesBuilder.addAll(mc.font.split(FormattedText.composite(
                    mc.font.getSplitter().headByWidth(quest.getTitle(), 160, Style.EMPTY.withBold(true)),
                    Component.literal(" ")
                            .withStyle(ChatFormatting.DARK_AQUA)
                            .append(data.getRelativeProgress(quest) + "%")
            ), 500));

            if (FTBQuestsClientConfig.PINNED_VISIBILITY.get() == PinnedTrackerVisibility.ALL) {
                for (Task task : quest.getTasks()) {
                    if (!data.isCompleted(task)) {
                        linesBuilder.addAll(mc.font.split(FormattedText.composite(
                                Component.literal("└").withStyle(ChatFormatting.GRAY),
                                mc.font.getSplitter().headByWidth(task.getMutableTitle().withStyle(ChatFormatting.GRAY), 160, Style.EMPTY.applyFormat(ChatFormatting.GRAY)),
                                Component.literal(" ")
                                        .withStyle(ChatFormatting.GREEN)
                                        .append(task.formatProgress(data, data.getProgress(task)))
                                        .append("/")
                                        .append(task.formatMaxProgress())
                        ), 500));
                    }
                }
            }
        }

        renderData = RenderData.create(linesBuilder.build(), showChapterTitle, mc.font);
    }

    void render(Minecraft mc, GuiGraphicsExtractor graphics) {
        if (renderData == null || renderData.pinnedQuestText().isEmpty()) {
            return;
        }

        int width = renderData.width;
        int height = renderData.height;


        float scale = FTBQuestsClientConfig.PINNED_QUESTS_SCALE.get().floatValue();
        int insetX = FTBQuestsClientConfig.PINNED_QUESTS_INSET_X.get();
        int insetY = FTBQuestsClientConfig.PINNED_QUESTS_INSET_Y.get();
        var pos = FTBQuestsClientConfig.PINNED_QUESTS_POS.get().getPanelPos(
                mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
                (int) (width * scale), (int) (height * scale),
                insetX, insetY
        );

        graphics.pose().pushMatrix();
        graphics.pose().translate(pos.x(), pos.y());
        graphics.pose().scale(scale, scale);

        GuiHelper.drawHollowRect(graphics, 0, 0, width, height, Color4I.BLACK.withAlpha(100), false);
        IconHelper.renderIcon(Color4I.BLACK.withAlpha(100), graphics, 0, 0, width, height);
        IconHelper.renderIcon(Color4I.GRAY.withAlpha(50), graphics, 1, 1, width - 2, mc.font.lineHeight + 4);
        IconHelper.renderIcon(Color4I.BLACK, graphics, 0, mc.font.lineHeight + 4, width, 1);

        graphics.text(mc.font, renderData.title, (width - renderData.titleWidth) / 2, 4, 0xFFFFFF00);
        int yPos = mc.font.lineHeight + 8;
        for (FormattedCharSequence fcs : renderData.pinnedQuestText) {
            if (fcs == FormattedCharSequence.EMPTY) {
                yPos += VERTICAL_SPACING;
            } else {
                graphics.text(mc.font, fcs, 4, yPos, 0xFFFFFFFF);
                yPos += mc.font.lineHeight;
            }
        }

        graphics.pose().popMatrix();
    }

    private record RenderData(
            Component title,
            List<FormattedCharSequence> pinnedQuestText,
            int titleWidth,
            int width,
            int height
    )
    {
        static RenderData create(List<FormattedCharSequence> lines, boolean showChapterTitle, Font font) {
            MutableComponent title = Component.translatable("ftbquests.pinned");
            if (showChapterTitle) {
                ClientQuestFile.getInstance().getQuestScreen().flatMap(QuestScreen::getSelectedChapter)
                        .ifPresent(chapter -> title.append(": ").append(chapter.getTitle()));
            }
            int titleWidth = font.width(title);
            int width = titleWidth + 5;
            int height = font.lineHeight + 10;
            for (FormattedCharSequence s : lines) {
                width = Math.max(width, (int) font.getSplitter().stringWidth(s));
                height += s == FormattedCharSequence.EMPTY ? VERTICAL_SPACING : font.lineHeight;
            }
            width += 8;

            return new RenderData(title, lines, titleWidth, width, height);
        }
    }
}
