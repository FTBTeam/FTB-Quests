package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftblib.lib.item.ItemEntry;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiRewardNotifications;
import com.feed_the_beast.ftbquests.net.MessageDisplayItemRewardToast;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.RewardTable;
import com.feed_the_beast.ftbquests.quest.reward.WeightedReward;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ItemLootcrate extends Item
{
	public final LootRarity rarity;

	public ItemLootcrate(LootRarity r)
	{
		rarity = r;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);

		int size = player.isSneaking() ? stack.getCount() : 1;

		if (!world.isRemote)
		{
			NBTTagCompound nbt = stack.getTagCompound();

			if (nbt != null && nbt.hasKey("use_reward_table") ? nbt.getBoolean("use_reward_table") : ServerQuestFile.INSTANCE.lootCrateTables[rarity.ordinal()] != 0)
			{
				int tableid = nbt != null ? nbt.getInteger("reward_table") : 0;

				if (tableid == 0)
				{
					tableid = ServerQuestFile.INSTANCE.lootCrateTables[rarity.ordinal()];
				}

				RewardTable table = ServerQuestFile.INSTANCE.getRewardTable(tableid);

				if (table != null)
				{
					int totalWeight = table.getTotalWeight(true);

					if (totalWeight > 0)
					{
						for (int j = 0; j < size * table.lootSize; j++)
						{
							int number = player.world.rand.nextInt(totalWeight) + 1;
							int currentWeight = table.emptyWeight;

							if (currentWeight < number)
							{
								for (WeightedReward reward : table.rewards)
								{
									currentWeight += reward.weight;

									if (currentWeight >= number)
									{
										reward.reward.claim((EntityPlayerMP) player);
										break;
									}
								}
							}
						}
					}
				}
			}
			else
			{
				int lootSize = nbt != null ? nbt.getShort("loot_size") & 0xFFFF : 0;

				if (lootSize == 0)
				{
					lootSize = ServerQuestFile.INSTANCE.lootSize;
				}

				String lootTable = nbt != null ? nbt.getString("loot_table") : "";
				ResourceLocation lootTableLocation = lootTable.isEmpty() ? ServerQuestFile.INSTANCE.lootTables[rarity.ordinal()] : new ResourceLocation(lootTable);

				LootTable table = world.getLootTableManager().getLootTableFromLocation(lootTableLocation);
				IInventory inventory = new InventoryBasic("", true, lootSize);
				LootContext context = new LootContext.Builder((WorldServer) world).withLuck(player.getLuck()).withPlayer(player).build();
				Object2IntOpenHashMap<ItemEntry> map = new Object2IntOpenHashMap<>();
				map.defaultReturnValue(0);

				for (int i = 0; i < size; i++)
				{
					table.fillInventory(inventory, world.rand, context);

					for (int j = 0; j < lootSize; j++)
					{
						ItemStack stack1 = inventory.getStackInSlot(j);

						if (!stack1.isEmpty())
						{
							ItemEntry entry = ItemEntry.get(stack1);
							map.put(entry, map.getInt(entry) + stack1.getCount());
							ItemHandlerHelper.giveItemToPlayer(player, stack1);
						}
					}

					inventory.clear();
				}

				for (Object2IntOpenHashMap.Entry<ItemEntry> entry : map.object2IntEntrySet())
				{
					new MessageDisplayItemRewardToast(entry.getKey().getStack(entry.getIntValue(), false)).sendTo((EntityPlayerMP) player);
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
		return rarity == LootRarity.RARE || rarity == LootRarity.EPIC || rarity == LootRarity.LEGENDARY;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.UNCOMMON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		tooltip.add(rarity.getColor() + I18n.format(rarity.getTranslationKey()));

		if (!ClientQuestFile.exists())
		{
			return;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null && nbt.hasKey("use_reward_table") ? nbt.getBoolean("use_reward_table") : ClientQuestFile.INSTANCE.lootCrateTables[rarity.ordinal()] != 0)
		{
			int tableid = nbt != null ? nbt.getInteger("reward_table") : 0;

			if (tableid == 0)
			{
				tableid = ClientQuestFile.INSTANCE.lootCrateTables[rarity.ordinal()];
			}

			RewardTable table = ClientQuestFile.INSTANCE.getRewardTable(tableid);

			if (table != null)
			{
				table.addMouseOverText(tooltip, true, true);
			}
		}

		if (ClientQuestFile.INSTANCE.canEdit())
		{
			String lootTable = nbt != null ? nbt.getString("loot_table") : "";

			if (lootTable.isEmpty())
			{
				lootTable = ClientQuestFile.INSTANCE.lootTables[rarity.ordinal()].toString();
			}

			int lootSize = nbt != null ? nbt.getShort("loot_size") & 0xFFFF : 0;

			if (lootSize == 0)
			{
				lootSize = ClientQuestFile.INSTANCE.lootSize;
			}

			tooltip.add(TextFormatting.DARK_GRAY + lootTable + " x " + lootSize);
		}
	}
}