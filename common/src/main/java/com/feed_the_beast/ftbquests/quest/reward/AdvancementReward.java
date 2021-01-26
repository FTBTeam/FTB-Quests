package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @author LatvianModder
 */
public class AdvancementReward extends Reward
{
	public String advancement;
	public String criterion;

	public AdvancementReward(Quest quest)
	{
		super(quest);
		advancement = "";
		criterion = "";
	}

	@Override
	public RewardType getType()
	{
		return FTBQuestsRewards.ADVANCEMENT.get();
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		nbt.putString("advancement", advancement);
		nbt.putString("criterion", criterion);
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		advancement = nbt.getString("advancement");
		criterion = nbt.getString("criterion");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		buffer.writeUtf(advancement);
		buffer.writeUtf(criterion);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		advancement = buffer.readUtf();
		criterion = buffer.readUtf();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("advancement", advancement, v -> advancement = v, "").setNameKey("ftbquests.reward.ftbquests.advancement");
		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify)
	{
		Advancement a = player.server.getAdvancements().getAdvancement(new ResourceLocation(advancement));

		if (a != null)
		{
			if (criterion.isEmpty())
			{
				for (String s : a.getCriteria().keySet())
				{
					player.getAdvancements().award(a, s);
				}
			}
			else
			{
				player.getAdvancements().award(a, criterion);
			}
		}
	}

	@Override
	public MutableComponent getAltTitle()
	{
		Advancement a = Minecraft.getInstance().player.connection.getAdvancements().getAdvancements().get(new ResourceLocation(advancement));

		if (a != null && a.getDisplay() != null)
		{
			return new TranslatableComponent("ftbquests.reward.ftbquests.advancement").append(": ").append(a.getDisplay().getTitle().copy().withStyle(ChatFormatting.YELLOW));
		}

		return super.getAltTitle();
	}

	@Override
	public Icon getAltIcon()
	{
		Advancement a = Minecraft.getInstance().player.connection.getAdvancements().getAdvancements().get(new ResourceLocation(advancement));

		if (a != null && a.getDisplay() != null)
		{
			return ItemIcon.getItemIcon(a.getDisplay().getIcon());
		}

		return super.getAltIcon();
	}
}