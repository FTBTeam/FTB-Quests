package com.feed_the_beast.ftbquests.item;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
			String lootTable = stack.hasTagCompound() ? stack.getTagCompound().getString("loot_table") : "";
			ResourceLocation lootTableLocation = lootTable.isEmpty() ? ServerQuestFile.INSTANCE.lootTables[rarity.ordinal()] : new ResourceLocation(lootTable);

			int lootSize = stack.hasTagCompound() ? stack.getTagCompound().getShort("loot_size") & 0xFFFF : 0;

			if (lootSize == 0)
			{
				lootSize = ServerQuestFile.INSTANCE.lootSize;
			}

			LootTable table = world.getLootTableManager().getLootTableFromLocation(lootTableLocation);
			IInventory inventory = new InventoryBasic("", true, lootSize);
			LootContext context = new LootContext.Builder((WorldServer) world).withLuck(player.getLuck()).withPlayer(player).build();

			for (int i = 0; i < size; i++)
			{
				table.fillInventory(inventory, world.rand, context);

				for (int j = 0; j < lootSize; j++)
				{
					ItemStack stack1 = inventory.getStackInSlot(j);

					if (!stack1.isEmpty())
					{
						ItemHandlerHelper.giveItemToPlayer(player, stack1);
					}
				}

				inventory.clear();
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

		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.canEdit())
		{
			String lootTable = stack.hasTagCompound() ? stack.getTagCompound().getString("loot_table") : "";

			if (lootTable.isEmpty())
			{
				lootTable = ClientQuestFile.INSTANCE.lootTables[rarity.ordinal()].toString();
			}

			int lootSize = stack.hasTagCompound() ? stack.getTagCompound().getShort("loot_size") & 0xFFFF : 0;

			if (lootSize == 0)
			{
				lootSize = ClientQuestFile.INSTANCE.lootSize;
			}

			tooltip.add(TextFormatting.DARK_GRAY + lootTable + " x " + lootSize);
		}
	}
}