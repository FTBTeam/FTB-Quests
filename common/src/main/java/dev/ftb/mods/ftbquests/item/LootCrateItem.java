package dev.ftb.mods.ftbquests.item;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author LatvianModder
 */
public class LootCrateItem extends Item {
	public LootCrateItem() {
		super(ModItems.defaultProps()
				.rarity(Rarity.UNCOMMON)
				.component(ModDataComponents.LOOT_CRATE.get(), "")
		);
	}

	@Nullable
	public static LootCrate getCrate(ItemStack stack) {
		return FTBQuests.getComponent(stack, ModDataComponents.LOOT_CRATE)
				.map(type -> LootCrate.LOOT_CRATES.get(type))
				.orElse(null);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		LootCrate crate = getCrate(stack);

		if (crate == null) {
			return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
		}

		int nItems = player.isCrouching() ? stack.getCount() : 1;

		if (!world.isClientSide) {
			for (WeightedReward wr : crate.getTable().generateWeightedRandomRewards(player.getRandom(), nItems, true)) {
				wr.getReward().claim((ServerPlayer) player, true);
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

		stack.shrink(nItems);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		LootCrate crate = getCrate(stack);
		return crate != null && crate.isGlow();
	}

	@Override
	public Component getName(ItemStack stack) {
		LootCrate crate = getCrate(stack);
		return crate != null && !crate.getItemName().isEmpty() ? Component.translatable(crate.getItemName()) : super.getName(stack);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		if (context.registries() == null || !ClientQuestFile.exists()) {
			return;
		}

		LootCrate crate = getCrate(stack);
		if (crate != null) {
			if (crate.getItemName().isEmpty()) {
				// if crate doesn't have an item name, show the reward table's name in the tooltip
				tooltip.add(crate.getTable().getMutableTitle().withStyle(ChatFormatting.YELLOW));
				tooltip.add(Component.empty());
			}
			tooltip.add(Component.translatable("item.ftbquests.lootcrate.tooltip_1").withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("item.ftbquests.lootcrate.tooltip_2").withStyle(ChatFormatting.GRAY));
		} else {
			String name = stack.getOrDefault(ModDataComponents.LOOT_CRATE.get(), "");
			tooltip.add(Component.translatable("item.ftbquests.lootcrate.missing", name).withStyle(ChatFormatting.RED));
		}
	}
}
