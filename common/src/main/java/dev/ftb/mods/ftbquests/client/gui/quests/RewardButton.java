package dev.ftb.mods.ftbquests.client.gui.quests;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.ContextMenuBuilder;
import dev.ftb.mods.ftbquests.net.ReorderItemMessage;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.joml.Matrix3x2fStack;

public class RewardButton extends Button {
	private final QuestScreen questScreen;
	Reward reward;

	public RewardButton(Panel panel, Reward reward) {
		super(panel, reward.getTitle(), reward.getIcon());
		questScreen = (QuestScreen) panel.getGui();
		this.reward = reward;
		setSize(18, 18);
	}

	@Override
	public Component getTitle() {
		if (reward.isTeamReward()) {
			return super.getTitle().copy().withStyle(ChatFormatting.BLUE);
		}

		return super.getTitle();
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		questScreen.addInfoTooltip(list, reward);

		if (reward.addTitleInMouseOverText()) {
			if (reward instanceof ItemReward itemReward) {
				TooltipFlag.Default flag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
				itemReward.getItem().getTooltipLines(Item.TooltipContext.of(ClientUtils.getClientLevel()), ClientUtils.getClientPlayer(), flag)
						.forEach(list::add);
			} else {
				list.add(getTitle());
			}
		}

		if (reward.isTeamReward() || questScreen.file.selfTeamData.isRewardBlocked(reward)) {
			getIngredientUnderMouse().ifPresent(ingredient -> {
				if (ingredient.tooltip() && ingredient.ingredient() instanceof ItemStack stack && !stack.isEmpty()) {
					List<Component> list1 = new ArrayList<>();
					GuiHelper.addStackTooltip(stack, list1);
					list1.forEach(list::add);
				}
			});

			list.blankLine();
			reward.addMouseOverText(list);
			if (reward.isTeamReward()) {
				list.add(Component.translatable("ftbquests.reward.team_reward").withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE));
			} else if (questScreen.file.selfTeamData.isRewardBlocked(reward)) {
				list.add(Component.translatable("ftbquests.reward.this_blocked", questScreen.file.selfTeamData).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
			}
		} else {
			reward.addMouseOverText(list);
		}
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver()) {
			if (button.isRight() || getWidgetType() != WidgetType.DISABLED) {
				onClicked(button);
			}

			return true;
		}

		return false;
	}

	@Override
	public void onClicked(MouseButton button) {
		if (button.isLeft()) {
			if (reward.getQuestFile().canEdit() && Minecraft.getInstance().hasAltDown()) {
				reward.onEditButtonClicked(this);
			} else if (ClientQuestFile.exists()) {
				boolean canClick = questScreen.file.selfTeamData.getClaimType(ClientUtils.getClientPlayer().getUUID(), reward).canClaim();
				reward.onButtonClicked(this, canClick);
            }
		} else if (button.isRight() && ClientQuestFile.exists() && ClientQuestFile.getInstance().canEdit()) {
			playClickSound();

			ContextMenuBuilder builder = ContextMenuBuilder.create(reward, questScreen);

			builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.move_left"), Icons.LEFT,
					b -> NetworkManager.sendToServer(new ReorderItemMessage(reward.getId(), false))
			)));
			builder.insertAtTop(List.of(new ContextMenuItem(Component.translatable("ftbquests.gui.move_right"), Icons.RIGHT,
					b -> NetworkManager.sendToServer(new ReorderItemMessage(reward.getId(), true))
			)));

			builder.openContextMenu(getGui());
		}
	}

	@Override
	public Optional<PositionedIngredient> getIngredientUnderMouse() {
		return PositionedIngredient.of(reward.getIngredient(this), this);
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(graphics, theme, x, y, w, h);
		}
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		int bs = h >= 32 ? 32 : 16;
//		GuiHelper.setupDrawing();
		drawBackground(graphics, theme, x, y, w, h);
		drawIcon(graphics, theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);

		if (questScreen.file.selfTeamData == null) {
			return;
		} else if (questScreen.getContextMenu().isEmpty()) {
			//return;
		}

		Matrix3x2fStack poseStack = graphics.pose();

		poseStack.pushMatrix();
		poseStack.translate(0, 0);
//		RenderSystem.enableBlend();
		boolean completed = false;

		if (questScreen.file.selfTeamData.getClaimType(Minecraft.getInstance().player.getUUID(), reward).isClaimed()) {
			IconHelper.renderIcon(ThemeProperties.CHECK_ICON.get(), graphics, x + w - 9, y + 1, 8, 8);
			completed = true;
		} else if (questScreen.file.selfTeamData.isCompleted(reward.getQuest())) {
			IconHelper.renderIcon(ThemeProperties.ALERT_ICON.get(), graphics, x + w - 9, y + 1, 8, 8);
		}

		poseStack.popMatrix();

		if (!completed) {
			String s = reward.getButtonText();

			if (!s.isEmpty()) {
				poseStack.pushMatrix();
				poseStack.translate((float) (x + 19 - theme.getStringWidth(s) / 2D), y + 15);
				poseStack.scale(0.5F, 0.5F);
				theme.drawString(graphics, s, 0, 0, Color4I.WHITE, Theme.SHADOW);
				poseStack.popMatrix();
			}
		}
	}
}
