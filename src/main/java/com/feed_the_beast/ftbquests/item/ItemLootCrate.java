package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.loot.LootCrate;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemLootCrate extends Item
{
	@Nullable
	public static LootCrate getCrate(@Nullable World world, ItemStack stack)
	{
		if (stack.hasTagCompound() && stack.getItem() instanceof ItemLootCrate)
		{
			QuestFile file = FTBQuests.PROXY.getQuestFile(world);
			return file == null ? null : file.getLootCrate(stack.getTagCompound().getString("type"));
		}

		return null;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		LootCrate crate = getCrate(world, stack);

		if (crate == null)
		{
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}

		int size = player.isSneaking() ? stack.getCount() : 1;

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
								reward.reward.claim((EntityPlayerMP) player, true);
								break;
							}
						}
					}
				}
			}

			world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F + world.rand.nextFloat() * 0.4F);
		}
		else
		{
			new GuiRewardNotifications().openGui();

			for (int i = 0; i < 5; i++)
			{
				Vec3d vec3d = new Vec3d(((double) world.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
				vec3d = vec3d.rotatePitch(-player.rotationPitch * 0.017453292F);
				vec3d = vec3d.rotateYaw(-player.rotationYaw * 0.017453292F);
				double d0 = (double) (-world.rand.nextFloat()) * 0.6D - 0.3D;
				Vec3d vec3d1 = new Vec3d(((double) world.rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
				vec3d1 = vec3d1.rotatePitch(-player.rotationPitch * 0.017453292F);
				vec3d1 = vec3d1.rotateYaw(-player.rotationYaw * 0.017453292F);
				vec3d1 = vec3d1.add(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);
				world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z, Item.getIdFromItem(this), 0);
			}
		}

		stack.shrink(size);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack)
	{
		LootCrate crate = getCrate(null, stack);
		return crate != null && crate.glow;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		LootCrate crate = getCrate(null, stack);
		return crate != null && !crate.itemName.isEmpty() ? crate.itemName : super.getItemStackDisplayName(stack);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.UNCOMMON;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		if (isInCreativeTab(tab))
		{
			QuestFile file = FTBQuests.PROXY.getQuestFile(null);

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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (world == null || !ClientQuestFile.exists())
		{
			return;
		}

		LootCrate crate = getCrate(world, stack);

		if (crate != null)
		{
			if (crate.itemName.isEmpty())
			{
				tooltip.add(crate.table.getTitle());
			}
		}
		else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("type"))
		{
			tooltip.add(stack.getTagCompound().getString("type"));
		}
	}
}