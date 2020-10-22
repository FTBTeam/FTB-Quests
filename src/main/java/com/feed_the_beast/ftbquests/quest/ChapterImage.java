package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.net.MessageEditObject;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class ChapterImage implements Movable
{
	public Chapter chapter;
	public double x, y;
	public double width, height;
	public double rotation;
	public Icon image;
	public List<String> hover;
	public String click;

	public ChapterImage(Chapter c)
	{
		chapter = c;
		x = y = 0D;
		width = 1D;
		height = 1D;
		rotation = 0D;
		image = Icon.getIcon("minecraft:textures/gui/presets/isles.png");
		hover = new ArrayList<>();
		click = "";
	}

	public void writeData(CompoundNBT nbt)
	{
		nbt.putDouble("x", x);
		nbt.putDouble("y", y);
		nbt.putDouble("width", width);
		nbt.putDouble("height", height);
		nbt.putDouble("rotation", rotation);
		nbt.putString("image", image.toString());

		ListNBT hoverTag = new ListNBT();

		for (String s : hover)
		{
			hoverTag.add(StringNBT.valueOf(s));
		}

		nbt.put("hover", hoverTag);
		nbt.putString("click", click);
	}

	public void readData(CompoundNBT nbt)
	{
		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		width = nbt.getDouble("width");
		height = nbt.getDouble("height");
		rotation = nbt.getDouble("rotation");
		image = Icon.getIcon(nbt.getString("image"));

		hover.clear();
		ListNBT hoverTag = nbt.getList("hover", Constants.NBT.TAG_STRING);

		for (int i = 0; i < hoverTag.size(); i++)
		{
			hover.add(hoverTag.getString(i));
		}

		click = nbt.getString("click");
	}

	public void writeNetData(PacketBuffer buffer)
	{
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(width);
		buffer.writeDouble(height);
		buffer.writeDouble(rotation);
		NetUtils.writeIcon(buffer, image);
		NetUtils.writeStrings(buffer, hover);
		buffer.writeString(click);
	}

	public void readNetData(PacketBuffer buffer)
	{
		x = buffer.readDouble();
		y = buffer.readDouble();
		width = buffer.readDouble();
		height = buffer.readDouble();
		rotation = buffer.readDouble();
		image = NetUtils.readIcon(buffer);
		NetUtils.readStrings(buffer, hover);
		click = buffer.readString();
	}

	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("width", width, v -> width = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("height", height, v -> height = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("rotation", rotation, v -> rotation = v, 0, -180, 180);
		config.addString("image", image.toString(), v -> image = Icon.getIcon(v), "minecraft:textures/gui/presets/isles.png");
		config.addList("hover", hover, new ConfigString(), "");
		config.addString("click", click, v -> click = v, "");
	}

	@Override
	public Chapter getChapter()
	{
		return chapter;
	}

	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public double getY()
	{
		return y;
	}

	@Override
	public double getWidth()
	{
		return width;
	}

	@Override
	public double getHeight()
	{
		return height;
	}

	@Override
	public String getShape()
	{
		return "square";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void move(Chapter to, double _x, double _y)
	{
		x = _x;
		y = _y;

		if (to != chapter)
		{
			chapter.images.remove(this);
			new MessageEditObject(chapter).sendToServer();

			chapter = to;
			chapter.images.add(this);
		}

		new MessageEditObject(chapter).sendToServer();
	}
}