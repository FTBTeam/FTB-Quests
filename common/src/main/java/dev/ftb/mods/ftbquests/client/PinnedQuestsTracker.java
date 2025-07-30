package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum PinnedQuestsTracker {
    INSTANCE;

    private static final int INTERVAL = 30;
    public static final int VERTICAL_SPACING = 3;

    private final List<FormattedCharSequence> pinnedQuestText = new ArrayList<>();
    private int ticker = 0;
    private boolean showChapterTitle;

    public void tick(ClientQuestFile file) {
        if (++ticker >= INTERVAL) {
            collectPinnedQuests(file);
            ticker = 0;
        }
    }

    public void refresh() {
        ticker = INTERVAL;
    }

    private void collectPinnedQuests(ClientQuestFile file) {
        TeamData data = file.selfTeamData;

        showChapterTitle = false;
        List<Quest> pinnedQuests = new ArrayList<>();
        LongSet pinnedIds = data.getPinnedQuestIds(FTBQuestsClient.getClientPlayer());
        if (!pinnedIds.isEmpty()) {
            if (pinnedIds.contains(TeamData.AUTO_PIN_ID)) {
                // special auto-pin value: collect all quests which can be done now
                boolean wholeBook = FTBQuestsClientConfig.AUTO_PIN_FOLLOWS.get() == AutoPinTarget.QUEST_BOOK;
                file.forAllQuests(quest -> {
                    if (!data.isCompleted(quest) && data.canStartTasks(quest) && (wholeBook || file.isChapterSelected(quest.getChapter()))) {
                        pinnedQuests.add(quest);
                    }
                });
                showChapterTitle = !wholeBook;
            } else {
                pinnedIds.longStream()
                        .mapToObj(file::getQuest)
                        .filter(Objects::nonNull)
                        .forEach(pinnedQuests::add);
            }
        }

        rebuildPinnedText(pinnedQuests, Minecraft.getInstance(), data);
    }

    private void rebuildPinnedText(List<Quest> pinnedQuests, Minecraft mc, TeamData data) {
        pinnedQuestText.clear();

        for (int i = 0; i < pinnedQuests.size(); i++) {
            Quest quest = pinnedQuests.get(i);

            if (i > 0) pinnedQuestText.add(FormattedCharSequence.EMPTY);  // separator line between quests

            pinnedQuestText.addAll(mc.font.split(FormattedText.composite(
                    mc.font.getSplitter().headByWidth(quest.getTitle(), 160, Style.EMPTY.withBold(true)),
                    Component.literal(" ")
                            .withStyle(ChatFormatting.DARK_AQUA)
                            .append(data.getRelativeProgress(quest) + "%")
            ), 500));

            for (Task task : quest.getTasks()) {
                if (!data.isCompleted(task)) {
                    pinnedQuestText.addAll(mc.font.split(FormattedText.composite(
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

    void render(Minecraft mc, GuiGraphics graphics) {
        if (pinnedQuestText.isEmpty()) {
            return;
        }

        MutableComponent title = Component.translatable("ftbquests.pinned");
        if (showChapterTitle) {
            ClientQuestFile.INSTANCE.getQuestScreen().flatMap(QuestScreen::getSelectedChapter)
                    .ifPresent(chapter -> title.append(": ").append(chapter.getTitle()));
        }

        int titleWidth = mc.font.width(title);
        int width = titleWidth + 5;
        int height = mc.font.lineHeight + 10;
        for (FormattedCharSequence s : pinnedQuestText) {
            width = Math.max(width, (int) mc.font.getSplitter().stringWidth(s));
            height += s == FormattedCharSequence.EMPTY ? VERTICAL_SPACING : mc.font.lineHeight;
        }
        width += 8;

        float scale = FTBQuestsClientConfig.PINNED_QUESTS_SCALE.get().floatValue();
        int insetX = FTBQuestsClientConfig.PINNED_QUESTS_INSET_X.get();
        int insetY = FTBQuestsClientConfig.PINNED_QUESTS_INSET_Y.get();
        var pos = FTBQuestsClientConfig.PINNED_QUESTS_POS.get().getPanelPos(
                mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
                (int) (width * scale), (int) (height * scale),
                insetX, insetY
        );

        graphics.pose().pushPose();
        graphics.pose().translate(pos.x(), pos.y(), 100);
        graphics.pose().scale(scale, scale, 1F);

        GuiHelper.drawHollowRect(graphics, 0, 0, width, height, Color4I.BLACK.withAlpha(100), false);
        Color4I.BLACK.withAlpha(100).draw(graphics, 0, 0, width, height);
        Color4I.GRAY.withAlpha(50).draw(graphics, 1, 1, width - 2, mc.font.lineHeight + 4);
        Color4I.BLACK.draw(graphics, 0, mc.font.lineHeight + 4, width, 1);

        graphics.drawString(mc.font, title, (width - titleWidth) / 2, 4, 0xFFFFFF00);
        int yPos = mc.font.lineHeight + 8;
        for (FormattedCharSequence fcs : pinnedQuestText) {
            if (fcs == FormattedCharSequence.EMPTY) {
                yPos += VERTICAL_SPACING;
            } else {
                graphics.drawString(mc.font, fcs, 4, yPos, 0xFFFFFFFF);
                yPos += mc.font.lineHeight;
            }
        }

        graphics.pose().popPose();
    }
}
