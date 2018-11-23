package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.reward.XPLevelsReward;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemRandomLootcrate extends Item
{
	public static class WeightedReward
	{
		public final QuestReward reward;
		public int weight;

		public WeightedReward(QuestReward r, int w)
		{
			reward = r;
			weight = Math.max(w, 1);
		}
	}

	public static class Data implements ICapabilitySerializable<NBTTagCompound>
	{
		public final List<WeightedReward> rewards = new ObjectArrayList<>();

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CAP;
		}

		@Nullable
		@Override
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CAP ? (T) this : null;
		}

		@Override
		public NBTTagCompound serializeNBT()
		{
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();

			for (WeightedReward reward : rewards)
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				reward.reward.writeData(nbt1);

				if (reward.reward.getType() != FTBQuestsRewards.ITEM)
				{
					nbt1.setString("type", reward.reward.getType().getTypeForNBT());
				}

				if (reward.weight > 1)
				{
					nbt1.setInteger("weight", reward.weight);
				}

				list.appendTag(nbt1);
			}

			nbt.setTag("rewards", list);
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt)
		{
			rewards.clear();
			NBTTagList list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbt1 = list.getCompoundTagAt(i);
				QuestReward reward = QuestRewardType.createReward(Quest.FAKE_QUEST, nbt1.getString("type"));

				if (reward != null)
				{
					reward.readData(nbt1);
					rewards.add(new WeightedReward(reward, nbt1.getInteger("weight")));
				}
			}
		}
	}

	@CapabilityInject(Data.class)
	public static Capability<Data> CAP;

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);

		if (!world.isRemote)
		{
			Data data = stack.getCapability(CAP, null);
			int totalWeight = 0;

			for (WeightedReward reward : data.rewards)
			{
				totalWeight += reward.weight;
			}

			int number = world.rand.nextInt(totalWeight);
			int currentWeight = 0;

			for (WeightedReward reward : data.rewards)
			{
				currentWeight += reward.weight;

				if (currentWeight >= number)
				{
					reward.reward.claim((EntityPlayerMP) player);
					break;
				}
			}

			world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F + world.rand.nextFloat() * 0.4F);
		}
		else
		{
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

		stack.shrink(1);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public Data initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		return new Data();
	}

	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack)
	{
		return stack.getCapability(CAP, null).serializeNBT();
	}

	@Override
	public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		if (nbt != null)
		{
			stack.getCapability(CAP, null).deserializeNBT(nbt);
		}
	}

	@Override
	public boolean hasEffect(ItemStack stack)
	{
		return true;
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
			ItemStack stack = new ItemStack(this);
			Data data = stack.getCapability(CAP, null);
			ItemReward reward;

			reward = new ItemReward(Quest.FAKE_QUEST);
			reward.stack = new ItemStack(Items.POTATO);
			data.rewards.add(new WeightedReward(reward, 5));

			reward = new ItemReward(Quest.FAKE_QUEST);
			reward.stack = new ItemStack(Items.CARROT);
			data.rewards.add(new WeightedReward(reward, 5));

			reward = new ItemReward(Quest.FAKE_QUEST);
			reward.stack = new ItemStack(Items.APPLE);
			data.rewards.add(new WeightedReward(reward, 12));

			reward = new ItemReward(Quest.FAKE_QUEST);
			reward.stack = new ItemStack(Items.POISONOUS_POTATO);
			data.rewards.add(new WeightedReward(reward, 1));

			XPLevelsReward reward1 = new XPLevelsReward(Quest.FAKE_QUEST);
			reward1.xpLevels = 5;
			data.rewards.add(new WeightedReward(reward1, 3));

			items.add(stack);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		Data data = stack.getCapability(CAP, null);

		if (data == null)
		{
			return;
		}

		int totalWeight = 0;

		for (WeightedReward reward : data.rewards)
		{
			totalWeight += reward.weight;
		}

		for (WeightedReward reward : data.rewards)
		{
			tooltip.add(TextFormatting.GRAY + "  - " + reward.reward.getDisplayName().getFormattedText() + TextFormatting.DARK_GRAY + " [" + (reward.weight * 100 / totalWeight) + "%]");
		}
	}
}