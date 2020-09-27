package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemLootCrate extends Item
{
	public ItemLootCrate()
	{
		super(new Properties().group(FTBQuests.ITEM_GROUP));
	}

	@Nullable
	public static LootCrate getCrate(@Nullable IWorld world, ItemStack stack)
	{
		if (stack.hasTag() && stack.getItem() instanceof ItemLootCrate)
		{
			QuestFile file = world == null ? FTBQuests.PROXY.getQuestFile(Tristate.DEFAULT) : FTBQuests.PROXY.getQuestFile(world);
			return file == null ? null : file.getLootCrate(stack.getTag().getString("type"));
		}

		return null;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		LootCrate crate = getCrate(world, stack);

		if (crate == null)
		{
			return new ActionResult<>(ActionResultType.SUCCESS, stack);
		}

		int size = player.isCrouching() ? stack.getCount() : 1;

		if (!world.isRemote)
		{
			int totalWeight = crate.table.getTotalWeight(true);

			if (totalWeight > 0)
			{
				for (int j = 0; j < size * crate.table.lootSize; j++)
				{
					int number = player.world.rand.nextInt(totalWeight) + 1;
					int currentWeight = crate.table.emptyWeight;

					if (currentWeight < number)
					{
						for (WeightedReward reward : crate.table.rewards)
						{
							currentWeight += reward.weight;

							if (currentWeight >= number)
							{
								reward.reward.claim((ServerPlayerEntity) player, true);
								break;
							}
						}
					}
				}
			}

			world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F + world.rand.nextFloat() * 0.4F);
		}
		else
		{
			new GuiRewardNotifications().openGui();

			for (int i = 0; i < 5; i++)
			{
				Vector3d vec3d = new Vector3d(((double) world.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
				vec3d = vec3d.rotatePitch(-player.rotationPitch * 0.017453292F);
				vec3d = vec3d.rotateYaw(-player.rotationYaw * 0.017453292F);
				double d0 = (double) (-world.rand.nextFloat()) * 0.6D - 0.3D;
				Vector3d vec3d1 = new Vector3d(((double) world.rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
				vec3d1 = vec3d1.rotatePitch(-player.rotationPitch * 0.017453292F);
				vec3d1 = vec3d1.rotateYaw(-player.rotationYaw * 0.017453292F);
				vec3d1 = vec3d1.add(player.getPosX(), player.getPosY() + (double) player.getEyeHeight(), player.getPosZ());
				world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
			}
		}

		stack.shrink(size);
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack)
	{
		LootCrate crate = getCrate(null, stack);
		return crate != null && crate.glow;
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack)
	{
		LootCrate crate = getCrate(null, stack);
		return crate != null && !crate.itemName.isEmpty() ? new StringTextComponent(crate.itemName) : super.getDisplayName(stack);
	}

	@Override
	public Rarity getRarity(ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	@Override
	public void fillItemGroup(ItemGroup tab, NonNullList<ItemStack> items)
	{
		if (isInGroup(tab))
		{
			QuestFile file = FTBQuests.PROXY.getQuestFile(Tristate.DEFAULT);

			if (file != null)
			{
				for (RewardTable table : file.rewardTables)
				{
					if (table.lootCrate != null)
					{
						items.add(table.lootCrate.createStack());
					}
				}
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		tooltip.add(new TranslationTextComponent("item.ftbquests.lootcrate.tooltip_1").mergeStyle(TextFormatting.GRAY));
		tooltip.add(new TranslationTextComponent("item.ftbquests.lootcrate.tooltip_2").mergeStyle(TextFormatting.GRAY));

		if (world == null || !ClientQuestFile.exists())
		{
			return;
		}

		LootCrate crate = getCrate(world, stack);

		if (crate != null)
		{
			if (crate.itemName.isEmpty())
			{
				tooltip.add(new StringTextComponent(""));
				tooltip.add(crate.table.getTitle().mergeStyle(TextFormatting.GRAY));
			}
		}
		else if (stack.hasTag() && stack.getTag().contains("type"))
		{
			tooltip.add(new StringTextComponent(""));
			tooltip.add(new StringTextComponent(stack.getTag().getString("type")).mergeStyle(TextFormatting.GRAY));
		}
	}
}