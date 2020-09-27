package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		return FTBQuestsRewards.ADVANCEMENT;
	}

	@Override
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putString("advancement", advancement);
		nbt.putString("criterion", criterion);
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		advancement = nbt.getString("advancement");
		criterion = nbt.getString("criterion");
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		buffer.writeString(advancement);
		buffer.writeString(criterion);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		advancement = buffer.readString();
		criterion = buffer.readString();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("advancement", advancement, v -> advancement = v, "").setNameKey("ftbquests.reward.ftbquests.advancement");
		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
	public void claim(ServerPlayerEntity player, boolean notify)
	{
		Advancement a = player.server.getAdvancementManager().getAdvancement(new ResourceLocation(advancement));

		if (a != null)
		{
			if (criterion.isEmpty())
			{
				for (String s : a.getCriteria().keySet())
				{
					player.getAdvancements().grantCriterion(a, s);
				}
			}
			else
			{
				player.getAdvancements().grantCriterion(a, criterion);
			}
		}
	}

	@Override
	public IFormattableTextComponent getAltTitle()
	{
		Advancement a = Minecraft.getInstance().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(advancement));

		if (a != null && a.getDisplay() != null)
		{
			return new TranslationTextComponent("ftbquests.reward.ftbquests.advancement").appendString(": ").append(a.getDisplay().getTitle().deepCopy().mergeStyle(TextFormatting.YELLOW));
		}

		return super.getAltTitle();
	}

	@Override
	public Icon getAltIcon()
	{
		Advancement a = Minecraft.getInstance().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(new ResourceLocation(advancement));

		if (a != null && a.getDisplay() != null)
		{
			return ItemIcon.getItemIcon(a.getDisplay().getIcon());
		}

		return super.getAltIcon();
	}
}