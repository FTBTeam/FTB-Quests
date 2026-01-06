package dev.ftb.mods.ftbquests.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class LootCrateItem extends Item {
	public LootCrateItem(ResourceKey<Item> id) {
		super(ModItems.defaultProps()
				.setId(id)
				.rarity(Rarity.UNCOMMON)
				.component(ModDataComponents.LOOT_CRATE.get(), "")
				.component(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), List.of()))
		);
	}

	@Nullable
	public static LootCrate getCrate(ItemStack stack, boolean isClientSide) {
		return FTBQuests.getComponent(stack, ModDataComponents.LOOT_CRATE)
				.map(type -> LootCrate.getLootCrates(isClientSide).get(type))
				.orElse(null);
	}

	@Nullable
	public static LootCrate getCrate(ItemStack stack) {
		return getCrate(stack, Platform.getEnv() == EnvType.CLIENT);
	}



	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		LootCrate crate = getCrate(stack, player.level().isClientSide());

		if (crate == null) {
			return InteractionResult.FAIL;
		}

		int nItems = player.isCrouching() ? stack.getCount() : 1;

		if (!world.isClientSide()) {
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
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		LootCrate crate = getCrate(stack, true);
		return crate != null && crate.isGlow();
	}

	@Override
	public Component getName(ItemStack stack) {
		LootCrate crate = getCrate(stack);
		return crate != null && !crate.getItemName().isEmpty() ? Component.translatable(crate.getItemName()) : super.getName(stack);
	}

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag flag) {
        if (context.registries() == null || !ClientQuestFile.exists()) {
            return;
        }

        LootCrate crate = getCrate(stack, true);
        if (crate != null) {
            if (crate.getItemName().isEmpty()) {
                // if crate doesn't have an item name, show the reward table's name in the tooltip
                consumer.accept(Component.literal(crate.getStringID()).withStyle(ChatFormatting.YELLOW));
                consumer.accept(Component.empty());
            }
            consumer.accept(Component.translatable("item.ftbquests.lootcrate.tooltip_1").withStyle(ChatFormatting.GRAY));
            consumer.accept(Component.translatable("item.ftbquests.lootcrate.tooltip_2").withStyle(ChatFormatting.GRAY));
        } else {
            String name = stack.getOrDefault(ModDataComponents.LOOT_CRATE.get(), "");
            // stay quiet if there's no loot crate ID at all
            if (!name.isEmpty()) {
                consumer.accept(Component.translatable("item.ftbquests.lootcrate.missing", name).withStyle(ChatFormatting.RED));
            }
        }
    }

	public record LootCrateItemTintSource(int index, int defaultColor) implements ItemTintSource {
		public static final MapCodec<LootCrateItemTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
				i -> i.group(
								ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(LootCrateItemTintSource::index),
								ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(LootCrateItemTintSource::defaultColor)
						)
						.apply(i, LootCrateItemTintSource::new)
		);

		@Override
		public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
//			ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
//				LootCrate crate = LootCrateItem.getCrate(stack, true);
//				return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.getColor().rgb());
//			}, ModItems.LOOTCRATE.get());

			// TODO: @since 21.11: We need to validate this works, and we need to ensure it's not called a lot of times.
			if (itemStack.getItem() instanceof LootCrateItem) {
				LootCrate crate = LootCrateItem.getCrate(itemStack, true);
				return crate == null ? 0xFFFFFFFF : (0xFF000000 | crate.getColor().rgb());
			}

			return 0xFFFFFFFF;
		}

		@Override
		public MapCodec<? extends ItemTintSource> type() {
			return MAP_CODEC;
		}
	}
}
