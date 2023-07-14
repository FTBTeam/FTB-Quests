package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class AdvancementReward extends Reward {
	public ResourceLocation advancement;
	public String criterion;

	public AdvancementReward(Quest quest) {
		super(quest);
		advancement = new ResourceLocation("minecraft:story/root");
		criterion = "";
	}

	@Override
	public RewardType getType() {
		return RewardTypes.ADVANCEMENT;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("advancement", advancement.toString());
		nbt.putString("criterion", criterion);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		advancement = new ResourceLocation(nbt.getString("advancement"));
		criterion = nbt.getString("criterion");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeResourceLocation(advancement);
		buffer.writeUtf(criterion, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		advancement = buffer.readResourceLocation();
		criterion = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		if (KnownServerRegistries.client != null && !KnownServerRegistries.client.advancements.isEmpty()) {
			config.addEnum("advancement", advancement, v -> advancement = v, NameMap.of(KnownServerRegistries.client.advancements.keySet().iterator().next(), KnownServerRegistries.client.advancements.keySet().toArray(new ResourceLocation[0]))
					.icon(resourceLocation -> ItemIcon.getItemIcon(KnownServerRegistries.client.advancements.get(resourceLocation).icon))
					.name(resourceLocation -> KnownServerRegistries.client.advancements.get(resourceLocation).name)
					.create()).setNameKey("ftbquests.reward.ftbquests.advancement");
		} else {
			config.addString("advancement", advancement.toString(), v -> advancement = new ResourceLocation(v), "minecraft:story/root").setNameKey("ftbquests.reward.ftbquests.advancement");
		}

		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		Advancement a = player.server.getAdvancements().getAdvancement(advancement);

		if (a != null) {
			if (criterion.isEmpty()) {
				for (String s : a.getCriteria().keySet()) {
					player.getAdvancements().award(a, s);
				}
			} else {
				player.getAdvancements().award(a, criterion);
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Component getAltTitle() {
		KnownServerRegistries.AdvancementInfo info = KnownServerRegistries.client == null ? null : KnownServerRegistries.client.advancements.get(advancement);

		if (info != null && info.name.getContents() != ComponentContents.EMPTY) {
			return Component.translatable("ftbquests.reward.ftbquests.advancement").append(": ").append(info.name.copy().withStyle(ChatFormatting.YELLOW));
		}

		return super.getAltTitle();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		KnownServerRegistries.AdvancementInfo info = KnownServerRegistries.client == null ? null : KnownServerRegistries.client.advancements.get(advancement);

		if (info != null && !info.icon.isEmpty()) {
			return ItemIcon.getItemIcon(info.icon);
		}

		return super.getAltIcon();
	}
}
