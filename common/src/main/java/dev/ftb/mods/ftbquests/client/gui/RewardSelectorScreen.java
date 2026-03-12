package dev.ftb.mods.ftbquests.client.gui;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractGroupedButtonListScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.RewardButton;
import dev.ftb.mods.ftbquests.net.ClaimAllRewardsMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public class RewardSelectorScreen extends AbstractGroupedButtonListScreen<Chapter, Quest> {
    private final TeamData selfData;
    private final QuestScreen questScreen;
    private int widestQuestName;
    private int maxRewardCount;
    private int totalRewards;
    private int excludedRewards;
    private final long screenOpenedAt;

    public RewardSelectorScreen(TeamData selfData, QuestScreen questScreen) {
        super(Component.translatable("ftbquests.gui.reward_selector"));

        this.selfData = selfData;
        this.questScreen = questScreen;

        screenOpenedAt = Util.getMillis();

        showBottomPanel(true);
    }

    @Override
    public void alignWidgets() {
        super.alignWidgets();

        mainPanel.getWidgets().forEach(widget -> {
            if (widget instanceof RowPanel row) {
                row.alignWidgets();
            }
        });
    }

    @Override
    protected List<GroupData<Chapter, Quest>> getGroups() {
        List<GroupData<Chapter,Quest>> groups = new ArrayList<>();

        Player player = Minecraft.getInstance().player;
        widestQuestName = maxRewardCount = excludedRewards = totalRewards = 0;

        if (player != null && ClientQuestFile.exists()) {
            TeamData data = ClientQuestFile.INSTANCE.selfTeamData;
            ClientQuestFile.INSTANCE.forAllChapters(chapter -> {
                List<Quest> quests = chapter.getQuests().stream()
                        .filter(quest -> quest.hasUnclaimedRewardsRaw(data, player.getUUID()))
                        .toList();
                if (!quests.isEmpty()) {
                    groups.add(new GroupData<>(chapter, false, chapter.getTitle(), quests));
                    quests.forEach(quest -> {
                        int unclaimed = 0;
                        for (var reward : quest.getRewards()) {
                            if (!data.isRewardClaimed(player.getUUID(), reward)) {
                                totalRewards++;
                                unclaimed++;
                            }
                            if (reward.getExcludeFromClaimAll()) {
                                excludedRewards++;
                            }
                        }
                        int width = Mth.clamp(getTheme().getStringWidth(quest.getTitle()), 100, 250);
                        widestQuestName = Math.max(widestQuestName, width);
                        maxRewardCount = Math.max(maxRewardCount, unclaimed);
                    });
                }
            });
        }

        return groups;
    }

    @Override
    protected AbstractGroupedButtonListScreen<Chapter, Quest>.RowPanel createRowPanel(Panel panel, Quest quest) {
        return new RowPanel(panel, quest);
    }

    @Override
    protected Panel createBottomPanel() {
        return new CustomBottomPanel();
    }

    @Override
    public boolean onInit() {
        setSize(Mth.clamp(widestQuestName + maxRewardCount * 20 + 50, 220, getWindow().getGuiScaledWidth() * 4 / 5),
                getWindow().getGuiScaledHeight() * 4 / 5);
        return true;
    }

    private void doClaimAll() {
        NetworkManager.sendToServer(ClaimAllRewardsMessage.INSTANCE);
    }

    @Override
    public Theme getTheme() {
        // use the quests theme rather than ftblib default, since this is a player-facing screen
        return FTBQuestsTheme.INSTANCE;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.drawBackground(graphics, theme, x, y, w, h);
        theme.drawPanelBackground(graphics, x, y, w, h);
    }

    @Override
    protected void doCancel() {
        closeGui(true);
    }

    private class RowPanel extends AbstractGroupedButtonListScreen<Chapter,Quest>.RowPanel {
        public RowPanel(Panel parent, Quest value) {
            super(parent, value);
        }

        @Override
        public void addWidgets() {
            add(new TextField(this).setText(value.getTitle()).setMaxWidth(250).addFlags(Theme.SHADOW));
            List<RewardButton> buttons = value.getRewards().stream()
                    .filter(this::shouldShowButton)
                    .map(reward -> new RewardButton(this, reward, questScreen))
                    .toList();
            if (buttons.isEmpty()) {
                add(new SimpleButton(this, Component.empty(), Icons.ACCEPT, (btn, mb) -> {}) {
                    @Override
                    public void addMouseOverText(TooltipList list) {
                        list.add(Component.translatable("ftbquests.gui.all_rewards_claimed"));
                    }
                });
            } else {
                addAll(buttons);
            }
        }

        private boolean shouldShowButton(Reward reward) {
            return switch (selfData.getClaimType(Minecraft.getInstance().player.getUUID(), reward)) {
                case CANT_CLAIM -> false;
                // do show any claimed rewards that were only claimed since this screen was first opened
                case CLAIMED -> selfData.getRewardClaimTime(Minecraft.getInstance().player.getUUID(), reward)
                        .map(date -> date.getTime() > screenOpenedAt).orElse(false);
                case CAN_CLAIM -> true;
            };
        }

        @Override
        public void alignWidgets() {
            int xPos = width - 22 - (scrollBar.shouldDraw() ? scrollBar.width : 0);
            int tallest = 0;
            for (Widget w : widgets) {
                if (w instanceof TextField) {
                    w.setPosAndSize(18, (height - w.height) / 2, widestQuestName, w.height);
                } else if (w instanceof Button) {
                    w.setPosAndSize(xPos, (height - w.height) / 2, 16, 16);
                    xPos -= 22;
                }
                tallest = Math.max(tallest, w.height);
            }
            setHeight(Math.max(height, tallest + 6));
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.drawBackground(graphics, theme, x, y, w, h);

            value.getIcon().drawStatic(graphics, x + 2, y + (height - 12) / 2, 12, 12);
            graphics.hLine(x, x + w, y + h, 0x40808080);
        }
    }

    private class CustomBottomPanel extends Panel {
        private Button buttonClaimAll;
        private final Button buttonClose;

        public CustomBottomPanel() {
            super(RewardSelectorScreen.this);

            buttonClose = SimpleTextButton.create(this, Component.translatable("gui.close"),
                    Icons.CANCEL, mb -> doCancel(), hotkeyTooltip("ESC"));
        }

        @Override
        public void addWidgets() {
            Component[] tooltip = excludedRewards > 0 ?
                    new Component[] { Component.translatable("ftbquests.gui.claim_all_exclusion", excludedRewards) } :
                    new Component[0];
            buttonClaimAll = SimpleTextButton.create(this, Component.translatable("ftbquests.reward.claim_all"),
                    Icons.MONEY_BAG, mb -> doClaimAll(), tooltip);

            add(buttonClose);
            if (totalRewards - excludedRewards > 0) {
                add(buttonClaimAll);
            }
        }

        @Override
        public void alignWidgets() {
            buttonClose.setPos(width - buttonClose.width - 5, 3);
            buttonClaimAll.setPos(buttonClose.posX - buttonClaimAll.width - 5, 3);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawPanelBackground(graphics, x, y, w, h);
            Color4I.BLACK.withAlpha(80).draw(graphics, x, y + h - 1, w, 1);
        }
    }
}
