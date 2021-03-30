package dev.ftb.mods.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.net.MessageClaimAllRewards;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class ModpackButton extends TabButton {
	private final boolean unclaimedRewards;

	public ModpackButton(Panel panel) {
		super(panel, TextComponent.EMPTY, ClientQuestFile.INSTANCE.getIcon());
		title = questScreen.file.getTitle();
		unclaimedRewards = hasUnclaimedRewards(questScreen.file);
	}

	@Override
	public void onClicked(MouseButton button) {
		if (ClientQuestFile.exists() && unclaimedRewards) {
			playClickSound();
			new RewardNotificationsScreen().openGui();
			new MessageClaimAllRewards().sendToServer();
		}
	}

	private static boolean hasUnclaimedRewards(ClientQuestFile f) {
		for (ChapterGroup group : f.chapterGroups) {
			for (Chapter chapter : group.chapters) {
				for (Quest quest : chapter.quests) {
					if (f.self.isComplete(quest)) {
						for (Reward reward : quest.rewards) {
							if (!reward.getExcludeFromClaimAll() && !f.self.isRewardClaimed(reward.id)) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		super.addMouseOverText(list);

		if (unclaimedRewards) {
			list.blankLine();
			list.add(new TranslatableComponent("ftbquests.gui.collect_rewards").withStyle(ChatFormatting.GOLD));
		}
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		super.draw(matrixStack, theme, x, y, w, h);

		if (unclaimedRewards) {
			GuiHelper.setupDrawing();
			float s = w / 2F;//(int) (treeGui.getZoom() / 2 * quest.size);
			matrixStack.pushPose();
			matrixStack.translate(x + w - s, y, 200);
			matrixStack.scale(s, s, 1F);
			ThemeProperties.ALERT_ICON.get(questScreen.file).draw(matrixStack, 0, 0, 1, 1);
			matrixStack.popPose();
		}
	}
}