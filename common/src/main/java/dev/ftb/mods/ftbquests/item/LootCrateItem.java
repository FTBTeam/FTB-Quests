package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import me.shedaniel.architectury.utils.GameInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class LootCrateItem extends Item {
	public LootCrateItem() {
		super(new Properties().tab(FTBQuests.ITEM_GROUP));
	}

	@Nullable
	public static LootCrate getCrate(@Nullable LevelAccessor world, ItemStack stack) {
		if (stack.hasTag() && stack.getItem() instanceof LootCrateItem) {
			QuestFile file = world == null ? FTBQuests.PROXY.getQuestFile(GameInstance.getServer() == null || Thread.currentThread() != GameInstance.getServer().getRunningThread()) : FTBQuests.PROXY.getQuestFile(world.isClientSide());
			return file == null ? null : file.getLootCrate(stack.getTag().getString("type"));
		}

		return null;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		LootCrate crate = getCrate(world, stack);

		if (crate == null) {
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}

		int size = player.isCrouching() ? stack.getCount() : 1;

		if (!world.isClientSide) {
			int totalWeight = crate.table.getTotalWeight(true);

			if (totalWeight > 0) {
				for (int j = 0; j < size * crate.table.lootSize; j++) {
					int number = player.level.random.nextInt(totalWeight) + 1;
					int currentWeight = crate.table.emptyWeight;

					if (currentWeight < number) {
						for (WeightedReward reward : crate.table.rewards) {
							currentWeight += reward.weight;

							if (currentWeight >= number) {
								reward.reward.claim((ServerPlayer) player, true);
								break;
							}
						}
					}
				}
			}

			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F + world.random.nextFloat() * 0.4F);
		} else {
			new RewardNotificationsScreen().openGui();

			for (int i = 0; i < 5; i++) {
				Vec3 vec3d = new Vec3(((double) world.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
				vec3d = vec3d.xRot(-player.xRot * 0.017453292F);
				vec3d = vec3d.yRot(-player.yRot * 0.017453292F);
				double d0 = (double) (-world.random.nextFloat()) * 0.6D - 0.3D;
				Vec3 vec3d1 = new Vec3(((double) world.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
				vec3d1 = vec3d1.xRot(-player.xRot * 0.017453292F);
				vec3d1 = vec3d1.yRot(-player.yRot * 0.017453292F);
				vec3d1 = vec3d1.add(player.getX(), player.getY() + (double) player.getEyeHeight(), player.getZ());
				world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
			}
		}

		stack.shrink(size);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		LootCrate crate = getCrate(null, stack);
		return crate != null && crate.glow;
	}

	@Override
	public Component getName(ItemStack stack) {
		LootCrate crate = getCrate(null, stack);
		return crate != null && !crate.itemName.isEmpty() ? new TextComponent(crate.itemName) : super.getName(stack);
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.UNCOMMON;
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
		if (allowdedIn(tab)) {
			QuestFile file = FTBQuests.PROXY.getQuestFile(GameInstance.getServer() == null || Thread.currentThread() != GameInstance.getServer().getRunningThread());

			if (file != null) {
				for (RewardTable table : file.rewardTables) {
					if (table.lootCrate != null) {
						items.add(table.lootCrate.createStack());
					}
				}
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(new TranslatableComponent("item.ftbquests.lootcrate.tooltip_1").withStyle(ChatFormatting.GRAY));
		tooltip.add(new TranslatableComponent("item.ftbquests.lootcrate.tooltip_2").withStyle(ChatFormatting.GRAY));

		if (world == null || !ClientQuestFile.exists()) {
			return;
		}

		LootCrate crate = getCrate(world, stack);

		if (crate != null) {
			if (crate.itemName.isEmpty()) {
				tooltip.add(TextComponent.EMPTY);
				tooltip.add(crate.table.getMutableTitle().withStyle(ChatFormatting.GRAY));
			}
		} else if (stack.hasTag() && stack.getTag().contains("type")) {
			tooltip.add(TextComponent.EMPTY);
			tooltip.add(new TextComponent(stack.getTag().getString("type")).withStyle(ChatFormatting.GRAY));
		}
	}
}