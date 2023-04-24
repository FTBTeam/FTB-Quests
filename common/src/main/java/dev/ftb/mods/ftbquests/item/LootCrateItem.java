package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author LatvianModder
 */
public class LootCrateItem extends Item {
	public LootCrateItem() {
		super(new Properties().tab(FTBQuests.ITEM_GROUP));
	}

	@Nullable
	public static LootCrate getCrate(ItemStack stack) {
		if (stack.hasTag() && stack.getItem() instanceof LootCrateItem) {
			return LootCrate.LOOT_CRATES.get(stack.getTag().getString("type"));
		}

		return null;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		LootCrate crate = getCrate(stack);

		if (crate == null) {
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}

		int size = player.isCrouching() ? stack.getCount() : 1;

		if (!world.isClientSide) {
			for (WeightedReward reward : crate.table.generateWeightedRandomRewards(player.getRandom(), crate.table.lootSize * size, true)) {
				reward.reward.claim((ServerPlayer) player, true);
			}

			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F + world.random.nextFloat() * 0.4F);
		} else {
			new RewardNotificationsScreen().openGui();

			for (int i = 0; i < 5; i++) {
				Vec3 vec3d = new Vec3(((double) world.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
				vec3d = vec3d.xRot(-player.getXRot() * 0.017453292F);
				vec3d = vec3d.yRot(-player.getYRot() * 0.017453292F);
				double d0 = (double) (-world.random.nextFloat()) * 0.6D - 0.3D;
				Vec3 vec3d1 = new Vec3(((double) world.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
				vec3d1 = vec3d1.xRot(-player.getXRot() * 0.017453292F);
				vec3d1 = vec3d1.yRot(-player.getYRot() * 0.017453292F);
				vec3d1 = vec3d1.add(player.getX(), player.getY() + (double) player.getEyeHeight(), player.getZ());
				world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
			}
		}

		stack.shrink(size);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		LootCrate crate = getCrate(stack);
		return crate != null && crate.glow;
	}

	@Override
	public Component getName(ItemStack stack) {
		LootCrate crate = getCrate(stack);
		return crate != null && !crate.itemName.isEmpty() ? Component.translatable(crate.itemName) : super.getName(stack);
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.UNCOMMON;
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
		if (allowedIn(tab)) {
			for (LootCrate lootCrate : LootCrate.LOOT_CRATES.values()) {
				items.add(lootCrate.createStack());
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.ftbquests.lootcrate.tooltip_1").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.ftbquests.lootcrate.tooltip_2").withStyle(ChatFormatting.GRAY));

		if (world == null || !ClientQuestFile.exists()) {
			return;
		}

		LootCrate crate = getCrate(stack);

		if (crate != null) {
			if (crate.itemName.isEmpty()) {
				tooltip.add(Component.empty());
				tooltip.add(crate.table.getMutableTitle().withStyle(ChatFormatting.GRAY));
			}
		} else if (stack.hasTag() && stack.getTag().contains("type")) {
			tooltip.add(Component.empty());
			tooltip.add(Component.literal(stack.getTag().getString("type")).withStyle(ChatFormatting.GRAY));
		}
	}
}
