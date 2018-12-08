package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftbquests.item.LootRarity;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * @author LatvianModder
 */
public class EntityLootTable
{
	public final int[] defaultValues = new int[1 + LootRarity.VALUES.length];
	public final int[] values = new int[defaultValues.length];

	public EntityLootTable(int n, int c, int u, int r, int e, int l)
	{
		defaultValues[0] = n;
		defaultValues[1] = c;
		defaultValues[2] = u;
		defaultValues[3] = r;
		defaultValues[4] = e;
		defaultValues[5] = l;
		System.arraycopy(defaultValues, 0, values, 0, values.length);
	}

	public void set(int[] ai)
	{
		if (ai.length < 6)
		{
			System.arraycopy(defaultValues, 0, values, 0, values.length);
		}
		else
		{
			System.arraycopy(ai, 0, values, 0, values.length);
		}
	}

	@Nullable
	public LootRarity getRarity(Random random)
	{
		int totalWeight = 0;

		for (int i : values)
		{
			totalWeight += i;
		}

		if (totalWeight <= 0)
		{
			return null;
		}

		int number = random.nextInt(totalWeight) + 1;
		int currentWeight = values[0];

		if (currentWeight < number)
		{
			for (LootRarity r : LootRarity.VALUES)
			{
				currentWeight += values[r.ordinal() + 1];

				if (currentWeight >= number)
				{
					return r;
				}
			}
		}

		return null;
	}

	public void getConfig(ConfigGroup config)
	{
		config.addInt("empty", () -> values[0], v -> values[0] = v, defaultValues[0], 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward_table.empty_weight"));

		for (LootRarity rarity : LootRarity.VALUES)
		{
			int i = rarity.ordinal() + 1;
			config.addInt(rarity.getID(), () -> values[i], v -> values[i] = v, defaultValues[i], 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation(rarity.getTranslationKey()));
		}
	}
}