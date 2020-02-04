package com.feed_the_beast.ftbquests.quest.loot;

import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;

import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public final class LootCrate
{
	public final RewardTable table;
	public String stringID;
	public String itemName;
	public Color4I color;
	public boolean glow;
	public EntityWeight drops;

	public LootCrate(RewardTable t)
	{
		table = t;
		stringID = t.toString();
		itemName = "";
		color = Color4I.WHITE;
		glow = false;
		drops = new EntityWeight();
	}

	public void writeData(CompoundNBT nbt)
	{
		nbt.putString("string_id", stringID);

		if (!itemName.isEmpty())
		{
			nbt.putString("item_name", itemName);
		}

		nbt.putInt("color", color.rgb());

		if (glow)
		{
			nbt.putBoolean("glow", true);
		}

		CompoundNBT nbt1 = new CompoundNBT();
		drops.writeData(nbt1);
		nbt.put("drops", nbt1);
	}

	public void readData(CompoundNBT nbt)
	{
		stringID = nbt.getString("string_id");
		itemName = nbt.getString("item_name");
		color = Color4I.rgb(nbt.getInt("color"));
		glow = nbt.getBoolean("glow");
		drops.readData(nbt.getCompound("drops"));
	}

	public void writeNetData(PacketBuffer data)
	{
		data.writeString(stringID);
		data.writeString(itemName);
		data.writeInt(color.rgb());
		data.writeBoolean(glow);
		drops.writeNetData(data);
	}

	public void readNetData(PacketBuffer data)
	{
		stringID = data.readString();
		itemName = data.readString();
		color = Color4I.rgb(data.readInt());
		glow = data.readBoolean();
		drops.readNetData(data);
	}

	public void getConfig(ConfigGroup config)
	{
		config.addString("id", stringID, v -> stringID = v, "", Pattern.compile("[a-z0-9_]+"));
		config.addString("item_name", itemName, v -> itemName = v, "");
		config.addString("color", color.toString(), v -> color = Color4I.fromString(v), "#FFFFFF", Pattern.compile("^#[a-fA-F0-9]{6}$"));
		config.addBool("glow", glow, v -> glow = v, true);

		ConfigGroup d = config.getGroup("drops");
		d.setNameKey("ftbquests.loot.entitydrops");
		d.addInt("passive", drops.passive, v -> drops.passive = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.passive");
		d.addInt("monster", drops.monster, v -> drops.monster = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.monster");
		d.addInt("boss", drops.boss, v -> drops.boss = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.boss");
	}

	public String getStringID()
	{
		return stringID.isEmpty() ? QuestObjectBase.getCodeString(table) : stringID;
	}

	public ItemStack createStack()
	{
		ItemStack stack = new ItemStack(FTBQuestsItems.LOOTCRATE);
		stack.setTagInfo("type", StringNBT.valueOf(getStringID()));
		return stack;
	}
}