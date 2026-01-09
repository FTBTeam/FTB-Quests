package dev.ftb.mods.ftbquests.quest.reward;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementReward extends Reward {
	private Identifier advancement;
	private String criterion;

	public AdvancementReward(long id, Quest quest) {
		super(id, quest);
		advancement = Identifier.withDefaultNamespace("story/root");
		criterion = "";
	}

	@Override
	public RewardType getType() {
		return RewardTypes.ADVANCEMENT;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);
		nbt.store("advancement", Identifier.CODEC, advancement);
		nbt.putString("criterion", criterion);
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);
		advancement = nbt.read("advancement", Identifier.CODEC).orElseThrow();
		criterion = nbt.getString("criterion").orElseThrow();
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeIdentifier(advancement);
		buffer.writeUtf(criterion, Short.MAX_VALUE);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);
		advancement = buffer.readIdentifier();
		criterion = buffer.readUtf(Short.MAX_VALUE);
	}

	@Override
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		if (KnownServerRegistries.client != null && !KnownServerRegistries.client.advancements().isEmpty()) {
			var advancements = KnownServerRegistries.client.advancements();
			config.addEnum("advancement", advancement, v -> advancement = v,
					NameMap.of(advancements.keySet().iterator().next(), advancements.keySet().toArray(new Identifier[0]))
							.icon(resourceLocation -> ItemIcon.ofItemStack(advancements.get(resourceLocation).icon()))
							.name(resourceLocation -> advancements.get(resourceLocation).name())
							.create()).setNameKey("ftbquests.reward.ftbquests.advancement");
		} else {
			config.addString("advancement", advancement.toString(), v -> advancement = Identifier.tryParse(v), "minecraft:story/root").setNameKey("ftbquests.reward.ftbquests.advancement");
		}

		config.addString("criterion", criterion, v -> criterion = v, "");
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		AdvancementHolder advancementHolder = player.level().getServer().getAdvancements().get(advancement);

		if (advancementHolder != null) {
			if (criterion.isEmpty()) {
				for (String s : advancementHolder.value().criteria().keySet()) {
					player.getAdvancements().award(advancementHolder, s);
				}
			} else {
				player.getAdvancements().award(advancementHolder, criterion);
			}
		}
	}

	@Override
	public Component getAltTitle() {
		KnownServerRegistries.AdvancementInfo info = KnownServerRegistries.client == null ?
				null :
				KnownServerRegistries.client.advancements().get(advancement);

		if (info != null && !TextUtils.isComponentEmpty(info.name())) {
			return Component.translatable("ftbquests.reward.ftbquests.advancement").append(": ").append(info.name().copy().withStyle(ChatFormatting.YELLOW));
		}

		return super.getAltTitle();
	}

	@Override
	public Icon<?> getAltIcon() {
		KnownServerRegistries.AdvancementInfo info = KnownServerRegistries.client == null ?
				null :
				KnownServerRegistries.client.advancements().get(advancement);

		if (info != null && !info.icon().isEmpty()) {
			return ItemIcon.ofItemStack(info.icon());
		}

		return super.getAltIcon();
	}
}
