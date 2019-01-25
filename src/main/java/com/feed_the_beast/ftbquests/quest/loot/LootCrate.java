package com.feed_the_beast.ftbquests.quest.loot;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextComponentTranslation;

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

	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("string_id", stringID);

		if (!itemName.isEmpty())
		{
			nbt.setString("item_name", itemName);
		}

		nbt.setInteger("color", color.rgb());

		if (glow)
		{
			nbt.setBoolean("glow", true);
		}

		NBTTagCompound nbt1 = new NBTTagCompound();
		drops.writeData(nbt1);
		nbt.setTag("drops", nbt1);
	}

	public void readData(NBTTagCompound nbt)
	{
		stringID = nbt.getString("string_id");
		itemName = nbt.getString("item_name");
		color = Color4I.rgb(nbt.getInteger("color"));
		glow = nbt.getBoolean("glow");
		drops.readData(nbt.getCompoundTag("drops"));
	}

	public void writeNetData(DataOut data)
	{
		data.writeString(stringID);
		data.writeString(itemName);
		data.writeInt(color.rgb());
		data.writeBoolean(glow);
		drops.writeNetData(data);
	}

	public void readNetData(DataIn data)
	{
		stringID = data.readString();
		itemName = data.readString();
		color = Color4I.rgb(data.readInt());
		glow = data.readBoolean();
		drops.readNetData(data);
	}

	public void getConfig(ConfigGroup config)
	{
		config.addString("id", () -> stringID, v -> stringID = v, "", Pattern.compile("[a-z0-9_]+"));
		config.addString("item_name", () -> itemName, v -> itemName = v, "");
		config.addString("color", () -> color.toString(), v -> color = Color4I.fromJson(new JsonPrimitive(v)), "#FFFFFF", Pattern.compile("^#[a-fA-F0-9]{6}$"));
		config.addBool("glow", () -> glow, v -> glow = v, true);

		ConfigGroup d = config.getGroup("drops");
		d.setDisplayName(new TextComponentTranslation("ftbquests.loot.entitydrops"));
		d.addInt("passive", () -> drops.passive, v -> drops.passive = v, 0, 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.loot.entitytype.passive"));
		d.addInt("monster", () -> drops.monster, v -> drops.monster = v, 0, 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.loot.entitytype.monster"));
		d.addInt("boss", () -> drops.boss, v -> drops.boss = v, 0, 0, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.loot.entitytype.boss"));
	}

	public ItemStack createStack()
	{
		ItemStack stack = new ItemStack(FTBQuestsItems.LOOTCRATE);
		stack.setTagInfo("type", new NBTTagString(stringID.isEmpty() ? table.getCodeString() : stringID));
		return stack;
	}
}